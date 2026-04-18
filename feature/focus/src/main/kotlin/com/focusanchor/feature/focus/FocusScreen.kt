package com.focusanchor.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.focusanchor.core.model.FocusSessionStatus
import com.focusanchor.core.model.SuspendItemType
import kotlinx.coroutines.delay

private val durationOptions = listOf(15, 25, 40, 60)

@Composable
fun FocusScreen(
    currentSession: FocusSession?,
    currentSuspendCount: Int,
    debugPanels: List<FocusDebugPanel> = emptyList(),
    onStartSession: (FocusSession) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onAddSuspendAnchor: (SuspendItemType, String?) -> Unit,
    onDebugAction: (FocusDebugAction) -> Unit = {},
    onFinishSession: (Boolean) -> Unit,
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
            currentSuspendCount = currentSuspendCount,
            debugPanels = debugPanels,
            onPauseSession = onPauseSession,
            onResumeSession = onResumeSession,
            onAddSuspendAnchor = onAddSuspendAnchor,
            onDebugAction = onDebugAction,
            onFinishSession = onFinishSession,
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
    currentSuspendCount: Int,
    debugPanels: List<FocusDebugPanel>,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onAddSuspendAnchor: (SuspendItemType, String?) -> Unit,
    onDebugAction: (FocusDebugAction) -> Unit,
    onFinishSession: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFinishDialog by rememberSaveable(session.startedAtEpochMillis) { mutableStateOf(false) }
    var showQuickSuspendDialog by rememberSaveable(session.startedAtEpochMillis) { mutableStateOf(false) }
    val nowMillis = produceState(
        initialValue = System.currentTimeMillis(),
        session.startedAtEpochMillis,
        session.durationMinutes,
        session.accumulatedPausedMillis,
        session.pausedAtEpochMillis,
        session.status,
    ) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            value = currentTime
            if (session.status == FocusSessionStatus.Paused) {
                break
            }
            if (
                calculateFocusCountdownState(
                    session = session,
                    nowMillis = currentTime,
                ).isCompleted
            ) {
                break
            }
            delay(1000L)
        }
    }.value
    val countdownState = calculateFocusCountdownState(session = session, nowMillis = nowMillis)

    if (session.status == FocusSessionStatus.Running && countdownState.isCompleted) {
        AutoFinishingScreen(modifier = modifier)
        return
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("结束专注") },
            text = { Text("结束后会生成本次总结并离开当前会话") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        onFinishSession(true)
                    },
                ) {
                    Text("确认结束")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("继续专注")
                }
            },
        )
    }

    if (showQuickSuspendDialog) {
        FocusQuickSuspendDialog(
            onDismiss = { showQuickSuspendDialog = false },
            onSubmit = { type, keyword ->
                onAddSuspendAnchor(type, keyword)
                showQuickSuspendDialog = false
            },
        )
    }

    when (session.status) {
        FocusSessionStatus.Running -> RunningFocusScreen(
            session = session,
            countdownState = countdownState,
            currentSuspendCount = currentSuspendCount,
            debugPanels = debugPanels,
            onPauseSession = onPauseSession,
            onRequestSuspend = { showQuickSuspendDialog = true },
            onRequestFinish = { showFinishDialog = true },
            onDebugAction = onDebugAction,
            modifier = modifier,
        )
        FocusSessionStatus.Paused -> PausedFocusScreen(
            session = session,
            countdownState = countdownState,
            currentSuspendCount = currentSuspendCount,
            debugPanels = debugPanels,
            onResumeSession = onResumeSession,
            onRequestSuspend = { showQuickSuspendDialog = true },
            onRequestFinish = { showFinishDialog = true },
            onDebugAction = onDebugAction,
            modifier = modifier,
        )
    }
}

@Composable
private fun RunningFocusScreen(
    session: FocusSession,
    countdownState: FocusCountdownState,
    currentSuspendCount: Int,
    debugPanels: List<FocusDebugPanel>,
    onPauseSession: () -> Unit,
    onRequestSuspend: () -> Unit,
    onRequestFinish: () -> Unit,
    onDebugAction: (FocusDebugAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ActiveSessionLayout(
        session = session,
        countdownState = countdownState,
        currentSuspendCount = currentSuspendCount,
        debugPanels = debugPanels,
        statusTitle = "专注中",
        statusBody = "这轮专注正在进行。切到底部其他入口后再回来，倒计时会按真实时间继续流逝。",
        actions = {
            SessionActions(
                primaryLabel = "暂停",
                onPrimaryClick = onPauseSession,
                suspendLabel = "挂起一下",
                onSuspendClick = onRequestSuspend,
                secondaryLabel = "结束",
                onSecondaryClick = onRequestFinish,
            )
        },
        onDebugAction = onDebugAction,
        modifier = modifier,
    )
}

@Composable
private fun PausedFocusScreen(
    session: FocusSession,
    countdownState: FocusCountdownState,
    currentSuspendCount: Int,
    debugPanels: List<FocusDebugPanel>,
    onResumeSession: () -> Unit,
    onRequestSuspend: () -> Unit,
    onRequestFinish: () -> Unit,
    onDebugAction: (FocusDebugAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ActiveSessionLayout(
        session = session,
        countdownState = countdownState,
        currentSuspendCount = currentSuspendCount,
        debugPanels = debugPanels,
        statusTitle = "已暂停",
        statusBody = "这轮专注已经暂停，倒计时会保持冻结，只有点继续才会恢复。",
        actions = {
            SessionActions(
                primaryLabel = "继续",
                onPrimaryClick = onResumeSession,
                suspendLabel = "挂起一下",
                onSuspendClick = onRequestSuspend,
                secondaryLabel = "结束",
                onSecondaryClick = onRequestFinish,
            )
        },
        onDebugAction = onDebugAction,
        modifier = modifier,
    )
}

@Composable
private fun AutoFinishingScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "正在生成总结",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "本轮专注已自然结束，正在为你整理本次结果。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionLayout(
    session: FocusSession,
    countdownState: FocusCountdownState,
    currentSuspendCount: Int,
    debugPanels: List<FocusDebugPanel>,
    statusTitle: String,
    statusBody: String,
    actions: @Composable () -> Unit,
    onDebugAction: (FocusDebugAction) -> Unit,
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
                    text = statusTitle,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = statusBody,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            FocusAnchorSectionCard(
                title = "剩余时间",
                body = "想到别的事时先挂起，不要离开当前专注。当前已挂起 $currentSuspendCount 条。",
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
        item {
            actions()
        }
        debugPanels.forEach { panel ->
            item {
                FocusAnchorSectionCard(
                    title = panel.title,
                    body = panel.body,
                ) {
                    panel.action?.let { action ->
                        OutlinedButton(
                            onClick = { onDebugAction(action) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(action.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionActions(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    suspendLabel: String,
    onSuspendClick: () -> Unit,
    secondaryLabel: String,
    onSecondaryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(primaryLabel)
        }
        OutlinedButton(
            onClick = onSuspendClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(suspendLabel)
        }
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(secondaryLabel)
        }
    }
}

@Composable
private fun ChipGroup(labels: List<ChipOption>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
