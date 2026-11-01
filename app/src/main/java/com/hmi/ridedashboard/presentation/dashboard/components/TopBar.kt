package com.hmi.ridedashboard.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmi.ridedashboard.R
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.presentation.theme.HmiColors
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ClockFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun TopBar(rideState: RideState, signalHealth: SignalHealth, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.app_title).uppercase(),
            color = HmiColors.Muted,
            fontSize = 13.sp,
            letterSpacing = 3.sp,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        RideStatePill(rideState, Modifier.align(Alignment.Center))
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SignalIndicator(signalHealth)
            Clock()
        }
    }
}

@Composable
private fun RideStatePill(rideState: RideState, modifier: Modifier = Modifier) {
    val (label, color) = when (rideState) {
        RideState.PARKED -> stringResource(R.string.ride_parked) to HmiColors.Muted
        RideState.READY -> stringResource(R.string.ride_ready) to HmiColors.Ok
        RideState.RIDING -> stringResource(R.string.ride_riding) to HmiColors.Accent
        RideState.ACCELERATING -> stringResource(R.string.ride_accelerating) to HmiColors.HighSpeed
        RideState.COASTING -> stringResource(R.string.ride_coasting) to HmiColors.Accent
        RideState.MOTOR_DISABLED -> stringResource(R.string.ride_motor_disabled) to HmiColors.Danger
        RideState.UNKNOWN -> stringResource(R.string.ride_unknown) to HmiColors.Warning
    }
    Text(
        label.uppercase(),
        color = color,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 6.dp),
    )
}

@Composable
private fun SignalIndicator(health: SignalHealth) {
    val (label, color) = health.labelAndColor()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, color = color, fontSize = 13.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun Clock() {
    val time by produceState(LocalTime.now().format(ClockFormat)) {
        while (true) {
            delay(1_000)
            value = LocalTime.now().format(ClockFormat)
        }
    }
    Text(time, color = HmiColors.OnSurface, fontSize = 15.sp)
}

/** Shown only while the feed is not live, so the rider knows the values may be outdated. */
@Composable
fun SignalBanner(health: SignalHealth, modifier: Modifier = Modifier) {
    val message = when (health) {
        SignalHealth.LIVE -> return
        SignalHealth.AWAITING -> stringResource(R.string.banner_awaiting)
        SignalHealth.STALE -> stringResource(R.string.banner_stale)
        SignalHealth.LOST -> stringResource(R.string.banner_lost)
    }
    val color = health.labelAndColor().second
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(message, color = color, fontSize = 14.sp)
    }
}

@Composable
private fun SignalHealth.labelAndColor(): Pair<String, Color> = when (this) {
    SignalHealth.AWAITING -> stringResource(R.string.signal_awaiting) to HmiColors.Muted
    SignalHealth.LIVE -> stringResource(R.string.signal_live) to HmiColors.Ok
    SignalHealth.STALE -> stringResource(R.string.signal_stale) to HmiColors.Warning
    SignalHealth.LOST -> stringResource(R.string.signal_lost) to HmiColors.Danger
}
