package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.response.UserNews
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class UserNewsRepository {
    private val userNewsService = RetrofitUtil.userNewsService

    suspend fun getUserNewsList() : Response<List<UserNews>> {
        return userNewsService.getUserNewsList()
    }
}


