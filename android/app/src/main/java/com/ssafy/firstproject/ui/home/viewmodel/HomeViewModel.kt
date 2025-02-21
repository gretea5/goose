package com.ssafy.firstproject.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.NewsArticle
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel : ViewModel() {

    private val _newsList: MutableLiveData<List<NewsArticle>> = MutableLiveData()
    val newsList: LiveData<List<NewsArticle>> get() = _newsList

    init {
        getNewsList()
    }

    private fun getNewsList() {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.newsRepository.getNewsList()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _newsList.value = it
                    }
                    Log.d(TAG, "getNewsList: ${response.body()}")
                } else {
                    Log.d(TAG, "getNewsList fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getNewsList: ${it.message}", it)
            }
        }
    }
}