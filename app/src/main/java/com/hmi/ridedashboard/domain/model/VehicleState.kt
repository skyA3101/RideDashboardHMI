package com.hmi.ridedashboard.domain.model

/** Validated vehicle state that the presentation layer renders. */
data class VehicleState(
    val speed: SpeedReading,
    val motor: MotorStatus,
    val killSwitch: KillSwitchStatus,
    val rideState: RideState,
    val health: SignalHealth,
) {
    companion object {
        val Initial = VehicleState(
            speed = SpeedReading.Unavailable,
            motor = MotorStatus.UNKNOWN,
            killSwitch = KillSwitchStatus.UNKNOWN,
            rideState = RideState.UNKNOWN,
            health = SignalHealth.AWAITING,
        )
    }
}

sealed interface SpeedReading {
    data class Valid(val kmh: Int) : SpeedReading

    /** A value was received but is physically implausible (negative, NaN, out of range). */
    data object Invalid : SpeedReading

    /** The speed signal was missing from the frame. */
    data object Unavailable : SpeedReading
}

enum class MotorStatus { ON, OFF, UNKNOWN }

enum class KillSwitchStatus { ACTIVE, INACTIVE, UNKNOWN }

enum class RideState { PARKED, READY, RIDING, ACCELERATING, COASTING, MOTOR_DISABLED, UNKNOWN }

enum class SignalHealth {
    /** No frame received yet. */
    AWAITING,

    /** Frames are arriving within the expected interval. */
    LIVE,

    /** No frame within the stale timeout; values shown are last known. */
    STALE,

    /** The data source failed; reconnecting. */
    LOST,
}
