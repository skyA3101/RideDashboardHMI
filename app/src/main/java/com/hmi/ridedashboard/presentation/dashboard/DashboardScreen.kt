package com.hmi.ridedashboard.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmi.ridedashboard.R
import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.domain.model.RideState
import com.hmi.ridedashboard.domain.model.SignalHealth
import com.hmi.ridedashboard.presentation.dashboard.components.KillSwitchCard
import com.hmi.ridedashboard.presentation.dashboard.components.MotorStatusCard
import com.hmi.ridedashboard.presentation.dashboard.components.SignalBanner
import com.hmi.ridedashboard.presentation.dashboard.components.Speedometer
import com.hmi.ridedashboard.presentation.dashboard.components.TopBar
import com.hmi.ridedashboard.presentation.theme.HmiColors
import com.hmi.ridedashboard.presentation.theme.RideDashboardTheme

@Composable
fun DashboardRoute(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state = state, onFaultInjectionToggled = viewModel::onFaultInjectionToggled)
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onFaultInjectionToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentAlpha by animateFloatAsState(if (state.isDegraded) 0.45f else 1f, label = "degradedAlpha")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HmiColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        TopBar(rideState = state.rideState, signalHealth = state.signalHealth)

        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            SignalBanner(state.signalHealth)
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MotorStatusCard(state.motorStatus, Modifier.weight(1f))
            Speedometer(state.speed, Modifier.weight(1.6f).fillMaxHeight())
            KillSwitchCard(state.killSwitch, Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.fault_injection), color = HmiColors.Muted, fontSize = 13.sp)
            Switch(
                checked = state.faultInjectionEnabled,
                onCheckedChange = onFaultInjectionToggled,
                colors = SwitchDefaults.colors(checkedTrackColor = HmiColors.Warning),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
private fun DashboardRidingPreview() {
    RideDashboardTheme {
        DashboardScreen(
            state = DashboardUiState(
                speed = SpeedUi.Value(50, 50 / GAUGE_MAX_KMH),
                motorStatus = MotorStatus.ON,
                killSwitch = KillSwitchStatus.INACTIVE,
                rideState = RideState.ACCELERATING,
                signalHealth = SignalHealth.LIVE,
            ),
            onFaultInjectionToggled = {},
        )
    }
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
private fun DashboardKillSwitchPreview() {
    RideDashboardTheme {
        DashboardScreen(
            state = DashboardUiState(
                speed = SpeedUi.Value(0, 0f),
                motorStatus = MotorStatus.OFF,
                killSwitch = KillSwitchStatus.ACTIVE,
                rideState = RideState.MOTOR_DISABLED,
                signalHealth = SignalHealth.STALE,
            ),
            onFaultInjectionToggled = {},
        )
    }
}
