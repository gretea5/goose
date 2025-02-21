package com.ssafy.firstproject.data.model.response

data class NewsArticle(
    val biasScore: Double,
    val content: String,
    val description: String,
    val extractedAt: List<Int>,
    val id: String,
    val naverLink: String,
    val originalLink: String,
    val paragraphs: List<String>,
    val pubDate: String,
    val pubDateTimestamp: Long,
    val reliability: Double,
    val title: String,
    val topImage: String,
    val aiRate: Double,
    val evaluationMessage: String
)