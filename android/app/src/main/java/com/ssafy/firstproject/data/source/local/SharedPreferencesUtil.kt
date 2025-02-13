package com.ssafy.firstproject.data.source.local

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(context: Context) {

    companion object {
        private const val SHARED_PREFERENCES_NAME = "shared_preferences"
        private const val ACCESS_TOKEN_KEY_NAME = "access_token"
    }

    private var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addAccessToken(accessToken: String) {
        val editor = preferences.edit()
        editor.putString(ACCESS_TOKEN_KEY_NAME, accessToken)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return preferences.getString(ACCESS_TOKEN_KEY_NAME, "")
    }

    fun removeAccessToken() {
        val editor = preferences.edit()
        editor.remove(ACCESS_TOKEN_KEY_NAME)
        editor.apply()
    }
}