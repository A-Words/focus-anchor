package com.focusanchor.core.data

import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        )

        repository.startSession(session)

        assertEquals(session, repository.currentSession())
    }
}
