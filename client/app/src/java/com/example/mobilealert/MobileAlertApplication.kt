package com.example.mobilealert

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.mobilealert.service.AlertService

class MobileAlertApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Автозапуск сервиса при старте приложения
        AlertService.startService(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                AlertService.CHANNEL_ID,
                "Сервис оповещений",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для фонового сервиса оповещений"
            }

            val alertChannel = NotificationChannel(
                AlertService.ALERT_CHANNEL_ID,
                "Экстренные оповещения",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для экстренных оповещений и тревог"
                setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM), null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }
}