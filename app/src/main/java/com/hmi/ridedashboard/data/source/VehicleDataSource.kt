package com.hmi.ridedashboard.data.source

import kotlinx.coroutines.flow.Flow

/** Abstraction over where frames come from: the simulator today, a CAN/VHAL bridge later. */
interface VehicleDataSource {
    fun vehicleSignals(): Flow<VehicleSignalDto>
}
