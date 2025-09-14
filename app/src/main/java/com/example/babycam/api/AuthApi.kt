package com.example.babycam.api

import com.example.babycam.model.AdminRequest
import com.example.babycam.model.AdminResponse
import com.example.babycam.model.LoginRequest
import com.example.babycam.model.LoginResponse
import com.example.babycam.model.RegisterRequest
import com.example.babycam.model.RegisterResponse
import com.example.babycam.model.UnlockRequest
import com.example.babycam.model.UnlockResponse
import com.example.babycam.model.UserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET



interface AuthApi {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/admin/unlock")
    fun unlock(@Body request: UnlockRequest): Call<UnlockResponse>

    @POST("/admin/toggleAdmin")
    fun giveAdmin(@Body request: AdminRequest): Call<AdminResponse>

    @GET("api/auth/admin/users")
    fun getUsers(): Call<List<UserDTO>>
}
