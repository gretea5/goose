package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.data.model.request.LogoutRequest
import com.ssafy.firstproject.data.model.request.ProfileEditRequest
import com.ssafy.firstproject.data.model.request.User
import com.ssafy.firstproject.data.model.response.AuthResponse
import com.ssafy.firstproject.data.model.response.DuplicateCheckResponse
import com.ssafy.firstproject.data.model.response.ProfileEditResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {

    @POST("users/refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<AuthResponse>

    @POST("users/signup")
    suspend fun signUp(@Body user: User): Response<AuthResponse>

    @POST("users/login")
    suspend fun login(@Body user: User): Response<AuthResponse>

    @POST("users/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<AuthResponse>

    @GET("users/check-username")
    suspend fun checkUserName(@Query("username") id: String): Response<DuplicateCheckResponse>

    @GET("users/check-nickname")
    suspend fun checkNIckName(@Query("nickname") nickname: String): Response<DuplicateCheckResponse>

    @PUT("users/update")
    suspend fun updateUserInfo(@Body profileEditRequest: ProfileEditRequest): Response<ProfileEditResponse>
}