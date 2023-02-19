package com.straiberry.android.checkup.checkup.domain.repository

import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.*
import okhttp3.MultipartBody

interface CheckupSdkRepo {
    suspend fun getSDKToken(appId: String, packageName: String): SdkTokenSuccessModel
    suspend fun getCheckup(checkupId: String): CheckupResultSuccessModel
    suspend fun createCheckup(checkupType: CheckupType): CreateCheckupSuccessModel
    suspend fun checkupHistory(page: Int): CheckupHistorySuccessModel
    suspend fun addImageToCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part,
    ): AddImageToCheckupSuccessModel

    suspend fun updateImageInCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        imageId: Int
    ): UpdateImageInCheckupSuccessModel

    suspend fun addSeveralToothToCheckup(
        checkupId: String,
        data: ArrayList<AddToothToCheckupRequest>
    ): AddToothToCheckupSuccessModel
}