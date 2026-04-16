package com.focusanchor.core.data

import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.SuspendAnchor
import com.focusanchor.core.model.SuspendItemType

class InMemoryFocusRepository : FocusRepository {
    override fun currentSession(): FocusSession? =
        FocusSession(
            title = "背单词",
            durationMinutes = 25,
            mode = FocusMode.Study,
            interruptionCount = 1,
        )

    override fun recentSummaries(): List<FocusSessionSummary> =
        listOf(
            FocusSessionSummary(
                title = "高数刷题",
                plannedMinutes = 40,
                actualMinutes = 38,
                interruptionCount = 1,
                suspendCount = 2,
                tone = "整体稳定",
            ),
        )

    override fun suspendedAnchors(): List<SuspendAnchor> =
        listOf(
            SuspendAnchor(type = SuspendItemType.Message, keyword = "导师"),
            SuspendAnchor(type = SuspendItemType.Research, keyword = "六级报名"),
        )
}

