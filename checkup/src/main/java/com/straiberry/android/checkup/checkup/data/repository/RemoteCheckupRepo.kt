package com.straiberry.android.checkup.checkup.data.repository

import android.content.Context
import com.straiberry.android.checkup.checkup.data.mapper.toDomainModel
import com.straiberry.android.checkup.checkup.data.networking.CheckupApi
import com.straiberry.android.checkup.checkup.data.networking.model.*
import com.straiberry.android.checkup.checkup.domain.model.*
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo
import com.straiberry.android.checkup.common.helper.SdkAuthorizationHelper
import com.straiberry.android.checkup.common.helper.StraiberryCheckupSdkInfo
import okhttp3.MultipartBody

class RemoteCheckupRepo(
    private val checkupApi: CheckupApi,
    private val context: Context,
    private val authorizationHelper: SdkAuthorizationHelper
) : CheckupRepo {

    override suspend fun createCheckup(
        checkupType: CheckupType
    ): CreateCheckupSuccessModel =
        checkupApi.createCheckup(
            createCheckupRequest = CreateCheckupRequest(
                checkupType = checkupType
            ),
            header = authorizationHelper.setHeaders()
        ).toDomainModel()

    override suspend fun getSDKToken(appId: String, packageName: String) =
        checkupApi.getSDKToken(
            GetSDKTokenRequest(appId, packageName)
        ).toDomainModel()


    /**
     * Delete a specific checkup based on checkup id
     */
    override suspend fun deleteCheckup(checkupId: String): DeleteCheckupSuccessModel =
        checkupApi.deleteCheckup(checkupId, authorizationHelper.setHeaders()).toDomainModel()

    /** Get a specific checkup result */
    override suspend fun getCheckup(checkupId: String): CheckupResultSuccessModel =
        checkupApi.getCheckup(checkupId, authorizationHelper.setHeaders()).toDomainModel()

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
        checkupApi.addImageToCheckup(
            checkupId = checkupId,
            imageType = imageType,
            image = image,
            lastImage = lastImage,
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
        checkupApi.updateImageInCheckup(
            imageId = imageId,
            checkupId = checkupId,
            imageType = imageType,
            image = image,
            header = authorizationHelper.setHeaders()
        ).toDomainModel()

    /**
     * Adding selected tooth to checkup plus answered questions.
     */
    override suspend fun addToothToCheckup(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
        checkupId: String
    ): AddToothToCheckupSuccessModel =
        checkupApi.addToothToCheckup(
            addToothToCheckupRequest = AddToothToCheckupRequest(
                toothNumber = toothNumber,
                duration = duration,
                cause = cause,
                pain = pain,
                checkupId = checkupId
            ), header = authorizationHelper.setHeaders()
        ).toDomainModel()

    /**
     * Adding multiple teeth to checkup
     */
    override suspend fun addSeveralToothToCheckup(
        checkupId: String,
        data: ArrayList<AddToothToCheckupRequest>
    ) = checkupApi.addSeveralToothToCheckup(
        addSeveralTeethToCheckup = AddSeveralTeethToCheckup(
            uniqueId = StraiberryCheckupSdkInfo.getUniqueId(),
            checkupId = checkupId, data = data
        ), header = authorizationHelper.setHeaders()
    ).toDomainModel()

    /**
     * Update a specific tooth based on user edit's
     */
    override suspend fun updateToothInCheckup(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
        checkupId: String,
        toothId: Int
    ): UpdateToothInCheckupSuccessModel =
        checkupApi.updateToothInCheckup(
            toothId = toothId,
            addToothToCheckupRequest = AddToothToCheckupRequest(
                toothNumber = toothNumber,
                duration = duration,
                cause = cause,
                pain = pain,
                checkupId = checkupId
            ),
            header = authorizationHelper.setHeaders()
        ).toDomainModel()

    /**
     * Delete a tooth from checkup.
     */
    override suspend fun deleteToothInCheckup(toothId: Int): DeleteToothInCheckupSuccessModel =
        checkupApi.deleteToothInCheckup(toothId, authorizationHelper.setHeaders())
            .toDomainModel()

    /**
     * Getting all created checkups
     */
    override suspend fun checkupHistory(page: Int): CheckupHistorySuccessModel =
        checkupApi.checkupHistory(page, authorizationHelper.setHeaders()).toDomainModel()

    override suspend fun addDentalIssue(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ): AddToothToDentalIssueSuccessModel = checkupApi.addDentalIssue(
        AddDentalIssueRequest(toothNumber, duration, cause, pain),
        authorizationHelper.setHeaders()
    ).toDomainModel()

    override suspend fun deleteDentalIssue(dentalId: Int): AddToothToCheckupSuccessModel =
        checkupApi.deleteDentalIssue(dentalId, authorizationHelper.setHeaders())
            .toDomainModel()

    override suspend fun updateDentalIssue(
        toothId: Int,
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ): AddToothToDentalIssueSuccessModel = checkupApi.updateDentalIssue(
        toothId,
        AddDentalIssueRequest(toothNumber, duration, cause, pain),
        authorizationHelper.setHeaders()
    ).toDomainModel()
}