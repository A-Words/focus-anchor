package com.focusanchor.core.model

data class FocusSession(
    val title: String,
    val durationMinutes: Int,
    val mode: FocusMode,
    val startedAtEpochMillis: Long,
    val status: FocusSessionStatus = FocusSessionStatus.Running,
    val pausedAtEpochMillis: Long? = null,
    val accumulatedPausedMillis: Long = 0L,
    val interruptionCount: Int = 0,
)
