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
import com.focusanchor.core.model.FocusSessionSummary

@Composable
fun HistoryScreen(
    summaries: List<FocusSessionSummary>,
    modifier: Modifier = Modifier,
) {
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
        if (summaries.isEmpty()) {
            item {
                FocusAnchorSectionCard(
                    title = "还没有历史记录",
                    body = "完成一轮专注后，这里会保留你的会话结果。",
                )
            }
        } else {
            items(summaries, key = { "${it.title}-${it.actualMinutes}-${it.suspendCount}" }) { summary ->
                FocusAnchorSectionCard(
                    title = "${summary.title}｜${summary.actualMinutes} 分钟",
                    body = "中断 ${summary.interruptionCount} 次，挂起 ${summary.suspendCount} 条，${summary.tone}",
                )
            }
        }
    }
}
