package com.hmi.ridedashboard.testutil

import com.hmi.ridedashboard.domain.model.VehicleSignal
import com.hmi.ridedashboard.domain.repository.SimulationController
import com.hmi.ridedashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/** Frames are buffered until collected, so tests can push without racing the subscriber. */
class FakeVehicleRepository : VehicleRepository {
    private val frames = Channel<VehicleSignal>(Channel.UNLIMITED)

    fun send(speedKmh: Float?, motorOn: Boolean?, killSwitchActive: Boolean?) {
        frames.trySend(VehicleSignal(speedKmh, motorOn, killSwitchActive))
    }

    override fun observeVehicleSignals(): Flow<VehicleSignal> = frames.receiveAsFlow()
}

class FakeSimulationController : SimulationController {
    override val faultInjectionEnabled = MutableStateFlow(false)

    override fun setFaultInjectionEnabled(enabled: Boolean) {
        faultInjectionEnabled.value = enabled
    }
}
