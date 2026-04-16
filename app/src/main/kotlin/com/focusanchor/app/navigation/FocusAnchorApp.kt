package com.focusanchor.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.focusanchor.feature.focus.FocusScreen
import com.focusanchor.feature.history.HistoryScreen
import com.focusanchor.feature.inbox.InboxScreen
import com.focusanchor.feature.summary.SummaryScreen

private enum class TopLevelDestination(val label: String) {
    Focus(label = "专注"),
    Inbox(label = "稍后箱"),
    History(label = "历史"),
    Summary(label = "总结"),
}

@Composable
fun FocusAnchorApp() {
    var destination by rememberSaveable { mutableStateOf(TopLevelDestination.Focus) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    TopLevelDestination.Focus -> Icons.Outlined.HourglassTop
                                    TopLevelDestination.Inbox -> Icons.Outlined.Inbox
                                    TopLevelDestination.History -> Icons.Outlined.History
                                    TopLevelDestination.Summary -> Icons.Outlined.Insights
                                },
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (destination) {
            TopLevelDestination.Focus -> FocusScreen(modifier = Modifier.padding(innerPadding))
            TopLevelDestination.Inbox -> InboxScreen(modifier = Modifier.padding(innerPadding))
            TopLevelDestination.History -> HistoryScreen(modifier = Modifier.padding(innerPadding))
            TopLevelDestination.Summary -> SummaryScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
