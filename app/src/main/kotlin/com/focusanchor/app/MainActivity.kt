package com.focusanchor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.focusanchor.app.navigation.FocusAnchorApp
import com.focusanchor.core.designsystem.theme.FocusAnchorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusAnchorTheme {
                FocusAnchorApp()
            }
        }
    }
}

