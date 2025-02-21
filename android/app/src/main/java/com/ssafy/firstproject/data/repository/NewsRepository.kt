package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.data.model.response.NewsArticle
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class NewsRepository {

    private val newsService = RetrofitUtil.newsService

    suspend fun getNewsList(): Response<List<NewsArticle>> {
        return newsService.getNewsList()
    }

    suspend fun getSearchNewsList(keyword: String): Response<List<NewsArticle>> {
        return newsService.getSearchNewsList(keyword)
    }

    suspend fun getNewsArticle(newsId: String): Response<NewsAnalysisArticle> {
        return newsService.getNewsArticle(newsId)
    }
}