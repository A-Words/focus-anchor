package com.focusanchor.core.data

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.SuspendAnchor

interface FocusRepository {
    fun currentSession(): FocusSession?
    fun startSession(session: FocusSession)
    fun pauseCurrentSession(pausedAtEpochMillis: Long)
    fun resumeCurrentSession(resumedAtEpochMillis: Long)
    fun finishCurrentSession(finishedAtEpochMillis: Long, endedEarly: Boolean): FocusSessionSummary?
    fun recentSummaries(): List<FocusSessionSummary>
    fun suspendedAnchors(): List<SuspendAnchor>
}
