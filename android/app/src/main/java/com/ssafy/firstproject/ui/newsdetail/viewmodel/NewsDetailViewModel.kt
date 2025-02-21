package com.ssafy.firstproject.ui.newsdetail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import kotlinx.coroutines.launch

private const val TAG = "NewsDetailViewModel"

class NewsDetailViewModel : ViewModel() {

    private val _newsArticle: MutableLiveData<NewsAnalysisArticle> = MutableLiveData()
    val newsArticle: LiveData<NewsAnalysisArticle> get() = _newsArticle

    fun getNewsArticle(newsId: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.newsRepository.getNewsArticle(newsId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _newsArticle.value = it
                    }
                    Log.d(TAG, "getNewsArticle: ${response.body()}")
                } else {
                    Log.d(TAG, "getNewsArticle: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getNewsArticle: ${it.message}", it)
            }
        }
    }
}