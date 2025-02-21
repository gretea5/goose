package com.ssafy.firstproject.data.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserChoiceResponse(
    val correct: Boolean,
    val dwellTime: Long,
    val id: Int,
    val newsId: String,
    val nickname: String,
    val solvedAt: List<Int>,
    val userChoice: String,
    val correctAnswer: String
) : Parcelable