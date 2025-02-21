package com.ssafy.firstproject.data.model.response

data class AuthResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val error: String
)
