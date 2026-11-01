package com.hmi.ridedashboard.domain.usecase

import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SpeedReading
import javax.inject.Inject

/**
 * Derives a ride state from the validated signals. The scenario is never sent by the
 * source; it is inferred, so the same logic would work against real CAN data.
 */
class RideStateResolver @Inject constructor() {

    fun resolve(
        previousSpeed: SpeedReading,
        speed: SpeedReading,
        motor: MotorStatus,
        killSwitch: KillSwitchStatus,
    ): RideState {
        if (killSwitch == KillSwitchStatus.ACTIVE) return RideState.MOTOR_DISABLED
        if (speed !is SpeedReading.Valid || killSwitch == KillSwitchStatus.UNKNOWN) return RideState.UNKNOWN

        return when (motor) {
            MotorStatus.UNKNOWN -> RideState.UNKNOWN
            // Motor OFF does not force speed to zero: the vehicle can still be rolling.
            MotorStatus.OFF -> if (speed.kmh == 0) RideState.PARKED else RideState.COASTING
            MotorStatus.ON -> when {
                speed.kmh == 0 -> RideState.READY
                isAccelerating(previousSpeed, speed) -> RideState.ACCELERATING
                else -> RideState.RIDING
            }
        }
    }

    // Pulling away from standstill counts as riding; accelerating means gaining speed while already moving.
    private fun isAccelerating(previous: SpeedReading, current: SpeedReading.Valid): Boolean =
        previous is SpeedReading.Valid &&
            previous.kmh > 0 &&
            current.kmh - previous.kmh >= ACCELERATION_THRESHOLD_KMH

    companion object {
        const val ACCELERATION_THRESHOLD_KMH = 3
    }
}
