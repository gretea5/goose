package com.ssafy.firstproject.data.model.response

data class FakeNews(
    val content: String,
    val correctAnswer: String,
    val dwellTimeRanking: List<TimeRanking>,
    val fakeReason: String,
    val imageUrl: String,
    val newsDate: String,
    val id: String,
    val selectionPercentages: Map<String, Double>,
    val title: String,
    val voteCounts: Map<String, Int>
)