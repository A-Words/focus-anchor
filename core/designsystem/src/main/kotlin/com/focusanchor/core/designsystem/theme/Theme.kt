package com.focusanchor.core.designsystem.theme

import android.os.Build
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusAnchorTypography,
        content = content,
    )
}

@Preview(name = "FocusAnchorTheme Light")
@Composable
private fun FocusAnchorThemeLightPreview() {
    FocusAnchorTheme(
        darkTheme = false,
        dynamicColor = false,
    ) {
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
    FocusAnchorTheme(
        darkTheme = true,
        dynamicColor = false,
    ) {
        Surface {
            Text(
                text = "专注主题预览",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
