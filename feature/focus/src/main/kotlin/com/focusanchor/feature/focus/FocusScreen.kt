package com.focusanchor.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusanchor.core.designsystem.component.FocusAnchorSectionCard
import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession

private val durationOptions = listOf(15, 25, 40, 60)

@Composable
fun FocusScreen(
    currentSession: FocusSession?,
    onStartSession: (FocusSession) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (currentSession == null) {
        FocusCreationScreen(
            onStartSession = onStartSession,
            modifier = modifier,
        )
    } else {
        ActiveFocusScreen(
            session = currentSession,
            modifier = modifier,
        )
    }
}

@Composable
private fun FocusCreationScreen(
    onStartSession: (FocusSession) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedDurationMinutes by rememberSaveable { mutableIntStateOf(25) }
    var selectedModeName by rememberSaveable { mutableStateOf(FocusMode.Study.name) }
    val selectedMode = FocusMode.valueOf(selectedModeName)
    val canStart = title.trim().isNotEmpty()

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
                    text = "先明确本轮唯一任务，再进入专注状态。当前先完成任务名、时长和模式的最小创建闭环。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "开始专注",
                body = "填写任务名，选择时长和模式后即可开始当前会话。",
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("任务名") },
                        placeholder = { Text("例如：背单词") },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "选择时长",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        ChipGroup(
                            labels = durationOptions.map { minutes ->
                                ChipOption(
                                    label = "$minutes 分钟",
                                    selected = selectedDurationMinutes == minutes,
                                    onClick = { selectedDurationMinutes = minutes },
                                )
                            },
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "选择模式",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        ChipGroup(
                            labels = FocusMode.entries.map { mode ->
                                ChipOption(
                                    label = mode.label,
                                    selected = selectedMode == mode,
                                    onClick = { selectedModeName = mode.name },
                                )
                            },
                        )
                        Text(
                            text = selectedMode.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Button(
                        onClick = {
                            onStartSession(
                                FocusSession(
                                    title = title.trim(),
                                    durationMinutes = selectedDurationMinutes,
                                    mode = selectedMode,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canStart,
                    ) {
                        Text("开始专注")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveFocusScreen(
    session: FocusSession,
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
                    text = "专注中",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "当前会话已经建立，切换到底部其他入口后返回，这轮专注仍会保留在内存中。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(
            listOf(
                "当前任务：${session.title}",
                "专注模式：${session.mode.label}",
                "设定时长：${session.durationMinutes} 分钟",
            ),
        ) { line ->
            FocusAnchorSectionCard(
                title = line,
                body = "本轮先提供静态专注状态展示，后续再接倒计时、挂起和结束流转。",
            )
        }
    }
}

@Composable
private fun ChipGroup(labels: List<ChipOption>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.chunked(2).forEach { row ->
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { option ->
                    FilterChip(
                        selected = option.selected,
                        onClick = option.onClick,
                        label = { Text(option.label) },
                    )
                }
            }
        }
    }
}

private data class ChipOption(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)
