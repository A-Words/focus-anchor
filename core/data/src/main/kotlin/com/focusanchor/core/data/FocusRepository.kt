package com.focusanchor.core.data

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.SuspendAnchor

interface FocusRepository {
    fun currentSession(): FocusSession?
    fun recentSummaries(): List<FocusSessionSummary>
    fun suspendedAnchors(): List<SuspendAnchor>
}

