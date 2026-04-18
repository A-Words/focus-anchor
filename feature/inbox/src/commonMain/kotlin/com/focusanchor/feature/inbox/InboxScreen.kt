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

@Composable
fun InboxScreen(
    anchors: List<SuspendAnchor>,
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
                    text = "稍后处理箱",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "统一承接专注中挂起的意图，后续可接入转待办、删除、标记忽略等动作。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (anchors.isEmpty()) {
            item {
                FocusAnchorSectionCard(
                    title = "还没有挂起事项",
                    body = "开始专注后，可以从页面或通知里把突发念头先挂进这里。",
                )
            }
        } else {
            items(anchors, key = { it.id }) { anchor ->
                FocusAnchorSectionCard(
                    title = buildString {
                        append(anchor.type.label)
                        anchor.keyword?.takeIf { it.isNotBlank() }?.let {
                            append("｜")
                            append(it)
                        }
                    },
                    body = "已收入稍后箱。后续这里可扩展转待办、忽略和处理状态。",
                )
            }
        }
    }
}
