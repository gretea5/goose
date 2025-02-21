package com.ssafy.firstproject.data.model.response

data class GameResultResponse(
    val correct: Boolean,
    val dwellTime: Long,
    val nickname: String,
    val ranking: List<TimeRanking>,
    val solvedAt: List<Int>,
    val userChoice: String
)