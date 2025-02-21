package com.ssafy.firstproject.ui.gameresultdetail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass.Companion.gameRepository
import com.ssafy.firstproject.data.model.response.GameResultDetailResponse
import kotlinx.coroutines.launch

private const val TAG = "GameResultDetailViewModel"

class GameResultDetailViewModel : ViewModel() {

    private val _gameResultDetailResponse = MutableLiveData<GameResultDetailResponse>()
    val gameResultDetailResponse: LiveData<GameResultDetailResponse> get() = _gameResultDetailResponse

    fun getGameResultDetail(newsId: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                gameRepository.getGameResultDetail(newsId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _gameResultDetailResponse.value = it
                    }
                    Log.d(TAG, "getGameStatistics: ${response.body()}")
                } else {
                    Log.d(TAG, "getGameStatistics: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getGameStatistics: ${it.message}", it)
            }
        }
    }
}