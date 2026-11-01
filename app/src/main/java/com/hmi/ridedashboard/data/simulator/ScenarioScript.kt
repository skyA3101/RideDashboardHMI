package com.hmi.ridedashboard.data.simulator

import com.hmi.ridedashboard.data.source.VehicleSignalDto

sealed interface ScriptStep {
    val label: String

    /** Emit [frame] once per tick for [ticks] ticks. */
    data class Emit(override val label: String, val frame: VehicleSignalDto, val ticks: Int = 1) : ScriptStep

    /** Emit nothing for [ticks] ticks, simulating a bus dropout. */
    data class Dropout(override val label: String, val ticks: Int) : ScriptStep
}

object ScenarioScript {

    /** Parked -> Ready -> Riding -> Accelerating -> Motor disabled. */
    val nominal: List<ScriptStep> = listOf(
        ScriptStep.Emit("Parked", frame(0f, motorOn = false, killSwitch = false), ticks = 3),
        ScriptStep.Emit("Ready", frame(0f, motorOn = true, killSwitch = false), ticks = 3),
        ScriptStep.Emit("Riding", frame(25f, motorOn = true, killSwitch = false), ticks = 4),
        ScriptStep.Emit("Accelerating", frame(35f, motorOn = true, killSwitch = false)),
        ScriptStep.Emit("Accelerating", frame(45f, motorOn = true, killSwitch = false)),
        ScriptStep.Emit("Accelerating", frame(50f, motorOn = true, killSwitch = false)),
        // Kill switch cuts the motor; the vehicle coasts down rather than stopping instantly.
        ScriptStep.Emit("Motor disabled", frame(30f, motorOn = false, killSwitch = true)),
        ScriptStep.Emit("Motor disabled", frame(10f, motorOn = false, killSwitch = true)),
        ScriptStep.Emit("Motor disabled", frame(0f, motorOn = false, killSwitch = true), ticks = 3),
    )

    /** Appended after [nominal] when fault injection is enabled. */
    val faults: List<ScriptStep> = listOf(
        ScriptStep.Emit("Ready", frame(0f, motorOn = true, killSwitch = false), ticks = 2),
        ScriptStep.Emit("Negative speed", frame(-12f, motorOn = true, killSwitch = false), ticks = 2),
        ScriptStep.Emit("NaN speed", frame(Float.NaN, motorOn = true, killSwitch = false)),
        ScriptStep.Emit("Out-of-range speed", frame(999f, motorOn = true, killSwitch = false)),
        ScriptStep.Emit("Missing signals", VehicleSignalDto(null, null, null), ticks = 2),
        ScriptStep.Emit("Riding", frame(25f, motorOn = true, killSwitch = false), ticks = 2),
        ScriptStep.Dropout("Signal dropout", ticks = 4),
    )

    private fun frame(speedKmh: Float, motorOn: Boolean, killSwitch: Boolean) =
        VehicleSignalDto(speedKmh = speedKmh, motorOn = motorOn, killSwitchActive = killSwitch)
}
