package com.straiberry.android.checkup.checkup.domain.repository

import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.*
import okhttp3.MultipartBody

interface CheckupRepo {
    suspend fun getSDKToken(appId: String, packageName: String): SdkTokenSuccessModel
    suspend fun deleteCheckup(checkupId: String): DeleteCheckupSuccessModel
    suspend fun getCheckup(checkupId: String): CheckupResultSuccessModel
    suspend fun createCheckup(checkupType: CheckupType): CreateCheckupSuccessModel
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

    suspend fun addToothToCheckup(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
        checkupId: String
    ): AddToothToCheckupSuccessModel

    suspend fun addSeveralToothToCheckup(
        checkupId: String,
        data: ArrayList<AddToothToCheckupRequest>
    ): AddToothToCheckupSuccessModel

    suspend fun updateToothInCheckup(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
        checkupId: String,
        toothId: Int
    ): UpdateToothInCheckupSuccessModel

    suspend fun deleteToothInCheckup(toothId: Int): DeleteToothInCheckupSuccessModel

    suspend fun checkupHistory(page: Int): CheckupHistorySuccessModel

    suspend fun addDentalIssue(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
    ): AddToothToDentalIssueSuccessModel

    suspend fun deleteDentalIssue(dentalId: Int): AddToothToCheckupSuccessModel


    suspend fun updateDentalIssue(
        toothId: Int,
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
    ): AddToothToDentalIssueSuccessModel
}