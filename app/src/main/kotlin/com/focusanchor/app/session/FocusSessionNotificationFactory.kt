package com.focusanchor.app.session

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Notification.Action
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import com.focusanchor.app.MainActivity
import com.focusanchor.core.model.FocusSession
import com.focusanchor.core.model.FocusSessionStatus

internal data class FocusSessionNotificationModel(
    val session: FocusSession,
    val remainingSeconds: Long,
    val suspendCount: Int,
)

internal object FocusSessionNotifications {
    const val channelId = "focus_session"
    const val notificationId = 1001

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
}

internal object FocusSessionNotificationFactory {
    fun create(
        context: Context,
        model: FocusSessionNotificationModel,
    ): Notification {
        val renderer: FocusSessionNotificationRenderer = when {
            Build.VERSION.SDK_INT >= 36 -> ProgressStyleNotificationRenderer
            else -> StandardNotificationRenderer
        }
        return renderer.render(context, model)
    }
}

private interface FocusSessionNotificationRenderer {
    fun render(
        context: Context,
        model: FocusSessionNotificationModel,
    ): Notification
}

private object StandardNotificationRenderer : FocusSessionNotificationRenderer {
    override fun render(
        context: Context,
        model: FocusSessionNotificationModel,
    ): Notification = createBaseBuilder(context, model)
        .setStyle(
            Notification.BigTextStyle().bigText(
                "${statusLabel(model.session.status)}，剩余 ${formatRemainingTime(model.remainingSeconds)}，已挂起 ${model.suspendCount} 条。",
            ),
        )
        .build()
}

private object ProgressStyleNotificationRenderer : FocusSessionNotificationRenderer {
    override fun render(
        context: Context,
        model: FocusSessionNotificationModel,
    ): Notification {
        val builder = createBaseBuilder(context, model)
        val totalSeconds = (model.session.durationMinutes * 60L).coerceAtLeast(1L).toInt()
        val elapsedSeconds = (totalSeconds - model.remainingSeconds.coerceAtMost(totalSeconds.toLong())).toInt()

        builder.setStyle(createProgressStyle(totalSeconds = totalSeconds, elapsedSeconds = elapsedSeconds))
        return builder.build()
    }

    @SuppressLint("NewApi")
    private fun createProgressStyle(
        totalSeconds: Int,
        elapsedSeconds: Int,
    ): Notification.ProgressStyle = Notification.ProgressStyle()
        .setStyledByProgress(true)
        .setProgressSegments(
            listOf(
                Notification.ProgressStyle.Segment(totalSeconds)
                    .setColor(Color.parseColor("#3F51B5")),
            ),
        )
        .setProgress(elapsedSeconds)
}

private fun createBaseBuilder(
    context: Context,
    model: FocusSessionNotificationModel,
): Notification.Builder {
    val session = model.session
    val isRunning = session.status == FocusSessionStatus.Running
    val endAtMillis = System.currentTimeMillis() + (model.remainingSeconds * 1000L)

    return Notification.Builder(context, FocusSessionNotifications.channelId)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(session.title)
        .setContentText("${statusLabel(session.status)}｜剩余 ${formatRemainingTime(model.remainingSeconds)}")
        .setSubText("${session.mode.label}｜已挂起 ${model.suspendCount} 条")
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setContentIntent(createOpenAppPendingIntent(context))
        .setWhen(endAtMillis)
        .setUsesChronometer(isRunning)
        .setChronometerCountDown(isRunning)
        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        .addAction(
            if (isRunning) {
                createServiceAction(
                    context = context,
                    requestCode = 1,
                    title = "暂停",
                    iconRes = android.R.drawable.ic_media_pause,
                    action = FocusSessionForegroundService.actionPause,
                )
            } else {
                createServiceAction(
                    context = context,
                    requestCode = 2,
                    title = "继续",
                    iconRes = android.R.drawable.ic_media_play,
                    action = FocusSessionForegroundService.actionResume,
                )
            },
        )
        .addAction(
            createActivityAction(
                context = context,
                requestCode = 3,
                title = "挂起一下",
                iconRes = android.R.drawable.ic_input_add,
            ),
        )
        .addAction(
            createServiceAction(
                context = context,
                requestCode = 4,
                title = "结束",
                iconRes = android.R.drawable.ic_menu_close_clear_cancel,
                action = FocusSessionForegroundService.actionFinish,
            ),
        )
        .applyPromotedOngoingIfSupported()
}

private fun Notification.Builder.applyPromotedOngoingIfSupported(): Notification.Builder = apply {
    if (Build.VERSION.SDK_INT >= 36) {
        setFlag(Notification.FLAG_PROMOTED_ONGOING, true)
    }
}

private fun createServiceAction(
    context: Context,
    requestCode: Int,
    title: String,
    iconRes: Int,
    action: String,
): Action = Action.Builder(
    Icon.createWithResource(context, iconRes),
    title,
    PendingIntent.getService(
        context,
        requestCode,
        FocusSessionForegroundService.createIntent(context, action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    ),
).build()

private fun createActivityAction(
    context: Context,
    requestCode: Int,
    title: String,
    iconRes: Int,
): Action = Action.Builder(
    Icon.createWithResource(context, iconRes),
    title,
    PendingIntent.getActivity(
        context,
        requestCode,
        QuickSuspendActivity.createIntent(context),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    ),
).build()

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
