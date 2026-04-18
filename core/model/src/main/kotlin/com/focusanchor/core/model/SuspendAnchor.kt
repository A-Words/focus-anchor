package com.focusanchor.core.model

data class SuspendAnchor(
    val id: String,
    val type: SuspendItemType,
    val keyword: String?,
    val createdAtEpochMillis: Long,
    val sessionStartedAtEpochMillis: Long,
)
