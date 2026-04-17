package com.focusanchor.feature.focus

internal data class FocusCountdownState(
    val remainingSeconds: Long,
    val isCompleted: Boolean,
)

internal fun calculateFocusCountdownState(
    startedAtEpochMillis: Long,
    durationMinutes: Int,
    nowMillis: Long,
): FocusCountdownState {
    val totalDurationSeconds = durationMinutes * 60L
    val elapsedSeconds = ((nowMillis - startedAtEpochMillis).coerceAtLeast(0L)) / 1000L
    val remainingSeconds = (totalDurationSeconds - elapsedSeconds).coerceAtLeast(0L)

    return FocusCountdownState(
        remainingSeconds = remainingSeconds,
        isCompleted = remainingSeconds == 0L,
    )
}
