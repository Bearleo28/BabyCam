package com.example.babycam

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import android.widget.Button
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.content.Intent

class CameraActivity : AppCompatActivity() {

    private lateinit var videoWebView: WebView
    private lateinit var overlayControls: View
    private lateinit var backButton: Button

    private val overlayHandler = Handler(Looper.getMainLooper())
    private val overlayHideRunnable = Runnable {
        overlayControls.animate().alpha(0f).setDuration(300).withEndAction {
            overlayControls.visibility = View.GONE
        }
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //val toolBar = findViewById<Toolbar>(R.id.cameraToolBar)
        //setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        videoWebView = findViewById<WebView>(R.id.videoWebView)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        overlayControls = findViewById(R.id.overlayControls)
        backButton = findViewById(R.id.backButton)

// Enable JavaScript if needed (may not be necessary for MJPEG)
        videoWebView.settings.javaScriptEnabled = true
        videoWebView.settings.allowFileAccess = true

// Optional performance settings
        videoWebView.settings.loadWithOverviewMode = true
        videoWebView.settings.useWideViewPort = true

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
                                <img style='border-radius:20px;' src="Jessica.jpg" alt="Jessica" height="250" width="250" />
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
        // Load the video stream from Flask
        videoWebView.loadUrl("http://192.168.0.231:5000/video_feed")

    }



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
    override fun onDestroy() {
        super.onDestroy()
        overlayHandler.removeCallbacks(overlayHideRunnable)
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