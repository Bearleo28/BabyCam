package com.example.babycam

import android.view.View
import android.os.Bundle
import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.TextView
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
import com.example.babycam.ui.theme.BabyCamTheme
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Build

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainText = findViewById<TextView>(R.id.MainText)

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                     R.id.menu_logout -> {
                         showLogoutDialog()
                         true
                     }
                    R.id.menu_admin -> {
                        Toast.makeText(this@MainActivity, "This is a sneak preview Bruh!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@MainActivity, AdminActivity::class.java))
                        true
                    }
                    R.id.menu_background_notifications -> {
                        // Toggle the service
                        val intent = Intent(this, MonitorService::class.java)
                        if (MonitorService.isRunning) {
                            stopService(intent)
                            MonitorService.isRunning = false
                            Toast.makeText(this, "Notifications are OFF", Toast.LENGTH_SHORT).show()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                            MonitorService.isRunning = true
                            Toast.makeText(this, "Notifications are ON", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }


        val viewCameraButton = findViewById<Button>(R.id.viewCameraButton)
        viewCameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()  // REQUIRED for consistent behavior
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Sure Am Skippy!") { _, _ ->
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this@MainActivity, "Successfully Logged Out", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Take Me Back Bruh", null)
            .show()
    }


}