package com.focusanchor.core.data

import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionSummary
import com.focusanchor.core.model.FocusSessionStatus
import com.focusanchor.core.model.SuspendAnchor
import com.focusanchor.core.model.SuspendItemType

class InMemoryFocusRepository : FocusRepository {
    private var currentSession: FocusSession? = null
    private val recentSummaries = mutableListOf(
        FocusSessionSummary(
            title = "高数刷题",
            plannedMinutes = 40,
            actualMinutes = 38,
            endedEarly = false,
            interruptionCount = 1,
            suspendCount = 2,
            tone = "本次专注较稳定",
        ),
    )

    override fun currentSession(): FocusSession? = currentSession

    override fun startSession(session: FocusSession) {
        currentSession = session
    }

    override fun pauseCurrentSession(pausedAtEpochMillis: Long) {
        val session = currentSession ?: return
        if (session.status != FocusSessionStatus.Running) return

        currentSession = session.copy(
            status = FocusSessionStatus.Paused,
            pausedAtEpochMillis = pausedAtEpochMillis,
            interruptionCount = session.interruptionCount + 1,
        )
    }

    override fun resumeCurrentSession(resumedAtEpochMillis: Long) {
        val session = currentSession ?: return
        val pausedAtEpochMillis = session.pausedAtEpochMillis ?: return
        if (session.status != FocusSessionStatus.Paused) return

        currentSession = session.copy(
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
        val session = currentSession ?: return null
        val summary = FocusSessionSummary(
            title = session.title,
            plannedMinutes = session.durationMinutes,
            actualMinutes = calculateActualMinutes(session, finishedAtEpochMillis),
            endedEarly = endedEarly,
            interruptionCount = session.interruptionCount,
            suspendCount = 0,
            tone = summaryTone(
                endedEarly = endedEarly,
                interruptionCount = session.interruptionCount,
            ),
        )

        recentSummaries.add(0, summary)
        currentSession = null
        return summary
    }

    override fun recentSummaries(): List<FocusSessionSummary> = recentSummaries.toList()

    override fun suspendedAnchors(): List<SuspendAnchor> =
        listOf(
            SuspendAnchor(type = SuspendItemType.Message, keyword = "导师"),
            SuspendAnchor(type = SuspendItemType.Research, keyword = "六级报名"),
        )

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
