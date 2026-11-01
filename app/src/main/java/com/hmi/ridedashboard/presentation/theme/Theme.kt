package com.hmi.ridedashboard.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HmiColorScheme = darkColorScheme(
    primary = HmiColors.Accent,
    background = HmiColors.Background,
    surface = HmiColors.Surface,
    onBackground = HmiColors.OnSurface,
    onSurface = HmiColors.OnSurface,
    error = HmiColors.Danger,
)

/** Dashboards are always dark to avoid glare while riding. */
@Composable
fun RideDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HmiColorScheme, content = content)
}
