package com.hmi.ridedashboard.presentation

import app.cash.turbine.test
import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.domain.usecase.ObserveFaultInjectionUseCase
import com.hmi.ridedashboard.domain.usecase.ObserveVehicleStateUseCase
import com.hmi.ridedashboard.domain.usecase.RideStateResolver
import com.hmi.ridedashboard.domain.usecase.SetFaultInjectionUseCase
import com.hmi.ridedashboard.domain.usecase.SpeedValidator
import com.hmi.ridedashboard.presentation.dashboard.DashboardUiState
import com.hmi.ridedashboard.presentation.dashboard.DashboardViewModel
import com.hmi.ridedashboard.presentation.dashboard.SpeedUi
import com.hmi.ridedashboard.testutil.FakeSimulationController
import com.hmi.ridedashboard.testutil.FakeVehicleRepository
import com.hmi.ridedashboard.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeVehicleRepository()
    private val controller = FakeSimulationController()

    private fun viewModel() = DashboardViewModel(
        ObserveVehicleStateUseCase(repository, SpeedValidator(), RideStateResolver(), staleTimeoutMillis = 2_500L),
        ObserveFaultInjectionUseCase(controller),
        SetFaultInjectionUseCase(controller),
    )

    @Test
    fun `ui state updates from the vehicle feed without a refresh`() = runTest {
        viewModel().uiState.test {
            assertEquals(DashboardUiState(), awaitItem())

            repository.send(50f, motorOn = true, killSwitchActive = false)
            awaitItem().let {
                assertEquals(50, (it.speed as SpeedUi.Value).kmh)
                assertEquals(MotorStatus.ON, it.motorStatus)
                assertEquals(KillSwitchStatus.INACTIVE, it.killSwitch)
                assertEquals(SignalHealth.LIVE, it.signalHealth)
            }

            repository.send(0f, motorOn = false, killSwitchActive = true)
            awaitItem().let {
                assertEquals(RideState.MOTOR_DISABLED, it.rideState)
                assertEquals(KillSwitchStatus.ACTIVE, it.killSwitch)
            }
        }
    }

    @Test
    fun `invalid speed is shown as invalid, and a silent feed degrades the ui`() = runTest {
        viewModel().uiState.test {
            skipItems(1)

            repository.send(-5f, motorOn = true, killSwitchActive = false)
            assertEquals(SpeedUi.Invalid, awaitItem().speed)

            val stale = awaitItem()
            assertEquals(SignalHealth.STALE, stale.signalHealth)
            assertTrue(stale.isDegraded)
        }
    }

    @Test
    fun `gauge fraction is clamped to the gauge range`() = runTest {
        viewModel().uiState.test {
            skipItems(1)
            repository.send(150f, motorOn = true, killSwitchActive = false)
            assertEquals(1f, (awaitItem().speed as SpeedUi.Value).gaugeFraction)
        }
    }

    @Test
    fun `toggling fault injection is reflected in ui state`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onFaultInjectionToggled(true)
            assertTrue(awaitItem().faultInjectionEnabled)
        }
    }
}
