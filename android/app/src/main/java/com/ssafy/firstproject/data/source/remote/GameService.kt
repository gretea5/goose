package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.request.UserChoiceRequest
import com.ssafy.firstproject.data.model.response.FakeNews
import com.ssafy.firstproject.data.model.response.GameResultResponse
import com.ssafy.firstproject.data.model.response.GameResultDetailResponse
import com.ssafy.firstproject.data.model.response.UserChoiceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GameService {

    @GET("fake-news/random")
    suspend fun getFakeNews(): Response<FakeNews>

    @POST("game-result/submit")
    suspend fun submitUserChoice(
        @Body userChoiceRequest: UserChoiceRequest
    ): Response<UserChoiceResponse>

    @GET("statistics/news")
    suspend fun getGameResultDetail(
        @Query("newsId") newsId: String
    ): Response<GameResultDetailResponse>

    @GET("statistics/game-result")
    suspend fun getGameResult(
        @Query("newsId") newsId: String
    ): Response<GameResultResponse>
}