package com.straiberry.android.checkup.checkup.domain.model

import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import java.io.File

enum class CheckupLanguage { Persian, English }
data class SdkTokenSuccessModel(val refresh: String, val access: String)
data class AddImageToCheckupSuccessModel(val imageId: Int, val jawType: Int)
data class UpdateImageInCheckupSuccessModel(val isSuccess: Boolean)
data class AddToothToCheckupSuccessModel(val toothId: Int)
data class AddToothToDentalIssueSuccessModel(val toothId: Int)
data class UpdateToothInCheckupSuccessModel(val isSuccess: Boolean)
data class DeleteToothInCheckupSuccessModel(val isSuccess: Boolean)
data class CheckupHistorySuccessModel(val `data`: List<CheckupHistorySuccessResponse.Data?>?)
data class CheckupResultSuccessModel(val data: CheckupHistorySuccessResponse.Data)
data class CreateCheckupSuccessModel(val checkupId: String)
data class CheckupSdkInfoModel(
    val appId: String = "",
    val packageName: String = "",
    val displayName: String = "",
    val language: CheckupLanguage = CheckupLanguage.English
)

data class AddSeveralToothToCheckup(
    val toothNumber: Int,
    val duration: Int,
    val cause: Int,
    val pain: Int,
    val checkupId: String
)

data class UpdateImageInCheckup(val checkupId: String, val image: File, val imageType: Int)
data class DeleteCheckupSuccessModel(val isSuccess: Boolean)
data class DentalIssueQuestionsModel(
    val remoteTeethId: Int = 0,
    val teethId: Int,
    val answerOneIndex: Int,
    val answerTwoIndex: Int,
    val answerThreeIndex: Int
)