package com.focusanchor.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusanchor.core.designsystem.component.FocusAnchorSectionCard
import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.SuspendItemType

@Composable
fun FocusScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "专注会话",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "主入口预留任务、时长、模式三段式输入，后续可接 ViewModel 与倒计时服务。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "开始专注",
                body = "MVP 首页负责建立当前唯一任务，后续这里接入任务名、时长选择器和开始按钮。",
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FocusMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = {},
                            label = { Text(mode.label) },
                        )
                    }
                }
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "快速挂起",
                body = "这里承接“挂起一下”动作，控制操作在 1~2 秒内完成。",
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SuspendItemType.entries.forEach { type ->
                        AssistChip(
                            onClick = {},
                            label = { Text(type.label) },
                        )
                    }
                }
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "专注监督",
                body = "后续在这里接入倒计时、常驻通知、离开页面确认与轻提醒策略。",
            )
        }
        items(
            listOf(
                "计时器状态与前台服务入口",
                "离开专注页的确认弹层",
                "中断统计与提醒阈值",
            ),
        ) { placeholder ->
            Text(
                text = "• $placeholder",
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
