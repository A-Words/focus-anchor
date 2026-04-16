package com.focusanchor.feature.inbox

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
import com.focusanchor.core.model.SuspendAnchor
import com.focusanchor.core.model.SuspendItemType

private val sampleAnchors = listOf(
    SuspendAnchor(type = SuspendItemType.Message, keyword = "导师"),
    SuspendAnchor(type = SuspendItemType.Research, keyword = "六级报名"),
    SuspendAnchor(type = SuspendItemType.Idea, keyword = "课题名"),
)

@Composable
fun InboxScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "稍后处理箱",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "统一承接专注中挂起的意图，后续可接入转待办、删除、标记忽略等动作。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(sampleAnchors) { anchor ->
            FocusAnchorSectionCard(
                title = "${anchor.type.label}｜${anchor.keyword}",
                body = "这里保留最小记录单位，后续可扩展为时间戳、来源会话、处理状态。",
            )
        }
    }
}

