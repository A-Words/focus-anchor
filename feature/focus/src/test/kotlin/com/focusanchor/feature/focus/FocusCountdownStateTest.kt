package com.focusanchor.feature.focus

import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusCountdownStateTest {
    @Test
    fun calculateFocusCountdownState_returnsFullDurationAtSessionStart() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(),
            nowMillis = 10_000L,
        )

        assertEquals(25 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_returnsRemainingSecondsAfterPartialElapsedTime() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(),
            nowMillis = 70_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_keepsRemainingSecondsFrozenWhilePaused() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(
                status = FocusSessionStatus.Paused,
                pausedAtEpochMillis = 70_000L,
            ),
            nowMillis = 300_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_excludesAccumulatedPauseAfterResume() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(accumulatedPausedMillis = 5_000L),
            nowMillis = 75_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_clampsRemainingSecondsToZeroWhenTimeIsExceeded() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(),
            nowMillis = 1_700_000L,
        )

        assertEquals(0L, countdownState.remainingSeconds)
        assertTrue(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_marksCountdownAsCompletedWhenItReachesZero() {
        val countdownState = calculateFocusCountdownState(
            session = sampleSession(),
            nowMillis = 1_510_000L,
        )

        assertEquals(0L, countdownState.remainingSeconds)
        assertTrue(countdownState.isCompleted)
    }

    private fun sampleSession(
        status: FocusSessionStatus = FocusSessionStatus.Running,
        pausedAtEpochMillis: Long? = null,
        accumulatedPausedMillis: Long = 0L,
    ) = FocusSession(
        title = "背单词",
        durationMinutes = 25,
        mode = FocusMode.Study,
        startedAtEpochMillis = 10_000L,
        status = status,
        pausedAtEpochMillis = pausedAtEpochMillis,
        accumulatedPausedMillis = accumulatedPausedMillis,
    )
}
