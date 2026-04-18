package com.focusanchor.core.data

import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionStatus
import com.focusanchor.core.model.SuspendItemType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InMemoryFocusRepositoryTest {
    private val repository = InMemoryFocusRepository()

    @Test
    fun startSession_updatesObservableCurrentSessionImmediately() {
        val session = sampleSession()

        repository.startSession(session)

        assertEquals(session, repository.currentSessionFlow.value)
    }

    @Test
    fun pauseAndResumeSession_updatesPauseStateAndAccumulatedMillis() {
        repository.startSession(sampleSession())

        repository.pauseCurrentSession(pausedAtEpochMillis = 70_000L)
        val pausedSession = repository.currentSessionFlow.value
        assertNotNull(pausedSession)
        assertEquals(FocusSessionStatus.Paused, pausedSession.status)
        assertEquals(70_000L, pausedSession.pausedAtEpochMillis)

        repository.resumeCurrentSession(resumedAtEpochMillis = 90_000L)
        val resumedSession = repository.currentSessionFlow.value
        assertNotNull(resumedSession)
        assertEquals(FocusSessionStatus.Running, resumedSession.status)
        assertEquals(null, resumedSession.pausedAtEpochMillis)
        assertEquals(20_000L, resumedSession.accumulatedPausedMillis)
    }

    @Test
    fun addSuspendAnchor_supportsTypeOnlyAndKeywordVariants() {
        repository.startSession(sampleSession())

        val typeOnly = repository.addSuspendAnchor(
            type = SuspendItemType.Idea,
            keyword = null,
            createdAtEpochMillis = 20_000L,
        )
        val withKeyword = repository.addSuspendAnchor(
            type = SuspendItemType.Todo,
            keyword = "实验报告",
            createdAtEpochMillis = 21_000L,
        )

        assertNotNull(typeOnly)
        assertNotNull(withKeyword)
        assertEquals(null, typeOnly.keyword)
        assertEquals("实验报告", withKeyword.keyword)
        assertEquals(10_000L, typeOnly.sessionStartedAtEpochMillis)
        assertEquals(10_000L, withKeyword.sessionStartedAtEpochMillis)
        assertEquals(2, repository.suspendedAnchorsFlow.value.count { it.sessionStartedAtEpochMillis == 10_000L })
    }

    @Test
    fun finishCurrentSession_usesRealSuspendCountInSummary() {
        repository.startSession(sampleSession())
        repository.addSuspendAnchor(
            type = SuspendItemType.Message,
            keyword = "导师",
            createdAtEpochMillis = 20_000L,
        )
        repository.addSuspendAnchor(
            type = SuspendItemType.Research,
            keyword = null,
            createdAtEpochMillis = 21_000L,
        )

        val summary = repository.finishCurrentSession(
            finishedAtEpochMillis = 1_510_000L,
            endedEarly = false,
        )

        assertNotNull(summary)
        assertEquals(2, summary.suspendCount)
        assertNull(repository.currentSessionFlow.value)
        assertEquals(summary, repository.recentSummariesFlow.value.first())
    }

    private fun sampleSession() = FocusSession(
        title = "背单词",
        durationMinutes = 25,
        mode = FocusMode.Study,
        startedAtEpochMillis = 10_000L,
    )
}
