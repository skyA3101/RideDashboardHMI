package com.hmi.ridedashboard.domain

import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SpeedReading
import com.hmi.ridedashboard.domain.usecase.RideStateResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class RideStateResolverTest {

    private val resolver = RideStateResolver()

    private fun resolve(prev: Int?, speed: SpeedReading, motor: MotorStatus, kill: KillSwitchStatus) =
        resolver.resolve(prev?.let { SpeedReading.Valid(it) } ?: SpeedReading.Unavailable, speed, motor, kill)

    @Test
    fun `resolves the five assignment scenarios`() {
        val on = MotorStatus.ON
        val off = MotorStatus.OFF
        val inactive = KillSwitchStatus.INACTIVE
        assertEquals(RideState.PARKED, resolve(0, SpeedReading.Valid(0), off, inactive))
        assertEquals(RideState.READY, resolve(0, SpeedReading.Valid(0), on, inactive))
        assertEquals(RideState.RIDING, resolve(0, SpeedReading.Valid(25), on, inactive))
        assertEquals(RideState.ACCELERATING, resolve(45, SpeedReading.Valid(50), on, inactive))
        assertEquals(RideState.MOTOR_DISABLED, resolve(0, SpeedReading.Valid(0), off, KillSwitchStatus.ACTIVE))
    }

    @Test
    fun `motor off while moving is coasting, not parked`() {
        assertEquals(RideState.COASTING, resolve(30, SpeedReading.Valid(20), MotorStatus.OFF, KillSwitchStatus.INACTIVE))
    }

    @Test
    fun `kill switch wins even when speed is invalid`() {
        assertEquals(RideState.MOTOR_DISABLED, resolve(50, SpeedReading.Invalid, MotorStatus.UNKNOWN, KillSwitchStatus.ACTIVE))
    }

    @Test
    fun `invalid or missing signals give unknown`() {
        assertEquals(RideState.UNKNOWN, resolve(25, SpeedReading.Invalid, MotorStatus.ON, KillSwitchStatus.INACTIVE))
        assertEquals(RideState.UNKNOWN, resolve(25, SpeedReading.Valid(25), MotorStatus.UNKNOWN, KillSwitchStatus.INACTIVE))
        assertEquals(RideState.UNKNOWN, resolve(25, SpeedReading.Valid(25), MotorStatus.ON, KillSwitchStatus.UNKNOWN))
    }
}
