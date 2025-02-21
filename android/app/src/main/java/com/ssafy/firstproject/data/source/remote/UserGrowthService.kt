package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.response.UserGrowth
import retrofit2.Response
import retrofit2.http.GET

interface UserGrowthService {
    @GET("statistics/mypage")
    suspend fun getUserGrowth() : Response<UserGrowth>
}