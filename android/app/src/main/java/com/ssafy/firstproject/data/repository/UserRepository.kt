package com.ssafy.firstproject.data.repository

import com.ssafy.firstproject.data.model.request.LogoutRequest
import com.ssafy.firstproject.data.model.request.ProfileEditRequest
import com.ssafy.firstproject.data.model.request.User
import com.ssafy.firstproject.data.model.response.AuthResponse
import com.ssafy.firstproject.data.model.response.DuplicateCheckResponse
import com.ssafy.firstproject.data.model.response.ProfileEditResponse
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import retrofit2.Response

class UserRepository {

    private val userService = RetrofitUtil.userService

    suspend fun refreshToken(refreshToken: String): Response<AuthResponse> {
        return userService.refreshToken(refreshToken)
    }

    suspend fun signUp(user: User): Response<AuthResponse> {
        return userService.signUp(user)
    }

    suspend fun login(user: User): Response<AuthResponse> {
        return userService.login(user)
    }

    suspend fun logout(logoutRequest: LogoutRequest): Response<AuthResponse> {
        return userService.logout(logoutRequest)
    }

    suspend fun checkId(id: String): Response<DuplicateCheckResponse> {
        return userService.checkUserName(id)
    }

    suspend fun checkNickname(nickname: String): Response<DuplicateCheckResponse> {
        return userService.checkNIckName(nickname)
    }

    suspend fun updateUserInfo(profileEditRequest: ProfileEditRequest): Response<ProfileEditResponse> {
        return userService.updateUserInfo(profileEditRequest)
    }
}