package com.straiberry.android.checkup.checkup.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.launch

class UserInfoViewModel : ViewModel() {
    private val _userAvatarAsBitmap = MutableLiveData<Bitmap?>()
    val userAvatarAsBitmap: LiveData<Bitmap?> = _userAvatarAsBitmap

    fun setUserAvatar(context: Context, imageUrl: String) {
        viewModelScope.launch {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .transformations(CircleCropTransformation())
                .build()
            if (loader.execute(request) is SuccessResult) {
                val result = (loader.execute(request) as SuccessResult).drawable
                _userAvatarAsBitmap.value = (result as BitmapDrawable).bitmap
            } else
                _userAvatarAsBitmap.value = null
        }
    }

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    fun setUserName(userName: String) {
        _userName.value = userName
    }


    init {
        _userName.value = ""
    }
}