package com.ssafy.firstproject.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtils {
    fun formatDateYYMMDD(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun convertPubDateToFormattedDate(pubDate: String): String {
        // 원본 pubDate의 형식
        val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        // 원하는 출력 형식
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)

        // 시간대 설정
        inputFormat.timeZone = TimeZone.getTimeZone("GMT+9") // 한국 시간대 적용

        // 변환
        val date = inputFormat.parse(pubDate)
        return outputFormat.format(date!!)
    }
}