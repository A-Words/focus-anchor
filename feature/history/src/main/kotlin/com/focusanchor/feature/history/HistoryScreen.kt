package com.focusanchor.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusanchor.core.designsystem.component.FocusAnchorSectionCard

private val historyPlaceholders = listOf(
    "今天专注 3 次，总时长 95 分钟",
    "最常见任务：背单词",
    "高分心时段：20:00 - 21:00",
)

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "第一版先保留轻量列表，后续再接趋势统计、行为分析与时间段洞察。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(historyPlaceholders) { item ->
            FocusAnchorSectionCard(
                title = item,
                body = "当前用于占位目录与职责，后续这里会切成列表项模型和统计模块。",
            )
        }
    }
}

