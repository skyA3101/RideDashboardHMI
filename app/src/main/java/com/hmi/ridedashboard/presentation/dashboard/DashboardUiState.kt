package com.hmi.ridedashboard.presentation.dashboard

import androidx.compose.runtime.Immutable
import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.domain.model.SpeedReading
import com.hmi.ridedashboard.domain.model.VehicleState

@Immutable
data class DashboardUiState(
    val speed: SpeedUi = SpeedUi.Unavailable,
    val motorStatus: MotorStatus = MotorStatus.UNKNOWN,
    val killSwitch: KillSwitchStatus = KillSwitchStatus.UNKNOWN,
    val rideState: RideState = RideState.UNKNOWN,
    val signalHealth: SignalHealth = SignalHealth.AWAITING,
    val faultInjectionEnabled: Boolean = false,
) {
    /** Values on screen are not current; the UI dims them. */
    val isDegraded: Boolean get() = signalHealth != SignalHealth.LIVE
}

sealed interface SpeedUi {
    data class Value(val kmh: Int, val gaugeFraction: Float) : SpeedUi
    data object Invalid : SpeedUi
    data object Unavailable : SpeedUi
}

const val GAUGE_MAX_KMH = 120f

fun VehicleState.toUiState(faultInjectionEnabled: Boolean) = DashboardUiState(
    speed = when (val reading = speed) {
        is SpeedReading.Valid -> SpeedUi.Value(
            kmh = reading.kmh,
            gaugeFraction = (reading.kmh / GAUGE_MAX_KMH).coerceIn(0f, 1f),
        )
        SpeedReading.Invalid -> SpeedUi.Invalid
        SpeedReading.Unavailable -> SpeedUi.Unavailable
    },
    motorStatus = motor,
    killSwitch = killSwitch,
    rideState = rideState,
    signalHealth = health,
    faultInjectionEnabled = faultInjectionEnabled,
)
