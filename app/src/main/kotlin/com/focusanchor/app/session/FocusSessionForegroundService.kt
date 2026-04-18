package com.focusanchor.app.session

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.focusanchor.app.FocusAnchorApplication
import com.focusanchor.core.model.FocusSessionStatus
import com.focusanchor.core.model.clockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusSessionForegroundService : Service() {
    private val focusRepository by lazy {
        (application as FocusAnchorApplication).focusRepository
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null
    private var hasStartedForeground = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FocusSessionNotifications.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            actionPause -> focusRepository.pauseCurrentSession(System.currentTimeMillis())
            actionResume -> focusRepository.resumeCurrentSession(System.currentTimeMillis())
            actionFinish -> {
                focusRepository.finishCurrentSession(
                    finishedAtEpochMillis = System.currentTimeMillis(),
                    endedEarly = true,
                )
                stopNow()
                return START_NOT_STICKY
            }
        }

        ensureTickerRunning()
        return START_STICKY
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        if (hasStartedForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            hasStartedForeground = false
        }
        super.onDestroy()
    }

    private fun ensureTickerRunning() {
        if (tickerJob?.isActive == true) return

        tickerJob = serviceScope.launch {
            while (isActive) {
                val session = focusRepository.currentSessionFlow.value
                if (session == null) {
                    stopNow()
                    break
                }

                val clockState = session.clockState(System.currentTimeMillis())
                if (session.status == FocusSessionStatus.Running && clockState.isCompleted) {
                    focusRepository.finishCurrentSession(
                        finishedAtEpochMillis = System.currentTimeMillis(),
                        endedEarly = false,
                    )
                    stopNow()
                    break
                }

                val suspendCount = focusRepository.suspendedAnchorsFlow.value.count {
                    it.sessionStartedAtEpochMillis == session.startedAtEpochMillis
                }
                val notification = FocusSessionNotificationFactory.create(
                    context = this@FocusSessionForegroundService,
                    model = FocusSessionNotificationModel(
                        session = session,
                        remainingSeconds = clockState.remainingSeconds,
                        suspendCount = suspendCount,
                    ),
                )

                if (!hasStartedForeground) {
                    startForegroundCompat(notification)
                    hasStartedForeground = true
                } else {
                    val manager = getSystemService(android.app.NotificationManager::class.java)
                    manager.notify(FocusSessionNotifications.notificationId, notification)
                }
                delay(1_000L)
            }
        }
    }

    private fun startForegroundCompat(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FocusSessionNotifications.notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(FocusSessionNotifications.notificationId, notification)
        }
    }

    private fun stopNow() {
        if (hasStartedForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            hasStartedForeground = false
        }
        stopSelf()
    }

    companion object {
        const val actionStart = "com.focusanchor.app.session.START"
        const val actionPause = "com.focusanchor.app.session.PAUSE"
        const val actionResume = "com.focusanchor.app.session.RESUME"
        const val actionFinish = "com.focusanchor.app.session.FINISH"

        fun start(context: Context) {
            context.startForegroundService(createIntent(context, actionStart))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusSessionForegroundService::class.java))
        }

        fun createIntent(
            context: Context,
            action: String,
        ): Intent = Intent(context, FocusSessionForegroundService::class.java).apply {
            this.action = action
        }
    }
}
