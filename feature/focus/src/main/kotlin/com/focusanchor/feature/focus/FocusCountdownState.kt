package com.focusanchor.feature.focus

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.clockState

internal data class FocusCountdownState(
    val remainingSeconds: Long,
    val isCompleted: Boolean,
)

internal fun calculateFocusCountdownState(
    session: FocusSession,
    nowMillis: Long,
): FocusCountdownState = session.clockState(nowMillis).let { state ->
    FocusCountdownState(
        remainingSeconds = state.remainingSeconds,
        isCompleted = state.isCompleted,
    )
}
