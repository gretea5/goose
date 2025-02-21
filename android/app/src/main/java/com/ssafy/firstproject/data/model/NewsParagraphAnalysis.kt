package com.ssafy.firstproject.data.model

data class NewsParagraphAnalysis(
    val paragraph: String?, // 단락 내용
    val paragraphReliability: Double?, // 신뢰도 (퍼센트)
    val paragraphReason: String? // 분석 이유
)