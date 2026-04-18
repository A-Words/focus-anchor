package com.focusanchor.app.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.focusanchor.app.FocusAnchorApplication
import com.focusanchor.core.designsystem.theme.FocusAnchorTheme
import com.focusanchor.feature.focus.FocusQuickSuspendScreen

class QuickSuspendActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val focusRepository = (application as FocusAnchorApplication).focusRepository
        if (focusRepository.currentSessionFlow.value == null) {
            finish()
            return
        }

        setFinishOnTouchOutside(true)
        setContent {
            FocusAnchorTheme {
                FocusQuickSuspendScreen(
                    onDismiss = ::finish,
                    onSubmit = { type, keyword ->
                        focusRepository.addSuspendAnchor(
                            type = type,
                            keyword = keyword,
                            createdAtEpochMillis = System.currentTimeMillis(),
                        )
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, QuickSuspendActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
