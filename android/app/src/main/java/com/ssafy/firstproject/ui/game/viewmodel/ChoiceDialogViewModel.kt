package com.ssafy.firstproject.ui.game.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass.Companion.gameRepository
import com.ssafy.firstproject.data.model.request.UserChoiceRequest
import com.ssafy.firstproject.data.model.response.UserChoiceResponse
import kotlinx.coroutines.launch

private const val TAG = "ChoiceDialogViewModel"

class ChoiceDialogViewModel : ViewModel() {

    private val _userChoiceResult = MutableLiveData<UserChoiceResponse>()
    val userChoiceResult: LiveData<UserChoiceResponse> get() = _userChoiceResult

    fun submitUserChoice(userChoiceRequest: UserChoiceRequest) {
        viewModelScope.launch {
            runCatching {
                gameRepository.submitUserChoice(userChoiceRequest)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userChoiceResult.value = it
                    }
                    Log.d(TAG, "submitUserChoice: ${response.body()}")
                } else {
                    Log.d(TAG, "submitUserChoice fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "submitUserChoice: ${it.message}", it)
            }
        }
    }
}