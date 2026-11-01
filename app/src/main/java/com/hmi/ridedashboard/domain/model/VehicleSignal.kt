package com.hmi.ridedashboard.domain.model

/**
 * Raw, unvalidated telemetry as received from the vehicle bus (or simulator).
 * A `null` field means the signal was missing from the frame.
 */
data class VehicleSignal(
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killSwitchActive: Boolean?,
)
