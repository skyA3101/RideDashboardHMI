package com.hmi.ridedashboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmi.ridedashboard.domain.usecase.ObserveFaultInjectionUseCase
import com.hmi.ridedashboard.domain.usecase.ObserveVehicleStateUseCase
import com.hmi.ridedashboard.domain.usecase.SetFaultInjectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeVehicleState: ObserveVehicleStateUseCase,
    observeFaultInjection: ObserveFaultInjectionUseCase,
    private val setFaultInjection: SetFaultInjectionUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(observeVehicleState(), observeFaultInjection()) { vehicle, faults ->
            vehicle.toUiState(faultInjectionEnabled = faults)
        }.stateIn(
            scope = viewModelScope,
            // Keeps the feed alive across configuration changes, stops it in the background.
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardUiState(),
        )

    fun onFaultInjectionToggled(enabled: Boolean) = setFaultInjection(enabled)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
