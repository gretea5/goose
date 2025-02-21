package com.ssafy.firstproject.data.source.local

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(context: Context) {

    companion object {
        private const val SHARED_PREFERENCES_NAME = "shared_preferences"
        private const val ACCESS_TOKEN_KEY_NAME = "access_token"
        private const val REFRESH_TOKEN_KEY_NAME = "refresh_token"
    }

    private var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addAccessToken(accessToken: String) {
        val editor = preferences.edit()
        editor.putString(ACCESS_TOKEN_KEY_NAME, accessToken)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return preferences.getString(ACCESS_TOKEN_KEY_NAME, null)
    }

    fun removeAccessToken() {
        val editor = preferences.edit()
        editor.remove(ACCESS_TOKEN_KEY_NAME)
        editor.apply()
    }

    fun addRefreshToken(refreshToken: String) {
        val editor = preferences.edit()
        editor.putString(REFRESH_TOKEN_KEY_NAME, refreshToken)
        editor.apply()
    }

    fun getRefreshToken(): String? {
        return preferences.getString(REFRESH_TOKEN_KEY_NAME, null)
    }

    fun removeRefreshToken() {
        val editor = preferences.edit()
        editor.remove(REFRESH_TOKEN_KEY_NAME)
        editor.apply()
    }

    fun checkLogin(): Boolean {
        return getAccessToken() != null // 로그인 사용자 : true, 비로그인 사용자 : false
    }
}