package com.example.mobilealert.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.mobilealert.R
import com.example.mobilealert.api.ApiClient
import com.example.mobilealert.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class AlertService : Service() {

    companion object {
        const val CHANNEL_ID = "AlertServiceChannel"
        const val ALERT_CHANNEL_ID = "AlertChannel"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        const val POLLING_INTERVAL = 10000L // 10 секунд

        fun startService(context: android.content.Context) {
            val intent = Intent(context, AlertService::class.java)
            context.startForegroundService(intent)
        }
    }

    private val binder = AlertBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var pollingJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null

    private var lastAlertTime: Long = 0
    private val ALERT_COOLDOWN = 30000L // 30 секунд коoldown

    inner class AlertBinder : Binder() {
        fun getService(): AlertService = this@AlertService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createServiceNotification())
        startPolling()
        return START_STICKY
    }

    private fun createServiceNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Сервис оповещений")
            .setContentText("Служба активна. Ожидаем команды...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startPolling() {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    checkServerForAlerts()
                }
            }, 0, POLLING_INTERVAL)
        }
    }

    private fun checkServerForAlerts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.alertService.getStatus()
                if (response.isSuccessful) {
                    val status = response.body()
                    if (status != null && status.hasAction) {
                        // Проверяем cooldown
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastAlertTime > ALERT_COOLDOWN) {
                            lastAlertTime = currentTime
                            handler.post {
                                triggerAlert(status.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerAlert(message: String) {
        playAlertSound()
        showAlertNotification(message)
    }

    private fun playAlertSound() {
        try {
            // Пытаемся воспроизвести кастомный звук
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound).apply {
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            // Fallback на стандартный звук
            try {
                val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, alertSound).apply {
                    start()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun showAlertNotification(message: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("🚨 Тревога!")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        mediaPlayer?.release()
        pollingJob?.cancel()
    }
}