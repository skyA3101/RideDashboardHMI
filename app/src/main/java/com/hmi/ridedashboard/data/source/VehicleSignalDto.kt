package com.hmi.ridedashboard.data.source

/** Wire-level frame as produced by a vehicle data source. Nullable fields model missing signals. */
data class VehicleSignalDto(
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killSwitchActive: Boolean?,
)
