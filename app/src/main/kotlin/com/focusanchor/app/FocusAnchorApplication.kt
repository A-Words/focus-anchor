package com.focusanchor.app

import android.app.Application
import com.focusanchor.core.data.FocusRepository
import com.focusanchor.core.data.InMemoryFocusRepository

class FocusAnchorApplication : Application() {
    val focusRepository: FocusRepository by lazy { InMemoryFocusRepository() }
}
