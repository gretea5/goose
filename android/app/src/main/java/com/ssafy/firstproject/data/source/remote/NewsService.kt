package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.data.model.response.NewsArticle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface NewsService {

    @GET("news/list")
    suspend fun getNewsList(): Response<List<NewsArticle>>

    @GET("news/search")
    suspend fun getSearchNewsList(
        @Query("keyword") keyword: String
    ): Response<List<NewsArticle>>

    @GET("news/{newsId}")
    suspend fun getNewsArticle(
        @Path("newsId") newsId: String
    ): Response<NewsAnalysisArticle>
}