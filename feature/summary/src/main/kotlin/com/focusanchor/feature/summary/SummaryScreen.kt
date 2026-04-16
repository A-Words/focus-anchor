package com.focusanchor.feature.summary

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

private val sampleSummary = FocusSessionSummary(
    title = "背单词",
    plannedMinutes = 25,
    actualMinutes = 25,
    interruptionCount = 1,
    suspendCount = 3,
    tone = "本次专注较稳定",
)

@Composable
fun SummaryScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "专注总结",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "会话结束后展示关键反馈，并作为进入下一轮专注或处理挂起事项的桥梁。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(
            listOf(
                "任务名称：${sampleSummary.title}",
                "设定 / 实际：${sampleSummary.plannedMinutes} / ${sampleSummary.actualMinutes} 分钟",
                "中断次数：${sampleSummary.interruptionCount}",
                "挂起事项：${sampleSummary.suspendCount}",
                "状态评价：${sampleSummary.tone}",
            ),
        ) { line ->
            FocusAnchorSectionCard(
                title = line,
                body = "后续这里可扩展操作建议、继续下一轮、转入稍后处理箱。",
            )
        }
    }
}

