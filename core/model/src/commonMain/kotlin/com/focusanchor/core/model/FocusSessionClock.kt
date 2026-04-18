package com.focusanchor.core.model

data class FocusSessionClockState(
    val remainingSeconds: Long,
    val isCompleted: Boolean,
)

fun FocusSession.clockState(nowMillis: Long): FocusSessionClockState {
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

    return FocusSessionClockState(
        remainingSeconds = remainingSeconds,
        isCompleted = remainingSeconds == 0L,
    )
}
