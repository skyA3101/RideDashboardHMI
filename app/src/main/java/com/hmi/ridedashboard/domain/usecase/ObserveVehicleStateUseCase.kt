package com.hmi.ridedashboard.domain.usecase

import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.domain.model.StaleTimeoutMillis
import com.hmi.ridedashboard.domain.model.VehicleSignal
import com.hmi.ridedashboard.domain.model.VehicleState
import com.hmi.ridedashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

/**
 * Turns raw frames into validated [VehicleState]s and handles the failure modes:
 * invalid/missing signals, stale data (no frame within [staleTimeoutMillis]) and
 * source errors (reported as [SignalHealth.LOST], then retried).
 */
class ObserveVehicleStateUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val speedValidator: SpeedValidator,
    private val rideStateResolver: RideStateResolver,
    @StaleTimeoutMillis private val staleTimeoutMillis: Long,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<VehicleState> =
        repository.observeVehicleSignals()
            .map<VehicleSignal, SourceEvent> { SourceEvent.Frame(it) }
            .retryWhen { _, _ ->
                emit(SourceEvent.Failed)
                delay(RETRY_DELAY_MILLIS)
                true
            }
            .runningFold(VehicleState.Initial) { state, event ->
                when (event) {
                    is SourceEvent.Frame -> reduce(state, event.signal)
                    SourceEvent.Failed -> state.copy(health = SignalHealth.LOST)
                }
            }
            .transformLatest { state ->
                emit(state)
                // A newer frame cancels this wait; if none arrives in time the data is stale.
                if (state.health == SignalHealth.LIVE || state.health == SignalHealth.AWAITING) {
                    delay(staleTimeoutMillis)
                    emit(state.copy(health = SignalHealth.STALE))
                }
            }
            .distinctUntilChanged()

    private fun reduce(previous: VehicleState, signal: VehicleSignal): VehicleState {
        val speed = speedValidator.validate(signal.speedKmh)
        val motor = when (signal.motorOn) {
            true -> MotorStatus.ON
            false -> MotorStatus.OFF
            null -> MotorStatus.UNKNOWN
        }
        val killSwitch = when (signal.killSwitchActive) {
            true -> KillSwitchStatus.ACTIVE
            false -> KillSwitchStatus.INACTIVE
            null -> KillSwitchStatus.UNKNOWN
        }
        return VehicleState(
            speed = speed,
            motor = motor,
            killSwitch = killSwitch,
            rideState = rideStateResolver.resolve(previous.speed, speed, motor, killSwitch),
            health = SignalHealth.LIVE,
        )
    }

    private sealed interface SourceEvent {
        data class Frame(val signal: VehicleSignal) : SourceEvent
        data object Failed : SourceEvent
    }

    companion object {
        const val RETRY_DELAY_MILLIS = 2_000L
    }
}
