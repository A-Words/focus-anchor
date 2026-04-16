package com.focusanchor.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FocusAnchorPrimary,
    onPrimary = FocusAnchorOnPrimary,
    secondary = FocusAnchorSecondary,
    background = FocusAnchorBackground,
    surface = FocusAnchorSurface,
    onSurface = FocusAnchorOnSurface,
)

@Composable
fun FocusAnchorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = FocusAnchorTypography,
        content = content,
    )
}

