package com.hmi.ridedashboard.domain

import app.cash.turbine.test
import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.domain.model.SpeedReading
import com.hmi.ridedashboard.domain.model.VehicleSignal
import com.hmi.ridedashboard.domain.model.VehicleState
import com.hmi.ridedashboard.domain.repository.VehicleRepository
import com.hmi.ridedashboard.domain.usecase.ObserveVehicleStateUseCase
import com.hmi.ridedashboard.domain.usecase.RideStateResolver
import com.hmi.ridedashboard.domain.usecase.SpeedValidator
import com.hmi.ridedashboard.testutil.FakeVehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveVehicleStateUseCaseTest {

    private val repository = FakeVehicleRepository()

    private fun useCase(repo: VehicleRepository = repository) =
        ObserveVehicleStateUseCase(repo, SpeedValidator(), RideStateResolver(), STALE_TIMEOUT)

    @Test
    fun `each frame produces a live state for the scenario sequence`() = runTest {
        useCase()().test {
            assertEquals(VehicleState.Initial, awaitItem())

            repository.send(0f, motorOn = false, killSwitchActive = false)
            assertEquals(RideState.PARKED, awaitItem().rideState)

            repository.send(0f, motorOn = true, killSwitchActive = false)
            assertEquals(RideState.READY, awaitItem().rideState)

            repository.send(25f, motorOn = true, killSwitchActive = false)
            awaitItem().let {
                assertEquals(RideState.RIDING, it.rideState)
                assertEquals(SpeedReading.Valid(25), it.speed)
                assertEquals(SignalHealth.LIVE, it.health)
            }

            repository.send(50f, motorOn = true, killSwitchActive = false)
            assertEquals(RideState.ACCELERATING, awaitItem().rideState)

            repository.send(0f, motorOn = false, killSwitchActive = true)
            awaitItem().let {
                assertEquals(RideState.MOTOR_DISABLED, it.rideState)
                assertEquals(MotorStatus.OFF, it.motor)
                assertEquals(KillSwitchStatus.ACTIVE, it.killSwitch)
            }
        }
    }

    @Test
    fun `invalid and missing signals are reported without failing the stream`() = runTest {
        useCase()().test {
            skipItems(1)

            repository.send(-12f, motorOn = true, killSwitchActive = false)
            awaitItem().let {
                assertEquals(SpeedReading.Invalid, it.speed)
                assertEquals(RideState.UNKNOWN, it.rideState)
                assertEquals(SignalHealth.LIVE, it.health)
            }

            repository.send(null, motorOn = null, killSwitchActive = null)
            awaitItem().let {
                assertEquals(SpeedReading.Unavailable, it.speed)
                assertEquals(MotorStatus.UNKNOWN, it.motor)
                assertEquals(KillSwitchStatus.UNKNOWN, it.killSwitch)
            }

            // Stream keeps working after bad frames.
            repository.send(25f, motorOn = true, killSwitchActive = false)
            assertEquals(SpeedReading.Valid(25), awaitItem().speed)
        }
    }

    @Test
    fun `state goes stale when frames stop and recovers on the next frame`() = runTest {
        useCase()().test {
            skipItems(1)
            repository.send(25f, motorOn = true, killSwitchActive = false)
            val live = awaitItem()
            val liveAt = currentTime

            val stale = awaitItem()
            assertEquals(SignalHealth.STALE, stale.health)
            assertEquals(STALE_TIMEOUT, currentTime - liveAt)
            // Last known values are kept so the UI can show them dimmed.
            assertEquals(live.speed, stale.speed)

            repository.send(30f, motorOn = true, killSwitchActive = false)
            assertEquals(SignalHealth.LIVE, awaitItem().health)
        }
    }

    @Test
    fun `source failure is reported as lost then retried`() = runTest {
        var subscriptions = 0
        val flaky = object : VehicleRepository {
            override fun observeVehicleSignals() = flow {
                if (++subscriptions == 1) throw IOException("bus error")
                emit(VehicleSignal(25f, motorOn = true, killSwitchActive = false))
            }
        }

        useCase(flaky)().test {
            assertEquals(SignalHealth.AWAITING, awaitItem().health)
            assertEquals(SignalHealth.LOST, awaitItem().health)
            awaitItem().let {
                assertEquals(SignalHealth.LIVE, it.health)
                assertEquals(SpeedReading.Valid(25), it.speed)
            }
            assertEquals(2, subscriptions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val STALE_TIMEOUT = 2_500L
    }
}
