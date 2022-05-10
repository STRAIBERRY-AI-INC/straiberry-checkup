package com.straiberry.android.checkup.checkup.data.mapper

import com.straiberry.android.checkup.checkup.data.networking.model.*
import com.straiberry.android.checkup.checkup.domain.model.*

fun SdkTokenSuccessResponse.toDomainModel() = SdkTokenSuccessModel(this.refresh!!, this.access!!)
fun CreateCheckupSuccessResponse.toDomainModel() =
    CreateCheckupSuccessModel(checkupId = this.data?.id!!)

fun DeleteCheckupSuccessResponse.toDomainModel() =
    DeleteCheckupSuccessModel(isSuccess = true)

fun AddImageToCheckupSuccessResponse.toDomainModel() =
    AddImageToCheckupSuccessModel(
        imageId = this.data?.id!!,
        jawType = this.data.imageType?.toInt()!!
    )

fun CheckupHistorySuccessResponse.toDomainModel() =
    CheckupHistorySuccessModel(this.data)

fun CheckupResultSuccessResponse.toDomainModel() =
    CheckupResultSuccessModel(this.data)

fun UpdateImageInCheckupSuccessModel.toDomainModel() =
    UpdateImageInCheckupSuccessModel(true)

fun AddToothToCheckupSuccessResponse.toDomainModel() =
    AddToothToCheckupSuccessModel(1)

fun AddToothToDentalIssueSuccessResponse.toDomainModel() =
    AddToothToDentalIssueSuccessModel(this.data.id)

fun UpdateToothInCheckupSuccessResponse.toDomainModel() =
    UpdateToothInCheckupSuccessModel(true)

fun DeleteToothInCheckupSuccessResponse.toDomainModel() =
    DeleteToothInCheckupSuccessModel(true)