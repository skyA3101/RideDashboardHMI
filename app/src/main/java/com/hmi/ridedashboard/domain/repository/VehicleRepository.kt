package com.hmi.ridedashboard.domain.repository

import com.hmi.ridedashboard.domain.model.VehicleSignal
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /** Cold stream of raw vehicle frames. May throw if the underlying source fails. */
    fun observeVehicleSignals(): Flow<VehicleSignal>
}
