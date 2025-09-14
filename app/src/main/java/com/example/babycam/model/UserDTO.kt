package com.example.babycam.model

data class UserDTO(
    val username: String,
    val isLockedOut: Boolean,
    val failedAttempts: Int,
    val isAdmin: Boolean
)
