package com.hmi.ridedashboard.presentation.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmi.ridedashboard.R
import com.hmi.ridedashboard.domain.model.KillSwitchStatus
import com.hmi.ridedashboard.domain.model.MotorStatus
import com.hmi.ridedashboard.presentation.theme.HmiColors

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun MotorStatusCard(status: MotorStatus, modifier: Modifier = Modifier) {
    val (label, tint) = when (status) {
        MotorStatus.ON -> stringResource(R.string.motor_on) to HmiColors.Ok
        MotorStatus.OFF -> stringResource(R.string.motor_off) to HmiColors.Muted
        MotorStatus.UNKNOWN -> stringResource(R.string.value_unavailable) to HmiColors.Warning
    }
    val color by animateColorAsState(tint, label = "motorTint")
    val description = stringResource(R.string.cd_motor, label)

    StatusCard(
        title = stringResource(R.string.motor_title),
        modifier = modifier.semantics { contentDescription = description },
        background = HmiColors.Surface,
        border = BorderStroke(2.dp, color.copy(alpha = 0.6f)),
    ) {
        PowerGlyph(color = color, modifier = Modifier.size(44.dp))
        Text(label, color = color, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Kill switch uses shape, colour, icon and motion so the two states can't be confused:
 * ACTIVE is a pulsing solid red card with a warning icon; INACTIVE is a calm outlined card.
 */
@Composable
fun KillSwitchCard(status: KillSwitchStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        KillSwitchStatus.ACTIVE -> stringResource(R.string.kill_active)
        KillSwitchStatus.INACTIVE -> stringResource(R.string.kill_inactive)
        KillSwitchStatus.UNKNOWN -> stringResource(R.string.value_unavailable)
    }
    val description = stringResource(R.string.cd_kill_switch, label)
    val cardModifier = modifier.semantics { contentDescription = description }
    val title = stringResource(R.string.kill_title)

    when (status) {
        KillSwitchStatus.ACTIVE -> {
            val pulse by rememberInfiniteTransition(label = "killPulse").animateFloat(
                initialValue = 1f,
                targetValue = 0.55f,
                animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
                label = "killPulseAlpha",
            )
            StatusCard(title, cardModifier, background = HmiColors.Danger.copy(alpha = pulse), titleColor = Color.White) {
                StatusIcon(Icons.Filled.Warning, Color.White)
                Text(label, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
        }
        KillSwitchStatus.INACTIVE -> StatusCard(
            title, cardModifier, background = HmiColors.Surface, border = BorderStroke(2.dp, HmiColors.Ok.copy(alpha = 0.6f)),
        ) {
            StatusIcon(Icons.Filled.CheckCircle, HmiColors.Ok)
            Text(label, color = HmiColors.Ok, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        KillSwitchStatus.UNKNOWN -> StatusCard(
            title, cardModifier, background = HmiColors.Surface, border = BorderStroke(2.dp, HmiColors.Warning.copy(alpha = 0.6f)),
        ) {
            StatusIcon(Icons.Filled.Info, HmiColors.Warning)
            Text(label, color = HmiColors.Warning, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, tint: Color) {
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(44.dp))
}

@Composable
private fun StatusCard(
    title: String,
    modifier: Modifier = Modifier,
    background: Color,
    border: BorderStroke? = null,
    titleColor: Color = HmiColors.Muted,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background, CardShape)
            .then(if (border != null) Modifier.border(border, CardShape) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(title.uppercase(), color = titleColor, fontSize = 13.sp, letterSpacing = 2.sp, modifier = Modifier.alpha(0.9f))
        content()
    }
}
