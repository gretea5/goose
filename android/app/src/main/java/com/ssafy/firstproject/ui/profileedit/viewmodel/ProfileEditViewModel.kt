package com.ssafy.firstproject.ui.profileedit.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.data.model.request.ProfileEditRequest
import com.ssafy.firstproject.data.model.response.DuplicateCheckResponse
import com.ssafy.firstproject.data.model.response.ProfileEditResponse
import kotlinx.coroutines.launch

private const val TAG = "ProfileEditViewModel"

class ProfileEditViewModel : ViewModel() {

    private val _isPasswordValid = MutableLiveData<Boolean>()
    val isPasswordValid: LiveData<Boolean> get() = _isPasswordValid

    private val _isPasswordMatch = MutableLiveData<Boolean>()
    val isPasswordMatch: LiveData<Boolean> get() = _isPasswordMatch

    private val _isNicknameValid = MutableLiveData<Boolean>()
    val isNicknameValid: LiveData<Boolean> get() = _isNicknameValid

    private val _isNicknameDuplicate = MutableLiveData<DuplicateCheckResponse>()
    val isNicknameDuplicate: LiveData<DuplicateCheckResponse> get() = _isNicknameDuplicate

    private val _isAllValid = MutableLiveData<Boolean>()
    val isAllValid: LiveData<Boolean> get() = _isAllValid

    fun checkPasswordValidation(password: String) {
        _isPasswordValid.value = password.matches("^[a-z0-9]{4,12}$".toRegex())
    }

    fun checkPasswordMatch(password: String, confirmPassword: String) {
        _isPasswordMatch.value = password == confirmPassword
    }

    fun checkNickNameValidation(nickname: String) {
        _isNicknameValid.value = nickname.matches("^[가-힣a-zA-Z0-9]{2,12}$".toRegex())
    }

    fun setNicknameDuplicateFalse() {
        _isNicknameDuplicate.value = DuplicateCheckResponse(available = false, "")
    }

    fun checkAllValidation() {
        _isAllValid.value = (isPasswordValid.value == true &&
                isPasswordMatch.value == true &&
                isNicknameValid.value == true &&
                isNicknameDuplicate.value?.available == true)
    }

    fun checkNicknameDuplicate(nickname: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.userRepository.checkNickname(nickname)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _isNicknameDuplicate.value = it
                    }
                    Log.d(TAG, "checkNicknameDuplicate: ${response.body()}")
                } else {
                    Log.d(TAG, "checkNicknameDuplicate fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "checkNicknameDuplicate: ${it.message}", it)
            }
        }
    }

    private val _profileEditResponse: MutableLiveData<ProfileEditResponse> = MutableLiveData()
    val profileEditResponse: LiveData<ProfileEditResponse> get() = _profileEditResponse

    fun updateProfile(profileEditRequest: ProfileEditRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                ApplicationClass.userRepository.updateUserInfo(profileEditRequest)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _profileEditResponse.value = it
                    }
                    Log.d(TAG, "updateProfile: ${response.body()}")
                } else {
                    Log.d(TAG, "updateProfile fail: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "updateProfile: ${it.message}", it)
            }
        }
    }
}