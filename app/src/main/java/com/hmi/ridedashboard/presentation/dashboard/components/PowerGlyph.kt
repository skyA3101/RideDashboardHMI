package com.hmi.ridedashboard.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/** IEC 5009 power symbol; drawn because it isn't in material-icons-core. */
@Composable
fun PowerGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = size.minDimension * 0.11f
        val inset = stroke * 1.2f
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawLine(color, Offset(size.width / 2, stroke / 2), Offset(size.width / 2, size.height / 2), stroke, StrokeCap.Round)
    }
}
