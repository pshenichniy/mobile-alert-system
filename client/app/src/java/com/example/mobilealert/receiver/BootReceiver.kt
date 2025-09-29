package com.example.mobilealert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mobilealert.service.AlertService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Запускаем сервис после перезагрузки
            AlertService.startService(context)
        }
    }
}