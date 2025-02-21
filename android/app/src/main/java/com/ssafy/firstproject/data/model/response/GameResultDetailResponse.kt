package com.ssafy.firstproject.data.model.response

data class GameResultDetailResponse(
    val correctAnswer: String,
    val fakeReason: String,
    val selectionPercentages: Map<String, Double>
)