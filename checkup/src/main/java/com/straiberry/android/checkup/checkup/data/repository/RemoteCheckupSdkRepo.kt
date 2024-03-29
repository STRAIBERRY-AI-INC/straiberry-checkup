package com.straiberry.android.checkup.checkup.data.repository

import android.content.Context
import com.straiberry.android.checkup.checkup.data.mapper.toDomainModel
import com.straiberry.android.checkup.checkup.data.networking.CheckupSdkApi
import com.straiberry.android.checkup.checkup.data.networking.model.*
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
        checkupType: CheckupType
    ): CreateCheckupSuccessModel =
        checkupSdkApi.createCheckup(
            createCheckupRequest = CreateCheckupRequest(
                checkupType = checkupType,
                displayName = StraiberryCheckupSdkInfo.getDisplayName(),
                uniqueId = StraiberryCheckupSdkInfo.getUniqueId()
            ),
            header = authorizationHelper.setHeaders()
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
            header = authorizationHelper.setHeaders()
        ).toDomainModel()

    /**
     * Adding multiple teeth to checkup
     */
    override suspend fun addSeveralToothToCheckup(
        checkupId: String,
        data: ArrayList<AddToothToCheckupRequest>
    ) = checkupSdkApi.addSeveralToothToCheckup(
        addSeveralTeethToCheckup = AddSeveralTeethToCheckup(
            checkupId = checkupId, data = data
        ), uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
         header = authorizationHelper.setHeaders()
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
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            header = authorizationHelper.setHeaders()
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
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            header = authorizationHelper.setHeaders()
        ).toDomainModel()

    /**
     * Getting all created checkups
     */
    override suspend fun checkupHistory(page: Int): CheckupHistorySuccessModel =
        checkupSdkApi.checkupHistory(
            page = page,
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            header = authorizationHelper.setHeaders()
        ).toDomainModel()
}