package com.focusanchor.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = FocusAnchorPrimary,
    onPrimary = FocusAnchorOnPrimary,
    secondary = FocusAnchorSecondary,
    background = FocusAnchorBackground,
    onBackground = FocusAnchorOnBackground,
    surface = FocusAnchorSurface,
    onSurface = FocusAnchorOnSurface,
    surfaceVariant = FocusAnchorSurfaceVariant,
    onSurfaceVariant = FocusAnchorOnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = FocusAnchorDarkPrimary,
    onPrimary = FocusAnchorDarkOnPrimary,
    secondary = FocusAnchorDarkSecondary,
    background = FocusAnchorDarkBackground,
    onBackground = FocusAnchorDarkOnBackground,
    surface = FocusAnchorDarkSurface,
    onSurface = FocusAnchorDarkOnSurface,
    surfaceVariant = FocusAnchorDarkSurfaceVariant,
    onSurfaceVariant = FocusAnchorDarkOnSurfaceVariant,
)

@Composable
fun FocusAnchorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FocusAnchorTypography,
        content = content,
    )
}

@Preview(name = "FocusAnchorTheme Light")
@Composable
private fun FocusAnchorThemeLightPreview() {
    FocusAnchorTheme(darkTheme = false) {
        Surface {
            Text(
                text = "专注主题预览",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "FocusAnchorTheme Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FocusAnchorThemeDarkPreview() {
    FocusAnchorTheme(darkTheme = true) {
        Surface {
            Text(
                text = "专注主题预览",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
