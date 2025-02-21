package com.ssafy.firstproject.ui.gameresult.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass.Companion.gameRepository
import com.ssafy.firstproject.data.model.response.GameResultResponse
import kotlinx.coroutines.launch

private const val TAG = "GameResultViewModel"

class GameResultViewModel : ViewModel() {

    private val _gameResult = MutableLiveData<GameResultResponse>()
    val gameResult: LiveData<GameResultResponse> get() = _gameResult

    fun getGameResult(newsId: String) {
        viewModelScope.launch {
            runCatching {
                gameRepository.getGameResult(newsId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _gameResult.value = it
                    }
                    Log.d(TAG, "getGameResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getGameResult fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getGameResult: ${it.message}", it)
            }
        }
    }
}