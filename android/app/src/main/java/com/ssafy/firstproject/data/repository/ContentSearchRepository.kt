package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.request.KeywordSearch
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class ContentSearchRepository {

    private val contentSearchService = RetrofitUtil.contentSearchService

    suspend fun searchKeywords(
        keywordSearch: KeywordSearch
    ): Response<List<NewsAnalysisArticle>> {
        return contentSearchService.searchKeywords(keywordSearch)
    }

    suspend fun searchByUrl(url: String): Response<NewsAnalysisArticle> {
        return contentSearchService.searchByUrl(url)
    }
}