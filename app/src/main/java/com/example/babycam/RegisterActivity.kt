package com.example.babycam


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.babycam.api.RetrofitClient
import com.example.babycam.model.LoginRequest
import com.example.babycam.model.LoginResponse
import com.example.babycam.model.RegisterRequest
import com.example.babycam.model.RegisterResponse
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat.RegisterReceiverFlags
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val registerUser = findViewById<EditText>(R.id.registerUser)
        val passwordEnter = findViewById<EditText>(R.id.passwordEnter)
        val passwordConfirm = findViewById<EditText>(R.id.passwordConfirm)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginReturnButton = findViewById<Button>(R.id.loginReturnButton)
        val registerMessage = findViewById<TextView>(R.id.registerMessage)

        createAccountButton.setOnClickListener {
            val user = registerUser.text.toString()
            val pass1 = passwordEnter.text.toString()
            val pass2 = passwordConfirm.text.toString()

            if (pass1 == pass2) {
                val request = RegisterRequest(user, pass1)

                RetrofitClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Successful!",
                                Toast.LENGTH_LONG
                            ).show()
                            createAccountButton.visibility = View.GONE
                            registerMessage.text = "Account Created! Weclome to the Fam!"
                            registerMessage.setTextColor(getColor(R.color.Green))
                            loginReturnButton.visibility = View.VISIBLE

                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        t.printStackTrace()
                        Toast.makeText(this@RegisterActivity, "Something went wrong bb ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
            else {
                registerMessage.visibility = View.VISIBLE
                registerMessage.text = "Passwords do not match."
                registerMessage.setTextColor(getColor(R.color.Red))
            }
        }

        loginReturnButton.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
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