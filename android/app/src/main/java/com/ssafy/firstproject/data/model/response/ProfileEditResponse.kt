package com.ssafy.firstproject.data.model.response

data class ProfileEditResponse(
    val accessToken: String,
    val error: String,
    val nickname: String,
    val refreshToken: String,
    val success: Boolean
)