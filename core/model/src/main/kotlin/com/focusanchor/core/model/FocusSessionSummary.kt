package com.focusanchor.core.model

data class FocusSessionSummary(
    val title: String,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val interruptionCount: Int,
    val suspendCount: Int,
    val tone: String,
)

