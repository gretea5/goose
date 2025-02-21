package com.ssafy.firstproject.data.model.request

data class ProfileEditRequest(
    val newNickname: String,
    val newPassword: String
)