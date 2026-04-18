package com.focusanchor.app.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focusanchor.app.MainActivity
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionStatus

internal data class FocusSessionNotificationModel(
    val session: FocusSession,
    val remainingSeconds: Long,
    val suspendCount: Int,
)

internal data class FocusSessionNotificationDiagnostics(
    val notificationsEnabled: Boolean,
    val canPostPromotedNotifications: Boolean,
    val hasPromotableCharacteristics: Boolean,
    val isPromotedRequestApplied: Boolean,
    val channelImportance: Int?,
    val sdkInt: Int,
    val missingRequirement: String?,
)

internal data class FocusSessionNotificationBuildResult(
    val notification: Notification,
    val diagnostics: FocusSessionNotificationDiagnostics,
)

internal object FocusSessionNotifications {
    const val channelId = "focus_session"
    const val notificationId = 1001

    private const val logTag = "FocusNotification"
    private const val extraRequestPromotedOngoing = "android.requestPromotedOngoing"
    private const val extraShortCriticalText = "android.shortCriticalText"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            "专注进行中",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "显示专注进行中的剩余时间、暂停、结束和快速挂起入口"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun openAppNotificationSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun openPromotionSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= 36) {
            Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    fun logDiagnosticsIfChanged(
        lastDiagnostics: FocusSessionNotificationDiagnostics?,
        nextDiagnostics: FocusSessionNotificationDiagnostics,
    ) {
        if (lastDiagnostics == nextDiagnostics) return

        val level = if (nextDiagnostics.missingRequirement == null) Log.DEBUG else Log.WARN
        val message = buildString {
            append("通知实时活动诊断：")
            append("notificationsEnabled=").append(nextDiagnostics.notificationsEnabled)
            append(", canPostPromotedNotifications=").append(nextDiagnostics.canPostPromotedNotifications)
            append(", hasPromotableCharacteristics=").append(nextDiagnostics.hasPromotableCharacteristics)
            append(", isPromotedRequestApplied=").append(nextDiagnostics.isPromotedRequestApplied)
            append(", channelImportance=").append(nextDiagnostics.channelImportance)
            append(", sdkInt=").append(nextDiagnostics.sdkInt)
            nextDiagnostics.missingRequirement?.let {
                append(", missingRequirement=").append(it)
            }
        }

        when (level) {
            Log.WARN -> Log.w(logTag, message)
            else -> Log.d(logTag, message)
        }
    }

    fun buildDebugPanels(
        diagnostics: FocusSessionNotificationDiagnostics?,
        isDebugBuild: Boolean,
    ): List<com.focusanchor.feature.focus.FocusDebugPanel> {
        if (diagnostics == null) return emptyList()

        val panels = mutableListOf<com.focusanchor.feature.focus.FocusDebugPanel>()
        if (!diagnostics.notificationsEnabled) {
            panels += com.focusanchor.feature.focus.FocusDebugPanel(
                title = "通知未开启",
                body = "应用通知总开关未开启。前台通知和实时活动都可能无法正常展示。",
                action = com.focusanchor.feature.focus.FocusDebugAction.OpenNotificationSettings,
            )
        }
        if (!diagnostics.canPostPromotedNotifications) {
            panels += com.focusanchor.feature.focus.FocusDebugPanel(
                title = "未允许实时活动提升",
                body = "系统尚未允许该应用发布 promoted ongoing 通知，Android 16 的实时活动不会展示。",
                action = com.focusanchor.feature.focus.FocusDebugAction.OpenPromotionSettings,
            )
        }

        if (isDebugBuild) {
            panels += com.focusanchor.feature.focus.FocusDebugPanel(
                title = "实时活动诊断",
                body = buildString {
                    append("已申请提升=").append(if (diagnostics.isPromotedRequestApplied) "是" else "否")
                    append("，可被系统识别=").append(if (diagnostics.hasPromotableCharacteristics) "是" else "否")
                    append("，通知开关=").append(if (diagnostics.notificationsEnabled) "开启" else "关闭")
                    append("，提升授权=").append(if (diagnostics.canPostPromotedNotifications) "允许" else "未允许")
                    append("，渠道级别=").append(diagnostics.channelImportance ?: "未知")
                    append("，SDK=").append(diagnostics.sdkInt)
                    diagnostics.missingRequirement?.let {
                        append("。当前缺口：").append(it)
                    }
                },
            )
        }

        return panels
    }

    internal fun promotedOngoingExtras(
        remainingSeconds: Long,
        shouldRequestPromotedOngoing: Boolean,
    ): Bundle = Bundle().apply {
        putBoolean(extraRequestPromotedOngoing, shouldRequestPromotedOngoing)
        putCharSequence(extraShortCriticalText, "剩余 ${formatRemainingTime(remainingSeconds)}")
    }
}

internal object FocusSessionNotificationFactory {
    fun build(
        context: Context,
        model: FocusSessionNotificationModel,
    ): FocusSessionNotificationBuildResult {
        val notification = createBuilder(context, model).build()
        val diagnostics = createDiagnostics(context, notification)
        return FocusSessionNotificationBuildResult(
            notification = notification,
            diagnostics = diagnostics,
        )
    }

    fun diagnostics(
        context: Context,
        model: FocusSessionNotificationModel,
    ): FocusSessionNotificationDiagnostics = build(context, model).diagnostics
}

private fun createBuilder(
    context: Context,
    model: FocusSessionNotificationModel,
): NotificationCompat.Builder {
    val session = model.session
    val isRunning = session.status == FocusSessionStatus.Running
    val endAtMillis = System.currentTimeMillis() + (model.remainingSeconds * 1000L)
    val shouldRequestPromotedOngoing = Build.VERSION.SDK_INT >= 36

    return NotificationCompat.Builder(context, FocusSessionNotifications.channelId)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(session.title)
        .setContentText("${statusLabel(session.status)}｜剩余 ${formatRemainingTime(model.remainingSeconds)}")
        .setSubText("${session.mode.label}｜已挂起 ${model.suspendCount} 条")
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(createOpenAppPendingIntent(context))
        .setWhen(endAtMillis)
        .setUsesChronometer(isRunning)
        .setChronometerCountDown(isRunning)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .setExtras(
            FocusSessionNotifications.promotedOngoingExtras(
                remainingSeconds = model.remainingSeconds,
                shouldRequestPromotedOngoing = shouldRequestPromotedOngoing,
            ),
        )
        .setRequestPromotedOngoing(shouldRequestPromotedOngoing)
        .addAction(
            if (isRunning) {
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_media_pause,
                    "暂停",
                    PendingIntent.getService(
                        context,
                        1,
                        FocusSessionForegroundService.createIntent(context, FocusSessionForegroundService.actionPause),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                ).build()
            } else {
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_media_play,
                    "继续",
                    PendingIntent.getService(
                        context,
                        2,
                        FocusSessionForegroundService.createIntent(context, FocusSessionForegroundService.actionResume),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                ).build()
            },
        )
        .addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_input_add,
                "挂起一下",
                PendingIntent.getActivity(
                    context,
                    3,
                    QuickSuspendActivity.createIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            ).build(),
        )
        .addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "结束",
                PendingIntent.getService(
                    context,
                    4,
                    FocusSessionForegroundService.createIntent(context, FocusSessionForegroundService.actionFinish),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            ).build(),
        )
        .apply {
            if (Build.VERSION.SDK_INT >= 36) {
                setStyle(createProgressStyle(model))
            } else {
                setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "${statusLabel(model.session.status)}，剩余 ${formatRemainingTime(model.remainingSeconds)}，已挂起 ${model.suspendCount} 条。",
                    ),
                )
            }
        }
}

private fun createDiagnostics(
    context: Context,
    notification: Notification,
): FocusSessionNotificationDiagnostics {
    val manager = context.getSystemService(NotificationManager::class.java)
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val canPostPromotedNotifications = if (Build.VERSION.SDK_INT >= 36) {
        manager.canPostPromotedNotifications()
    } else {
        false
    }
    val isPromotedRequestApplied = NotificationCompat.isRequestPromotedOngoing(notification)
    val hasPromotableCharacteristics = if (Build.VERSION.SDK_INT >= 36) {
        notification.hasPromotableCharacteristics()
    } else {
        false
    }
    val channelImportance = manager.getNotificationChannel(FocusSessionNotifications.channelId)?.importance
    val missingRequirement = when {
        !notificationsEnabled -> "应用通知总开关未开启"
        Build.VERSION.SDK_INT >= 36 && !canPostPromotedNotifications -> "系统未允许该应用发布实时活动"
        Build.VERSION.SDK_INT >= 36 && !isPromotedRequestApplied -> "本次通知未显式请求 promoted ongoing"
        Build.VERSION.SDK_INT >= 36 && !hasPromotableCharacteristics -> "系统未识别到实时活动资格"
        else -> null
    }

    return FocusSessionNotificationDiagnostics(
        notificationsEnabled = notificationsEnabled,
        canPostPromotedNotifications = canPostPromotedNotifications,
        hasPromotableCharacteristics = hasPromotableCharacteristics,
        isPromotedRequestApplied = isPromotedRequestApplied,
        channelImportance = channelImportance,
        sdkInt = Build.VERSION.SDK_INT,
        missingRequirement = missingRequirement,
    )
}

private fun createProgressStyle(
    model: FocusSessionNotificationModel,
): NotificationCompat.ProgressStyle {
    val totalSeconds = (model.session.durationMinutes * 60L).coerceAtLeast(1L).toInt()
    val elapsedSeconds = (totalSeconds - model.remainingSeconds.coerceAtMost(totalSeconds.toLong())).toInt()
    return NotificationCompat.ProgressStyle()
        .setStyledByProgress(true)
        .setProgressSegments(
            listOf(
                NotificationCompat.ProgressStyle.Segment(totalSeconds)
                    .setColor(Color.parseColor("#3F51B5")),
            ),
        )
        .setProgress(elapsedSeconds)
}

private fun createOpenAppPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
    context,
    0,
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

private fun statusLabel(status: FocusSessionStatus): String = when (status) {
    FocusSessionStatus.Running -> "专注中"
    FocusSessionStatus.Paused -> "已暂停"
}

private fun formatRemainingTime(remainingSeconds: Long): String {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
