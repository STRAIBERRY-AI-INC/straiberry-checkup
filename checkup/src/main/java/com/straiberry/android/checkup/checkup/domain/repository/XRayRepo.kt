package com.straiberry.android.checkup.checkup.domain.repository

import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.CreateCheckupSuccessModel
import okhttp3.MultipartBody

interface XRayRepo {
    suspend fun createCheckup(checkupType: Int): CreateCheckupSuccessModel
    suspend fun addImageToCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part,
    ): AddImageToCheckupSuccessModel

    suspend fun checkXrayUrl(xrayUrl: String)
    suspend fun addXrayImage(
        checkupId: String,
        imageUrl: String
    ): AddImageToCheckupSuccessModel
}