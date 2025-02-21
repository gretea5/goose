package com.ssafy.firstproject.data.model.request

data class UserChoiceRequest(
    val dwellTime: Long,
    val newsId: String,
    val userChoice: String
)