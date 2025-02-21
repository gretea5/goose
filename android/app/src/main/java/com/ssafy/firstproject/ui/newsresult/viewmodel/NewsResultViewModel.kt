package com.ssafy.firstproject.ui.newsresult.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import kotlinx.coroutines.launch

private const val TAG = "NewsResultViewModel_ssafy"

class NewsResultViewModel : ViewModel() {
    private val _newsAnalysisResult = MutableLiveData<NewsAnalysisArticle>()
    val newsAnalysisResult: LiveData<NewsAnalysisArticle> get() = _newsAnalysisResult

    fun searchByUrl(url: String) {
        viewModelScope.launch {
            runCatching {
                ApplicationClass.contentSearchRepository.searchByUrl(url)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _newsAnalysisResult.value = it
                    }
                    Log.d(TAG, "searchByUrl: ${response.body()}")
                }
                Log.d(TAG, "searchByUrl: ${response.code()}")
            }.onFailure {
                Log.d(TAG, "searchByUrl: ${it.message} $it")
            }
        }
    }

    fun setNewsArticle(newsAnalysisArticle: NewsAnalysisArticle) {
        _newsAnalysisResult.value = newsAnalysisArticle
    }
}