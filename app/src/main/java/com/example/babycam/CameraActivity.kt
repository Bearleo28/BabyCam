package com.example.babycam

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {

    private lateinit var videoWebView: WebView
    private lateinit var audioWebView: WebView
    private lateinit var overlayControls: View
    private lateinit var backButton: Button
    //private lateinit var muteButton: Button
    private lateinit var socket: Socket
    private var alertActive = false

    private val overlayHandler = Handler(Looper.getMainLooper())
    private val overlayHideRunnable = Runnable {
        overlayControls.animate().alpha(0f).setDuration(300).withEndAction {
            overlayControls.visibility = View.GONE
        }
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    //private var mediaPlayer: MediaPlayer? = null
    //private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //val toolBar = findViewById<Toolbar>(R.id.cameraToolBar)
        //setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        videoWebView = findViewById<WebView>(R.id.videoWebView)
        audioWebView = findViewById<WebView>(R.id.audioWebView)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        overlayControls = findViewById(R.id.overlayControls)
        backButton = findViewById(R.id.backButton)

        //muteButton = findViewById((R.id.muteButton))

// Enable JavaScript if needed (may not be necessary for MJPEG)
        videoWebView.settings.javaScriptEnabled = true
        audioWebView.settings.javaScriptEnabled = true
        videoWebView.settings.allowFileAccess = true

// Optional performance settings
        videoWebView.settings.loadWithOverviewMode = true
        videoWebView.settings.useWideViewPort = true

//Here we create the notification channel BB!
        createNotificationChannel()

//Set up the connection to the socket.IO client BB
        setupSocket()




// Set a custom WebViewClient
        videoWebView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError

            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) {
                    view.loadDataWithBaseURL(
                            "file:///android_asset/",
                            """
                                <html><body style='text-align:center; padding-top:50px; margin-top:100px;'>
                                <img style='border-radius:20px' src="Jessica.jpg" alt="Jessica" height="250" width="250" />
                                <h2 style='color:red;'>Whoopsie!!! Camera feed unavailable.</h2>
                                <p>Please check to see if the camera is powered on
                                 and is connected to the internet.</p>
                                 <p>Here's a pic of Jess while you wait!</p>
                                 <h3> Swipe down to refresh. </h3>
                                </body></html>
                                """,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request.isForMainFrame) {
                    view.loadData(
                        "<html><body style='text-align:center; padding-top:50px;'>" +
                                "<h2 style='color:red;'>Server Error (${errorResponse.statusCode})</h2>" +
                                "<p>Could not load video stream.</p>" +
                                "</body></html>",
                        "text/html",
                        "UTF-8"
                    )

                }
            }

        }
        swipeRefreshLayout.setOnRefreshListener {
            videoWebView.loadUrl("http://192.168.0.231:5000/video_feed")
            audioWebView.loadUrl("http://192.168.0.231:5020")
            swipeRefreshLayout.isRefreshing = false
        }

        videoWebView.setOnTouchListener { _, _ ->
            showOverlayTemporarily()
            false
        }

        backButton.setOnClickListener {
            startActivity(Intent(this@CameraActivity, MainActivity::class.java))
            finish()
        }

//        muteButton.setOnClickListener {
//            if (isMuted) {
//                mediaPlayer?.setVolume(1f, 1f)
//                //we can set the muteButton image to change here if we want bb
//                isMuted = false
//            }
//            else {
//                mediaPlayer?.setVolume(0f, 0f)
//                //change muteButton image to a muted state here bb
//                isMuted = true
//            }
//        }
        // Load the video stream from Flask
        videoWebView.loadUrl("http://192.168.0.231:5000/video_feed")
        audioWebView.loadUrl("http://192.168.0.231:5020")

//        startAudioStream("http://192.168.0.231/5020")

    }

    private fun setupSocket() {
        try {
            socket = IO.socket("http://192.168.0.231:5020")

            socket.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    Log.d("SocketIO", "Connected to server")
                    socket.emit("ready")
                }
            }
            socket.on("alert") { args ->
                runOnUiThread {
                    if (!alertActive) {
                        alertActive = true
                        val level = (args[0] as JSONObject).getDouble("level")
                        Log.d("SocketIO", "Alert received: level=$level")

                        showNotification("Baby ALERT!!", "Noise Level Escalated!!")
                    }
                }
            }

            socket.on("alert_clear") {
                runOnUiThread {
                    Log.d("SocketIO", "Alert Cleared")
                    alertActive = false
                    NotificationManagerCompat.from(this).cancel(1)
                }
            }

            socket.connect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001 // request code
                )
                return
            }
        }

        val builder = NotificationCompat.Builder(this, "monitor_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                showNotification("Notifications Enabled", "You’ll get alerts when sound is detected.")
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "monitor_channel",
                "Baby Monitor Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts when sound exceeds noise threshold" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

//    private fun startAudioStream(url: String) {
//        mediaPlayer = MediaPlayer().apply {
//            setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .build()
//            )
//            try {
//                setDataSource(url)
//                setOnPreparedListener { start() }
//                prepareAsync(
//            }
//            catch (e: Exception) {
//                Log.e("CameraActivity", "Error initializing MediaPlayer", e)
//            }
//        }
//    }



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()  // REQUIRED for consistent behavior
        }
    }

    private fun showOverlayTemporarily() {
        overlayControls.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).start()
        }
        overlayHandler.removeCallbacks(overlayHideRunnable)
        overlayHandler.postDelayed(overlayHideRunnable, 3000)
    }

    override fun onPause() {
        super.onPause()

        audioWebView.onPause()
        audioWebView.pauseTimers()
        audioWebView.loadUrl("javascript:if(typeof AudioContext !== 'undefined'){ if (window.audioContext){ window.audioContext.suspend(); }}")

    }

    override fun onResume() {
        super.onResume()

        audioWebView.onResume()
        audioWebView.resumeTimers()
        audioWebView.loadUrl("javascript:if(typeof AudioContext !== 'undefined'){ if(window.audioContext){ window.audioContext.resume(); }}")

    }

    override fun onDestroy() {
        super.onDestroy()
        overlayHandler.removeCallbacks(overlayHideRunnable)

        audioWebView.apply {
            loadUrl("about:blank")
            stopLoading()
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
        if (::socket.isInitialized) {
            socket.disconnect()
            socket.close()
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