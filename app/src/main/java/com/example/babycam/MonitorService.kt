package com.example.babycam

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class MonitorService : Service() {

    private var alertActive = false
    private val alertHandler = Handler(Looper.getMainLooper())
    private var alertClearRunnable: Runnable? = null
    private val MIN_ALERT_DURATION_MS = 2000L

    companion object {
        var isRunning = false
        const val CHANNEL_ID = "monitor_channel"
        const val NOTIFICATION_ID = 100
    }

    private lateinit var socket: Socket

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()

        // Start as foreground service
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Baby Monitor Running")
            .setContentText("Listening for alerts...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
        startForeground(NOTIFICATION_ID, builder.build())

        setupSocket()
    }

    private fun setupSocket() {
        try {
            socket = IO.socket("http://192.168.0.231:5020") // Your Pi server

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("MonitorService", "Connected to server")
                socket.emit("ready")
            }

            socket.on("alert") { args ->
                val level = (args[0] as JSONObject).getDouble("level")
                Log.d("MonitorService", "Alert received: level=$level")
                if (!alertActive) {
                    alertActive = true
                    showAlertNotification("Baby ALERT!", "Noise level exceeded!")
                }

                alertClearRunnable?.let { alertHandler.removeCallbacks(it) }
            }

            socket.on("alert_clear") {

                alertClearRunnable = Runnable {
                    Log.d("MonitorService", "Alert cleared")
                    alertActive = false
                    //NotificationManagerCompat.from(this).cancel(200)
                }
                alertHandler.postDelayed(alertClearRunnable!!, MIN_ALERT_DURATION_MS)
            }

            socket.connect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAlertNotification(title: String, message: String) {
        try {

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            NotificationManagerCompat.from(this).notify(200, builder.build())
            Log.d("Monitor Service", "Notification Sent...")
        } catch (e: Exception) {
            Log.d("Monitor Service", "Failed to send notification: $e")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Baby Monitor Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Background alerts from your Raspberry Pi" }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::socket.isInitialized) {
            socket.disconnect()
            socket.close()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
