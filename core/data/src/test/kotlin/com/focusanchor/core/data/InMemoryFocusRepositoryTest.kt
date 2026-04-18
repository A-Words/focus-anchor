package com.focusanchor.core.data

import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryFocusRepositoryTest {
    @Test
    fun currentSession_isNullByDefault() {
        val repository = InMemoryFocusRepository()

        assertNull(repository.currentSession())
    }

    @Test
    fun startSession_storesCurrentSessionInMemory() {
        val repository = InMemoryFocusRepository()
        val session = FocusSession(
            title = "写作业",
            durationMinutes = 40,
            mode = FocusMode.Study,
            startedAtEpochMillis = 1_000L,
        )

        repository.startSession(session)

        assertEquals(session, repository.currentSession())
    }

    @Test
    fun pauseCurrentSession_marksSessionPausedAndIncrementsInterruptions() {
        val repository = InMemoryFocusRepository()
        repository.startSession(
            FocusSession(
                title = "背单词",
                durationMinutes = 25,
                mode = FocusMode.Study,
                startedAtEpochMillis = 1_000L,
            ),
        )

        repository.pauseCurrentSession(pausedAtEpochMillis = 6_000L)

        val pausedSession = assertNotNull(repository.currentSession())
        assertEquals(FocusSessionStatus.Paused, pausedSession.status)
        assertEquals(6_000L, pausedSession.pausedAtEpochMillis)
        assertEquals(1, pausedSession.interruptionCount)
    }

    @Test
    fun resumeCurrentSession_accumulatesPausedTimeAndReturnsToRunning() {
        val repository = InMemoryFocusRepository()
        repository.startSession(
            FocusSession(
                title = "刷题",
                durationMinutes = 40,
                mode = FocusMode.Study,
                startedAtEpochMillis = 1_000L,
                status = FocusSessionStatus.Paused,
                pausedAtEpochMillis = 6_000L,
                accumulatedPausedMillis = 2_000L,
                interruptionCount = 1,
            ),
        )

        repository.resumeCurrentSession(resumedAtEpochMillis = 10_000L)

        val resumedSession = assertNotNull(repository.currentSession())
        assertEquals(FocusSessionStatus.Running, resumedSession.status)
        assertEquals(null, resumedSession.pausedAtEpochMillis)
        assertEquals(6_000L, resumedSession.accumulatedPausedMillis)
        assertEquals(1, resumedSession.interruptionCount)
    }

    @Test
    fun finishCurrentSession_returnsSummaryClearsSessionAndPrependsHistory() {
        val repository = InMemoryFocusRepository()
        repository.startSession(
            FocusSession(
                title = "写作业",
                durationMinutes = 40,
                mode = FocusMode.Study,
                startedAtEpochMillis = 0L,
            ),
        )

        val summary = repository.finishCurrentSession(
            finishedAtEpochMillis = 2_400_000L,
            endedEarly = false,
        )

        val latestSummary = assertNotNull(summary)
        assertNull(repository.currentSession())
        assertEquals(latestSummary, repository.recentSummaries().first())
        assertEquals("写作业", latestSummary.title)
        assertEquals(40, latestSummary.actualMinutes)
        assertEquals(false, latestSummary.endedEarly)
    }

    @Test
    fun finishCurrentSession_marksEndedEarlyAndExcludesActivePauseWindow() {
        val repository = InMemoryFocusRepository()
        repository.startSession(
            FocusSession(
                title = "看课",
                durationMinutes = 25,
                mode = FocusMode.Study,
                startedAtEpochMillis = 0L,
                status = FocusSessionStatus.Paused,
                pausedAtEpochMillis = 600_000L,
                accumulatedPausedMillis = 120_000L,
                interruptionCount = 2,
            ),
        )

        val summary = repository.finishCurrentSession(
            finishedAtEpochMillis = 900_000L,
            endedEarly = true,
        )

        val latestSummary = assertNotNull(summary)
        assertTrue(latestSummary.endedEarly)
        assertEquals(8, latestSummary.actualMinutes)
        assertEquals("本次专注已提前结束", latestSummary.tone)
    }
}
