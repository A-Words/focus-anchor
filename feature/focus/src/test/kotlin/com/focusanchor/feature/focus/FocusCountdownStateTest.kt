package com.focusanchor.feature.focus

import com.focusanchor.core.model.FocusSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusCountdownStateTest {
    @Test
    fun calculateFocusCountdownState_returnsFullDurationAtSessionStart() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 0L,
            pausedAtEpochMillis = null,
            status = FocusSessionStatus.Running,
            nowMillis = 10_000L,
        )

        assertEquals(25 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_returnsRemainingSecondsAfterPartialElapsedTime() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 0L,
            pausedAtEpochMillis = null,
            status = FocusSessionStatus.Running,
            nowMillis = 70_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_keepsRemainingSecondsFrozenWhilePaused() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 0L,
            pausedAtEpochMillis = 70_000L,
            status = FocusSessionStatus.Paused,
            nowMillis = 300_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_excludesAccumulatedPauseAfterResume() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 5_000L,
            pausedAtEpochMillis = null,
            status = FocusSessionStatus.Running,
            nowMillis = 75_000L,
        )

        assertEquals(24 * 60L, countdownState.remainingSeconds)
        assertFalse(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_clampsRemainingSecondsToZeroWhenTimeIsExceeded() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 0L,
            pausedAtEpochMillis = null,
            status = FocusSessionStatus.Running,
            nowMillis = 1_700_000L,
        )

        assertEquals(0L, countdownState.remainingSeconds)
        assertTrue(countdownState.isCompleted)
    }

    @Test
    fun calculateFocusCountdownState_marksCountdownAsCompletedWhenItReachesZero() {
        val countdownState = calculateFocusCountdownState(
            startedAtEpochMillis = 10_000L,
            durationMinutes = 25,
            accumulatedPausedMillis = 0L,
            pausedAtEpochMillis = null,
            status = FocusSessionStatus.Running,
            nowMillis = 1_510_000L,
        )

        assertEquals(0L, countdownState.remainingSeconds)
        assertTrue(countdownState.isCompleted)
    }
}
