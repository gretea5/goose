package com.ssafy.firstproject.ui.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import com.ssafy.firstproject.data.model.request.KeywordSearch
import com.ssafy.firstproject.data.model.request.LogoutRequest
import kotlinx.coroutines.launch

private const val TAG = "LogoutDialogViewModel"

class LogoutDialogViewModel : ViewModel() {

    private val _isLogoutSuccess = MutableLiveData<Boolean>()
    val isLogoutSuccess: LiveData<Boolean> get() = _isLogoutSuccess

    fun logout() {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.userRepository.logout(
                    LogoutRequest(
                        sharedPreferencesUtil.getRefreshToken()!!
                    )
                )
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _isLogoutSuccess.value = it.success
                    }
                    Log.d(TAG, "logout: ${response.body()}")
                } else {
                    _isLogoutSuccess.value = false
                    Log.d(TAG, "logout fail: ${response.code()}")
                }
            }.onFailure {
                _isLogoutSuccess.value = false
                Log.e(TAG, "logout: ${it.message}", it)
            }
        }
    }
}