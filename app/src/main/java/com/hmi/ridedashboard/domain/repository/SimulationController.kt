package com.hmi.ridedashboard.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Controls for the demo simulator; would not exist with a real vehicle bus. */
interface SimulationController {
    val faultInjectionEnabled: StateFlow<Boolean>

    fun setFaultInjectionEnabled(enabled: Boolean)
}
