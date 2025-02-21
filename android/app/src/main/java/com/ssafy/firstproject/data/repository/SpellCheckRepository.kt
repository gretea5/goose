package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.response.SpellCheckResponse
import com.ssafy.firstproject.data.model.request.SpellCheckRequest
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class SpellCheckRepository {
    private val correctableService = RetrofitUtil.spellCheckService

    suspend fun getSpellCheckedText(spellCheckRequest: SpellCheckRequest) : Response<SpellCheckResponse> {
        return correctableService.getSpellCheck(spellCheckRequest)
    }
}