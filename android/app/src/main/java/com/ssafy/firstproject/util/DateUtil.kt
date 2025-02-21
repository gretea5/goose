package com.ssafy.firstproject.util

object DateUtil {
    fun parseDate(dateArray: List<Int?>): String {
        val year = dateArray[0]?.rem(100) // 2025 -> 25 변환
        val month = dateArray[1]
        val day = dateArray[2]

        return "%02d-%02d-%02d".format(year, month, day)
    }
}