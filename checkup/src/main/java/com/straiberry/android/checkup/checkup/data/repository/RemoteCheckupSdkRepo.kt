package com.straiberry.android.checkup.checkup.data.repository

import android.content.Context
import com.straiberry.android.checkup.checkup.data.mapper.toDomainModel
import com.straiberry.android.checkup.checkup.data.networking.CheckupSdkApi
import com.straiberry.android.checkup.checkup.data.networking.model.AddSeveralTeethToCheckup
import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.data.networking.model.CreateCheckupRequest
import com.straiberry.android.checkup.checkup.data.networking.model.GetSDKTokenRequest
import com.straiberry.android.checkup.checkup.domain.model.*
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo
import com.straiberry.android.checkup.common.helper.SdkAuthorizationHelper
import com.straiberry.android.checkup.common.helper.StraiberryCheckupSdkInfo
import okhttp3.MultipartBody

class RemoteCheckupSdkRepo(
    private val checkupSdkApi: CheckupSdkApi,
    private val context: Context,
    private val authorizationHelper: SdkAuthorizationHelper
) : CheckupSdkRepo {

    override suspend fun createCheckup(
        checkupType: Int
    ): CreateCheckupSuccessModel =
        checkupSdkApi.createCheckup(
            createCheckupRequest = CreateCheckupRequest(
                checkupType = checkupType,
                displayName = StraiberryCheckupSdkInfo.getDisplayName(),
                uniqueId = StraiberryCheckupSdkInfo.getUniqueId()
            ),
            header = authorizationHelper.setHeaders(context)
        ).toDomainModel()

    override suspend fun getSDKToken(appId: String, packageName: String) =
        checkupSdkApi.getSDKToken(
            GetSDKTokenRequest(appId, packageName)
        ).toDomainModel()

    /** Get a specific checkup result */
    override suspend fun getCheckup(checkupId: String): CheckupResultSuccessModel =
        checkupSdkApi.getCheckup(
            checkupId = checkupId,
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            header = authorizationHelper.setHeaders(context)
        ).toDomainModel()

    /**
     * Adding multiple teeth to checkup
     */
    override suspend fun addSeveralToothToCheckup(
        checkupId: String,
        data: ArrayList<AddToothToCheckupRequest>
    ) = checkupSdkApi.addSeveralToothToCheckup(
        addSeveralTeethToCheckup = AddSeveralTeethToCheckup(
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            checkupId = checkupId, data = data
        ), header = authorizationHelper.setHeaders(context)
    ).toDomainModel()

    /**
     * Adding image's of selected jaw to checkup. user can upload
     * three image.
     */
    override suspend fun addImageToCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part
    ): AddImageToCheckupSuccessModel =
        checkupSdkApi.addImageToCheckup(
            checkupId = checkupId,
            imageType = imageType,
            image = image,
            lastImage = lastImage,
            header = authorizationHelper.setHeaders(context)
        ).toDomainModel()

    /**
     * Update a specific image in checkup based on checkup id
     * and uploaded image id.
     */
    override suspend fun updateImageInCheckup(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        imageId: Int
    ): UpdateImageInCheckupSuccessModel =
        checkupSdkApi.updateImageInCheckup(
            imageId = imageId,
            checkupId = checkupId,
            imageType = imageType,
            image = image,
            header = authorizationHelper.setHeaders(context)
        ).toDomainModel()

    /**
     * Getting all created checkups
     */
    override suspend fun checkupHistory(page: Int): CheckupHistorySuccessModel =
        checkupSdkApi.checkupHistory(
            page = page,
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            header = authorizationHelper.setHeaders(context)
        ).toDomainModel()
}