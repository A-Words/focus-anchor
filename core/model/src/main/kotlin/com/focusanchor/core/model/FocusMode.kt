package com.focusanchor.core.model

enum class FocusMode(
    val label: String,
    val description: String,
) {
    Study(label = "学习", description = "适合背单词、刷题、看课等常规专注场景"),
    Review(label = "复盘", description = "用于整理思路、回顾错题和复习总结"),
    Writing(label = "写作", description = "弱打扰、长时段输出场景"),
    Custom(label = "自定义", description = "预留给后续更细粒度的专注策略"),
}

