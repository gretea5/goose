package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.request.UserChoiceRequest
import com.ssafy.firstproject.data.model.response.FakeNews
import com.ssafy.firstproject.data.model.response.GameResultResponse
import com.ssafy.firstproject.data.model.response.GameResultDetailResponse
import com.ssafy.firstproject.data.model.response.UserChoiceResponse
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class GameRepository {

    private val gameService = RetrofitUtil.gameService

    suspend fun getFakeNews(): Response<FakeNews> {
        return gameService.getFakeNews()
    }

    suspend fun submitUserChoice(userChoiceRequest: UserChoiceRequest): Response<UserChoiceResponse> {
        return gameService.submitUserChoice(userChoiceRequest)
    }

    suspend fun getGameResultDetail(newsId: String): Response<GameResultDetailResponse> {
        return gameService.getGameResultDetail(newsId)
    }

    suspend fun getGameResult(newsId: String): Response<GameResultResponse> {
        return gameService.getGameResult(newsId)
    }
}