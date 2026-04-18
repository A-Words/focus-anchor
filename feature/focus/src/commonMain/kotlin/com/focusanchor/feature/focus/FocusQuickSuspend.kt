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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusanchor.core.designsystem.component.FocusAnchorSectionCard
import com.focusanchor.core.model.SuspendItemType

@Composable
fun FocusQuickSuspendDialog(
    onDismiss: () -> Unit,
    onSubmit: (SuspendItemType, String?) -> Unit,
) {
    var selectedTypeName by rememberSaveable { mutableStateOf(SuspendItemType.Message.name) }
    var keyword by rememberSaveable { mutableStateOf("") }
    val selectedType = SuspendItemType.valueOf(selectedTypeName)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("挂起一下") },
        text = {
            FocusQuickSuspendForm(
                selectedType = selectedType,
                keyword = keyword,
                onTypeSelected = { selectedTypeName = it.name },
                onKeywordChange = { keyword = it.take(5) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(selectedType, keyword.trim().ifBlank { null })
                },
            ) {
                Text("加入稍后箱")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("继续专注")
            }
        },
    )
}

@Composable
fun FocusQuickSuspendScreen(
    onDismiss: () -> Unit,
    onSubmit: (SuspendItemType, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTypeName by rememberSaveable { mutableStateOf(SuspendItemType.Message.name) }
    var keyword by rememberSaveable { mutableStateOf("") }
    val selectedType = SuspendItemType.valueOf(selectedTypeName)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FocusAnchorSectionCard(
                title = "挂起内容",
                body = "关键词可不填；如果填写，尽量控制在 1~5 个字。",
            ) {
                FocusQuickSuspendForm(
                    selectedType = selectedType,
                    keyword = keyword,
                    onTypeSelected = { selectedTypeName = it.name },
                    onKeywordChange = { keyword = it.take(5) },
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        onSubmit(selectedType, keyword.trim().ifBlank { null })
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("加入稍后箱")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
private fun FocusQuickSuspendForm(
    selectedType: SuspendItemType,
    keyword: String,
    onTypeSelected: (SuspendItemType) -> Unit,
    onKeywordChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SuspendItemType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(type.label) },
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("关键词") },
            placeholder = { Text("可不填，例如：导师") },
            supportingText = { Text("最多 5 个字") },
        )
    }
}
