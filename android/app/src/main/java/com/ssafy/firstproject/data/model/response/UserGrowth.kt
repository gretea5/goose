package com.ssafy.firstproject.data.model.response

data class UserGrowth(
    val correctCount: Int,
    val correctRate: Double,
    val gameRecords: List<GameRecord>,
    val totalQuestions: Int,
    val nickname: String
)