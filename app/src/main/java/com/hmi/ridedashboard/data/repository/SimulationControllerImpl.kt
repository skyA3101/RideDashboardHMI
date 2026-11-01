package com.hmi.ridedashboard.data.repository

import com.hmi.ridedashboard.data.simulator.SimulatedVehicleDataSource
import com.hmi.ridedashboard.domain.repository.SimulationController
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SimulationControllerImpl @Inject constructor(
    private val simulator: SimulatedVehicleDataSource,
) : SimulationController {

    override val faultInjectionEnabled: StateFlow<Boolean> = simulator.faultInjectionEnabled

    override fun setFaultInjectionEnabled(enabled: Boolean) = simulator.setFaultInjectionEnabled(enabled)
}
