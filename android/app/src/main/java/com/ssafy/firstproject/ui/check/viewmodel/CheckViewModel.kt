package com.ssafy.firstproject.ui.check.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.request.SpellCheckRequest
import com.ssafy.firstproject.data.model.response.SpellCheckResponse
import kotlinx.coroutines.launch

private const val TAG = "CheckViewModel"
class CheckViewModel : ViewModel() {
    private val _spellCheckedText: MutableLiveData<SpellCheckResponse> = MutableLiveData()
    val spellCheckedText: LiveData<SpellCheckResponse> get() = _spellCheckedText

    fun getSpellCheckedText(spellCheckRequest: SpellCheckRequest) {
        viewModelScope.launch {
            runCatching {
                ApplicationClass.spellCheckRepository.getSpellCheckedText(spellCheckRequest)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _spellCheckedText.value = it
                    }
                    Log.d(TAG, "getSpellCheckedText: ${response.body()}")
                } else {
                    Log.d(TAG, "getSpellCheckedText fail: ${response.code()}")
                }
            }.onFailure {
                Log.d(TAG, "getSpellCheckedText: ${it.message}, $it")
            }
        }
    }
}