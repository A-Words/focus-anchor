package com.focusanchor.core.model

data class FocusSession(
    val title: String,
    val durationMinutes: Int,
    val mode: FocusMode,
    val interruptionCount: Int = 0,
)

