package com.hmi.ridedashboard.presentation.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmi.ridedashboard.R
import com.hmi.ridedashboard.presentation.dashboard.GAUGE_MAX_KMH
import com.hmi.ridedashboard.presentation.dashboard.SpeedUi
import com.hmi.ridedashboard.presentation.theme.HmiColors
import kotlin.math.cos
import kotlin.math.sin

private const val START_ANGLE = 150f
private const val SWEEP_ANGLE = 240f
private const val HIGH_SPEED_KMH = 80
private const val TICK_STEP_KMH = 20

@Composable
fun Speedometer(speed: SpeedUi, modifier: Modifier = Modifier) {
    val targetKmh = (speed as? SpeedUi.Value)?.kmh ?: 0
    val targetFraction = (speed as? SpeedUi.Value)?.gaugeFraction ?: 0f
    val animatedKmh by animateIntAsState(targetKmh, tween(800), label = "speed")
    val animatedFraction by animateFloatAsState(targetFraction, tween(800), label = "gauge")
    val arcColor = if (targetKmh > HIGH_SPEED_KMH) HmiColors.HighSpeed else HmiColors.Accent

    val description = when (speed) {
        is SpeedUi.Value -> stringResource(R.string.cd_speed, speed.kmh)
        SpeedUi.Invalid -> stringResource(R.string.speed_invalid)
        SpeedUi.Unavailable -> stringResource(R.string.speed_unavailable)
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1.25f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        val numberSize = (maxHeight.value * 0.36f).sp

        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.045f
            val diameter = size.minDimension - stroke * 2
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2 + stroke)
            val arcSize = Size(diameter, diameter)

            drawArc(HmiColors.Outline, START_ANGLE, SWEEP_ANGLE, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            if (animatedFraction > 0f) {
                drawArc(arcColor, START_ANGLE, SWEEP_ANGLE * animatedFraction, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }

            val center = Offset(topLeft.x + diameter / 2, topLeft.y + diameter / 2)
            val outer = diameter / 2 - stroke * 1.4f
            val inner = outer - stroke * 0.9f
            for (kmh in 0..GAUGE_MAX_KMH.toInt() step TICK_STEP_KMH) {
                val angle = Math.toRadians((START_ANGLE + SWEEP_ANGLE * kmh / GAUGE_MAX_KMH).toDouble())
                val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                drawLine(HmiColors.Muted, center + dir * inner, center + dir * outer, strokeWidth = stroke * 0.25f)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
            when (speed) {
                is SpeedUi.Value -> {
                    Text(animatedKmh.toString(), color = HmiColors.OnSurface, fontSize = numberSize, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.unit_kmh), color = HmiColors.Muted, fontSize = 16.sp, letterSpacing = 2.sp)
                }
                SpeedUi.Invalid -> PlaceholderSpeed(numberSize, HmiColors.Warning, stringResource(R.string.speed_invalid))
                SpeedUi.Unavailable -> PlaceholderSpeed(numberSize, HmiColors.Muted, stringResource(R.string.speed_unavailable))
            }
        }
    }
}

@Composable
private fun PlaceholderSpeed(numberSize: androidx.compose.ui.unit.TextUnit, color: androidx.compose.ui.graphics.Color, caption: String) {
    Text("--", color = color, fontSize = numberSize, fontWeight = FontWeight.SemiBold)
    Text(caption.uppercase(), color = color, fontSize = 14.sp, letterSpacing = 1.sp)
}
