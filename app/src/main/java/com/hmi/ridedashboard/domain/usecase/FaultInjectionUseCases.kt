package com.hmi.ridedashboard.domain.usecase

import com.hmi.ridedashboard.domain.repository.SimulationController
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveFaultInjectionUseCase @Inject constructor(
    private val controller: SimulationController,
) {
    operator fun invoke(): StateFlow<Boolean> = controller.faultInjectionEnabled
}

class SetFaultInjectionUseCase @Inject constructor(
    private val controller: SimulationController,
) {
    operator fun invoke(enabled: Boolean) = controller.setFaultInjectionEnabled(enabled)
}
