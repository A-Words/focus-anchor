package com.focusanchor.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusanchor.core.designsystem.component.FocusAnchorSectionCard
import com.focusanchor.core.model.FocusMode
import com.focusanchor.core.model.FocusSession
import kotlinx.coroutines.delay

private val durationOptions = listOf(15, 25, 40, 60)

@Composable
fun FocusScreen(
    currentSession: FocusSession?,
    onStartSession: (FocusSession) -> Unit,
    onOpenSummary: () -> Unit,
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
            onOpenSummary = onOpenSummary,
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
                                    startedAtEpochMillis = System.currentTimeMillis(),
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
    onOpenSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nowMillis = produceState(
        initialValue = System.currentTimeMillis(),
        key1 = session.startedAtEpochMillis,
        key2 = session.durationMinutes,
    ) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            value = currentTime
            if (
                calculateFocusCountdownState(
                    startedAtEpochMillis = session.startedAtEpochMillis,
                    durationMinutes = session.durationMinutes,
                    nowMillis = currentTime,
                ).isCompleted
            ) {
                break
            }
            delay(1000L)
        }
    }.value
    val countdownState = calculateFocusCountdownState(
        startedAtEpochMillis = session.startedAtEpochMillis,
        durationMinutes = session.durationMinutes,
        nowMillis = nowMillis,
    )

    if (countdownState.isCompleted) {
        CompletedFocusScreen(
            session = session,
            onOpenSummary = onOpenSummary,
            modifier = modifier,
        )
    } else {
        RunningFocusScreen(
            session = session,
            countdownState = countdownState,
            modifier = modifier,
        )
    }
}

@Composable
private fun RunningFocusScreen(
    session: FocusSession,
    countdownState: FocusCountdownState,
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
                    text = "这轮专注正在进行。切到底部其他入口后再回来，倒计时会按真实时间继续流逝。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "剩余时间",
                body = "先把注意力留在当前任务，想到别的事时后续再补挂起入口。",
            ) {
                Text(
                    text = formatCountdown(countdownState.remainingSeconds),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "当前任务",
                body = session.title,
            )
        }
        item {
            FocusAnchorSectionCard(
                title = "专注模式",
                body = session.mode.label,
            )
        }
        item {
            FocusAnchorSectionCard(
                title = "设定时长",
                body = "${session.durationMinutes} 分钟",
            )
        }
    }
}

@Composable
private fun CompletedFocusScreen(
    session: FocusSession,
    onOpenSummary: () -> Unit,
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
                    text = "本轮已完成",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "倒计时已经归零。先确认本轮结果，再进入总结页查看后续承接。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "完成状态",
                body = "本轮专注已完成，当前先停留在完成态，不自动跳转页面。",
            ) {
                Text(
                    text = "00:00",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "本轮摘要",
                body = "任务：${session.title}\n模式：${session.mode.label}\n设定时长：${session.durationMinutes} 分钟",
            ) {
                Button(
                    onClick = onOpenSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("查看总结")
                }
            }
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

private fun formatCountdown(remainingSeconds: Long): String {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
