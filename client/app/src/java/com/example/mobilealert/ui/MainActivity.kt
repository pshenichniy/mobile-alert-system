package com.example.mobilealert.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobilealert.api.ApiClient
import com.example.mobilealert.databinding.ActivityMainBinding
import com.example.mobilealert.service.AlertService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var alertService: AlertService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as AlertService.AlertBinder
            alertService = binder.getService()
            isServiceBound = true
            updateUI()
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            isServiceBound = false
            alertService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        bindAlertService()
    }

    private fun setupUI() {
        binding.statusText.text = "Запуск сервиса..."

        binding.btnTestAlert.setOnClickListener {
            testAlert()
        }

        binding.btnCheckStatus.setOnClickListener {
            checkServerStatus()
        }

        binding.btnStartService.setOnClickListener {
            AlertService.startService(this)
            bindAlertService()
        }
    }

    private fun bindAlertService() {
        val intent = Intent(this, AlertService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun updateUI() {
        binding.statusText.text = "✅ Сервис активен\n📡 Ожидаем команды с сервера..."
    }

    private fun testAlert() {
        alertService?.triggerAlert("Тестовое оповещение")
        Toast.makeText(this, "Тестовое оповещение запущено", Toast.LENGTH_SHORT).show()
    }

    private fun checkServerStatus() {
        lifecycleScope.launch {
            try {
                binding.statusText.text = "Проверяем сервер..."
                val response = ApiClient.alertService.getServerInfo()
                if (response.isSuccessful) {
                    val info = response.body()
                    binding.statusText.text = """
                        📊 Статус сервера:
                        Имя: ${info?.name}
                        Версия: ${info?.version}
                        Статус: ${info?.status}
                        Uptime: ${info?.uptime?.toInt()} сек
                        Тревога: ${if (info?.state?.actionEnabled == true) "АКТИВНА" else "неактивна"}
                    """.trimIndent()
                } else {
                    binding.statusText.text = "❌ Ошибка подключения к серверу"
                }
            } catch (e: Exception) {
                binding.statusText.text = "❌ Ошибка: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}