package com.ssafy.firstproject.ui.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.data.model.response.UserGrowth
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "UserGrowthViewModel"

class UserGrowthViewModel : ViewModel() {

    private val _userGrowthData = MutableLiveData<UserGrowth?>()
    val userGrowthData: LiveData<UserGrowth?> get() = _userGrowthData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchUserGrowthData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // API 호출
                val response = RetrofitUtil.userGrowthService.getUserGrowth()

                if (response.isSuccessful) {
                    if (response.code() == 204) {
                        // No content
                        _errorMessage.postValue("데이터가 없습니다.")
                    } else {
                        // 응답 본문 처리
                        val userGrowth = response.body()
                        if (userGrowth != null) {
                            _userGrowthData.postValue(userGrowth)
                        } else {
                            _errorMessage.postValue("데이터가 없습니다.")
                        }
                    }
                    Log.d(TAG, "fetchUserGrowthData: ${response.body()}")
                } else {
                    // API 실패 처리
                    _errorMessage.postValue("API 오류가 발생했습니다. 상태 코드: ${response.code()}")
                    Log.d(TAG, "fetchUserGrowthData: ${response.code()}")
                }
            } catch (e: Exception) {
                // 네트워크 또는 기타 예외 처리
                _errorMessage.postValue("네트워크 오류가 발생했습니다.")
                Log.e(TAG, "fetchUserGrowthData: ${e.message}", e)
            }
        }
    }
}