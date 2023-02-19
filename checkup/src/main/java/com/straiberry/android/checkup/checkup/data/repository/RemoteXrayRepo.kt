package com.straiberry.android.checkup.checkup.data.repository

import android.content.Context
import com.straiberry.android.checkup.checkup.data.mapper.toDomainModel
import com.straiberry.android.checkup.checkup.data.networking.XRayApi
import com.straiberry.android.checkup.checkup.data.networking.model.AddXrayImageRequest
import com.straiberry.android.checkup.checkup.data.networking.model.CheckXrayUrlRequest
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.data.networking.model.CreateCheckupRequest
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.CreateCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo
import com.straiberry.android.checkup.common.helper.SdkAuthorizationHelper
import okhttp3.MultipartBody

class RemoteXrayRepo(
    private val xRayApi: XRayApi,
    private val context: Context,
    private val authorizationHelper: SdkAuthorizationHelper
) : XRayRepo {
    override suspend fun createCheckup(checkupType: CheckupType): CreateCheckupSuccessModel =
        xRayApi.createCheckup(
            CreateCheckupRequest(checkupType),
            authorizationHelper.setHeaders()
        ).toDomainModel()

    override suspend fun addImageToCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part
    ): AddImageToCheckupSuccessModel =
        xRayApi.addImageToXrayCheckup(
            checkupId,
            image,
            imageType,
            lastImage,
            authorizationHelper.setHeaders()
        )
            .toDomainModel()

    override suspend fun checkXrayUrl(xrayUrl: String) {
        xRayApi.checkXrayUrl(CheckXrayUrlRequest(xrayUrl), authorizationHelper.setHeaders())
    }

    override suspend fun addXrayImage(
        checkupId: String,
        imageUrl: String
    ): AddImageToCheckupSuccessModel =
        xRayApi.addXrayImage(
            AddXrayImageRequest(checkupId, imageUrl),
            authorizationHelper.setHeaders()
        ).toDomainModel()

}