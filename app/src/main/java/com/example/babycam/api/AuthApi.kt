package com.example.babycam.api

import com.example.babycam.model.LoginRequest
import com.example.babycam.model.LoginResponse
import com.example.babycam.model.RegisterRequest
import com.example.babycam.model.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST



interface AuthApi {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
