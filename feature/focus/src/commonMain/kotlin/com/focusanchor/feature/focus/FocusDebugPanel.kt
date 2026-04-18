package com.focusanchor.feature.focus

data class FocusDebugPanel(
    val title: String,
    val body: String,
    val action: FocusDebugAction? = null,
)

enum class FocusDebugAction(val label: String) {
    OpenNotificationSettings(label = "打开通知设置"),
    OpenPromotionSettings(label = "打开实时活动设置"),
}
