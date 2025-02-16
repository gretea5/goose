package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.request.KeywordSearch
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ContentSearchService {
    @POST("content/keywords-search")
    suspend fun searchKeywords(@Body keywordSearch: KeywordSearch): Response<List<NewsAnalysisArticle>>
}