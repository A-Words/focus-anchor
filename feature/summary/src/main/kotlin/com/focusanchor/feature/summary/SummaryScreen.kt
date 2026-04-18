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
    endedEarly = false,
    interruptionCount = 1,
    suspendCount = 3,
    tone = "本次专注较稳定",
)

@Composable
fun SummaryScreen(
    summary: FocusSessionSummary? = null,
    modifier: Modifier = Modifier,
) {
    val displaySummary = summary ?: sampleSummary

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
                "任务名称：${displaySummary.title}",
                "设定 / 实际：${displaySummary.plannedMinutes} / ${displaySummary.actualMinutes} 分钟",
                "是否中途退出：${if (displaySummary.endedEarly) "是" else "否"}",
                "中断次数：${displaySummary.interruptionCount}",
                "挂起事项：${displaySummary.suspendCount}",
                "状态评价：${displaySummary.tone}",
            ),
        ) { line ->
            FocusAnchorSectionCard(
                title = line,
                body = if (summary == null) {
                    "当前显示占位总结，真实会话结束后会优先展示本轮结果。"
                } else {
                    "当前展示本轮真实结果，后续这里可扩展继续下一轮和稍后处理承接。"
                },
            )
        }
    }
}
