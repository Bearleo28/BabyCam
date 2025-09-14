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
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import com.example.babycam.api.RetrofitClient
import com.example.babycam.model.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat

class AdminActivity: AppCompatActivity() {

    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val backButton = findViewById<Button>(R.id.adminBack)
        tableLayout = findViewById(R.id.userTable)

        backButton.setOnClickListener {
            startActivity(Intent(this@AdminActivity, MainActivity::class.java))
        }
        fetchUsers()
    }

    private fun fetchUsers() {
        RetrofitClient.instance.getUsers().enqueue(object : Callback<List<UserDTO>> {
            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    populateTable(users)
                    Log.d("API", "Code: ${response.code()}")
                    Log.d("API", "Message: Successfully retrieved User Info")
                } else {
                    Toast.makeText(this@AdminActivity, "API Error", Toast.LENGTH_SHORT).show()
                    Log.e("API", "Code: ${response.code()}")
                    Log.e("API", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun populateTable(users: List<UserDTO>) {
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        for (user in users) {
            val row = TableRow(this)

            val nameView = TextView(this).apply {
                text = user.username
                setPadding(20,20,20,20)
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                gravity = Gravity.CENTER
            }

            val lockedView = TextView(this).apply {
                text = user.isLockedOut.toString().uppercase()
                setPadding(20,20,20,20)
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                gravity = Gravity.CENTER
            }

            val attemptsView = TextView(this).apply {
                text = user.failedAttempts.toString()
                setPadding(20,20,20,20)
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                gravity = Gravity.CENTER
            }

            val adminView = TextView(this).apply {
                text = user.isAdmin.toString().uppercase()
                setPadding(20,20,20,20)
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                gravity = Gravity.CENTER
            }


            row.addView(nameView)
            row.addView(lockedView)
            row.addView(attemptsView)
            row.addView(adminView)

            tableLayout.addView(row)
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

}