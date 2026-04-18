package com.focusanchor.core.data

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.SuspendAnchor
import com.focusanchor.core.model.SuspendItemType
import kotlinx.coroutines.flow.StateFlow

interface FocusRepository {
    val currentSessionFlow: StateFlow<FocusSession?>
    val recentSummariesFlow: StateFlow<List<FocusSessionSummary>>
    val suspendedAnchorsFlow: StateFlow<List<SuspendAnchor>>

    fun startSession(session: FocusSession)
    fun pauseCurrentSession(pausedAtEpochMillis: Long)
    fun resumeCurrentSession(resumedAtEpochMillis: Long)
    fun finishCurrentSession(finishedAtEpochMillis: Long, endedEarly: Boolean): FocusSessionSummary?
    fun addSuspendAnchor(
        type: SuspendItemType,
        keyword: String?,
        createdAtEpochMillis: Long,
    ): SuspendAnchor?
}
