package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.response.UserNews
import retrofit2.Response
import retrofit2.http.GET

interface UserNewsService {

    @GET("content/my-news-analysis")
    suspend fun getUserNewsList() : Response<List<UserNews>>

}

