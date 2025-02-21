package com.ssafy.firstproject.data.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsAnalysisArticle(
    val id: String? = "",
    val biasScore: Double? = 0.0,
    val content: String? = "",
    val description: String? = "",
    val extractedAt: List<Int>? = listOf(),
    val naverLink: String? = "",
    val newsAgency: String? = "",
    val originalLink: String? = "",
    val paragraphReasons: List<String?> = listOf(),
    val paragraphReliabilities: List<Double?> = listOf(),
    val paragraphs: List<String?> = listOf(),
    val pubDate: String? = "",
    val pubDateTimestamp: Long? = 0,
    val reliability: Double? = 0.0,
    val title: String? = "",
    val topImage: String? = "",
    val aiRate: Double? = 0.0,
    val evaluationMessage: String? = ""
) : Parcelable {
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}