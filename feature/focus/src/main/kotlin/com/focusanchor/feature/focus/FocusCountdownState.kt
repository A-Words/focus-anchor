package com.focusanchor.feature.focus

import com.focusanchor.core.model.FocusSessionStatus

internal data class FocusCountdownState(
    val remainingSeconds: Long,
    val isCompleted: Boolean,
)

internal fun calculateFocusCountdownState(
    startedAtEpochMillis: Long,
    durationMinutes: Int,
    accumulatedPausedMillis: Long,
    pausedAtEpochMillis: Long?,
    status: FocusSessionStatus,
    nowMillis: Long,
): FocusCountdownState {
    val totalDurationSeconds = durationMinutes * 60L
    val activeElapsedMillis = when (status) {
        FocusSessionStatus.Running -> nowMillis - startedAtEpochMillis - accumulatedPausedMillis
        FocusSessionStatus.Paused -> (
            (pausedAtEpochMillis ?: nowMillis) -
                startedAtEpochMillis -
                accumulatedPausedMillis
            )
    }.coerceAtLeast(0L)
    val elapsedSeconds = activeElapsedMillis / 1000L
    val remainingSeconds = (totalDurationSeconds - elapsedSeconds).coerceAtLeast(0L)

    return FocusCountdownState(
        remainingSeconds = remainingSeconds,
        isCompleted = remainingSeconds == 0L,
    )
}
