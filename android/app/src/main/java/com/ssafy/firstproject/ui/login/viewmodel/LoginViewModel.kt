package com.ssafy.firstproject.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import com.ssafy.firstproject.data.model.request.User
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel : ViewModel() {

    private val _isLoginSuccess = MutableLiveData<Boolean>()
    val isLoginSuccess: LiveData<Boolean>
        get() = _isLoginSuccess

    fun login(user: User) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.userRepository.login(user)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _isLoginSuccess.value = it.success
                        if (it.success) {
                            sharedPreferencesUtil.addAccessToken(it.accessToken)
                            sharedPreferencesUtil.addRefreshToken(it.refreshToken)
                        }
                    }
                    Log.d(TAG, "login: ${response.body()}")
                } else {
                    _isLoginSuccess.value = false
                    Log.d(TAG, "login fail: ${response.code()}")
                }
            }.onFailure {
                _isLoginSuccess.value = false
                Log.e(TAG, "login: ${it.message}", it)
            }
        }
    }
}