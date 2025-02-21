package com.ssafy.firstproject.ui.game.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.FakeNews
import kotlinx.coroutines.launch

private const val TAG = "GameViewModel"

class GameViewModel : ViewModel() {

    private val _fakeNews: MutableLiveData<FakeNews> = MutableLiveData()
    val fakeNews: LiveData<FakeNews> get() = _fakeNews

    init {
        getFakeNews()
    }

    private fun getFakeNews() {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.gameRepository.getFakeNews()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _fakeNews.value = it
                    }
                    Log.d(TAG, "getFakeNews: ${response.body()}")
                } else {
                    Log.d(TAG, "getFakeNews fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getFakeNews: ${it.message}", it)
            }
        }
    }
}