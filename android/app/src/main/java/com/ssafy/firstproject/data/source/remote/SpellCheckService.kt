package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.response.SpellCheckResponse
import com.ssafy.firstproject.data.model.request.SpellCheckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SpellCheckService {
    @POST("correct")
    suspend fun getSpellCheck(@Body spellCheckRequest: SpellCheckRequest) : Response<SpellCheckResponse>
}