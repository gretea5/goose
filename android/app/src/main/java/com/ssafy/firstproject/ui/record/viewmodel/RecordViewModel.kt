package com.ssafy.firstproject.ui.record.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.UserNews
import kotlinx.coroutines.launch

private const val TAG = "RecordViewModel"

class RecordViewModel : ViewModel() {

    private val _userNewsList: MutableLiveData<List<UserNews>> = MutableLiveData()
    val userNewsList: LiveData<List<UserNews>> get() = _userNewsList

    init {
        getUserNewsList()
    }

    private fun getUserNewsList() {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.userNewsRepository.getUserNewsList()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userNewsList.value = it
                    }
                    Log.d(TAG, "getUserNewsList: ${response.body()}")
                } else {
                    Log.d(TAG, "getUserNewsList fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getUserNewsList: ${it.message}", it)
            }
        }
    }
}