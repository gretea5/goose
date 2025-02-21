package com.ssafy.firstproject.ui.search.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.response.NewsArticle
import kotlinx.coroutines.launch

private const val TAG = "SearchViewModel"

class SearchViewModel : ViewModel() {

    private val _newsList: MutableLiveData<List<NewsArticle>> = MutableLiveData()
    val newsList: LiveData<List<NewsArticle>> get() = _newsList

    fun getSearchNewsList(keyword: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.newsRepository.getSearchNewsList(keyword)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _newsList.value = it
                    }
                    Log.d(TAG, "getSearchNewsList: ${response.body()}")
                } else {
                    Log.d(TAG, "getSearchNewsList fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getSearchNewsList: ${it.message}", it)
            }
        }
    }

}