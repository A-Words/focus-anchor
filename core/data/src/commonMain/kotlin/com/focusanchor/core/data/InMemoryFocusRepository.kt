package com.focusanchor.core.data

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.FocusSessionStatus
import com.focusanchor.core.model.SuspendAnchor
import com.focusanchor.core.model.SuspendItemType
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryFocusRepository : FocusRepository {
    private val currentSession = MutableStateFlow<FocusSession?>(null)
    private val recentSummaries = MutableStateFlow(
        listOf(
            FocusSessionSummary(
                title = "高数刷题",
                plannedMinutes = 40,
                actualMinutes = 38,
                endedEarly = false,
                interruptionCount = 1,
                suspendCount = 2,
                tone = "本次专注较稳定",
            ),
        ),
    )
    private val suspendedAnchors = MutableStateFlow(
        listOf(
            SuspendAnchor(
                id = "sample-message",
                type = SuspendItemType.Message,
                keyword = "导师",
                createdAtEpochMillis = 0L,
                sessionStartedAtEpochMillis = 0L,
            ),
            SuspendAnchor(
                id = "sample-research",
                type = SuspendItemType.Research,
                keyword = "六级报名",
                createdAtEpochMillis = 1L,
                sessionStartedAtEpochMillis = 0L,
            ),
        ),
    )

    override val currentSessionFlow: StateFlow<FocusSession?> = currentSession.asStateFlow()
    override val recentSummariesFlow: StateFlow<List<FocusSessionSummary>> = recentSummaries.asStateFlow()
    override val suspendedAnchorsFlow: StateFlow<List<SuspendAnchor>> = suspendedAnchors.asStateFlow()

    override fun startSession(session: FocusSession) {
        currentSession.value = session
    }

    override fun pauseCurrentSession(pausedAtEpochMillis: Long) {
        val session = currentSession.value ?: return
        if (session.status != FocusSessionStatus.Running) return

        currentSession.value = session.copy(
            status = FocusSessionStatus.Paused,
            pausedAtEpochMillis = pausedAtEpochMillis,
            interruptionCount = session.interruptionCount + 1,
        )
    }

    override fun resumeCurrentSession(resumedAtEpochMillis: Long) {
        val session = currentSession.value ?: return
        val pausedAtEpochMillis = session.pausedAtEpochMillis ?: return
        if (session.status != FocusSessionStatus.Paused) return

        currentSession.value = session.copy(
            status = FocusSessionStatus.Running,
            pausedAtEpochMillis = null,
            accumulatedPausedMillis = session.accumulatedPausedMillis +
                (resumedAtEpochMillis - pausedAtEpochMillis).coerceAtLeast(0L),
        )
    }

    override fun finishCurrentSession(
        finishedAtEpochMillis: Long,
        endedEarly: Boolean,
    ): FocusSessionSummary? {
        val session = currentSession.value ?: return null
        val suspendCount = suspendedAnchors.value.count {
            it.sessionStartedAtEpochMillis == session.startedAtEpochMillis
        }
        val summary = FocusSessionSummary(
            title = session.title,
            plannedMinutes = session.durationMinutes,
            actualMinutes = calculateActualMinutes(session, finishedAtEpochMillis),
            endedEarly = endedEarly,
            interruptionCount = session.interruptionCount,
            suspendCount = suspendCount,
            tone = summaryTone(
                endedEarly = endedEarly,
                interruptionCount = session.interruptionCount,
            ),
        )

        recentSummaries.value = listOf(summary) + recentSummaries.value
        currentSession.value = null
        return summary
    }

    override fun addSuspendAnchor(
        type: SuspendItemType,
        keyword: String?,
        createdAtEpochMillis: Long,
    ): SuspendAnchor? {
        val session = currentSession.value ?: return null
        val anchor = SuspendAnchor(
            id = UUID.randomUUID().toString(),
            type = type,
            keyword = keyword?.takeIf { it.isNotBlank() },
            createdAtEpochMillis = createdAtEpochMillis,
            sessionStartedAtEpochMillis = session.startedAtEpochMillis,
        )
        suspendedAnchors.value = listOf(anchor) + suspendedAnchors.value
        return anchor
    }

    private fun calculateActualMinutes(
        session: FocusSession,
        finishedAtEpochMillis: Long,
    ): Int {
        val pausedAtEpochMillis = session.pausedAtEpochMillis
        val currentPauseMillis = if (
            session.status == FocusSessionStatus.Paused &&
            pausedAtEpochMillis != null
        ) {
            (finishedAtEpochMillis - pausedAtEpochMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        val activeMillis = (
            finishedAtEpochMillis -
                session.startedAtEpochMillis -
                session.accumulatedPausedMillis -
                currentPauseMillis
            ).coerceAtLeast(0L)

        return (activeMillis / 60_000L).toInt()
    }

    private fun summaryTone(
        endedEarly: Boolean,
        interruptionCount: Int,
    ): String = when {
        endedEarly -> "本次专注已提前结束"
        interruptionCount == 0 -> "本次专注较稳定"
        interruptionCount in 1..2 -> "中途有打断，但已回到任务"
        else -> "中断较多，下轮可缩短时长"
    }
}
