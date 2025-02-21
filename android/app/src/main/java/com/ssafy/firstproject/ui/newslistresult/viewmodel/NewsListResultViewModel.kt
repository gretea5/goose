package com.ssafy.firstproject.ui.newslistresult.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.request.KeywordSearch
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import kotlinx.coroutines.launch

private const val TAG = "NewsListResultViewModel_ssafy"

class NewsListResultViewModel : ViewModel() {
    private val _newsAnalysisArticles: MutableLiveData<List<NewsAnalysisArticle>> =
        MutableLiveData()
    val newsAnalysisArticles: LiveData<List<NewsAnalysisArticle>> get() = _newsAnalysisArticles

    fun getNewsArticle(keywordSearch: KeywordSearch) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.contentSearchRepository.searchKeywords(keywordSearch)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _newsAnalysisArticles.value = it
                    }
                    Log.d(TAG, "getNewsArticle: ${response.body()}")
                } else {
                    Log.d(TAG, "getNewsArticle fail: ${response.code()}")
                }

            }.onFailure {
                Log.e(TAG, "getNewsArticle: ${it.message}", it)
            }
        }
    }

}