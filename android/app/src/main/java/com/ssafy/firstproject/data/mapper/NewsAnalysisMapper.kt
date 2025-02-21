package com.ssafy.firstproject.data.mapper

import com.ssafy.firstproject.data.model.response.UserNews
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle

object NewsMapper {
    fun mapToNewsAnalysisArticle(userNews: UserNews): NewsAnalysisArticle {
        return NewsAnalysisArticle(
            id = userNews.id,
            biasScore = userNews.biasScore,
            content = userNews.content,
            description = userNews.description,
            extractedAt = userNews.extractedAt,
            naverLink = userNews.naverLink,
            newsAgency = userNews.newsAgency,
            originalLink = userNews.originalLink,
            paragraphReasons = userNews.paragraphReasons,
            paragraphReliabilities = userNews.paragraphReliabilities.map { it },
            paragraphs = userNews.paragraphs,
            pubDate = userNews.pubDate,
            pubDateTimestamp = convertPubDateToTimestamp(userNews.pubDate),
            reliability = userNews.reliability,
            title = userNews.title,
            topImage = userNews.topImage,
            aiRate = userNews.aiRate,
            evaluationMessage = userNews.evaluationMessage
        )
    }

    private fun convertPubDateToTimestamp(pubDate: String?): Long {
        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            formatter.parse(pubDate ?: "")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
