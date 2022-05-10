package com.straiberry.android.checkup.checkup.data.networking.model

import com.google.gson.annotations.SerializedName

data class AddToothToCheckupRequest(
    @SerializedName("tooth_number")
    val toothNumber: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("cause")
    val cause: Int? = null,
    @SerializedName("pain")
    val pain: Int? = null,
    @SerializedName("checkup")
    val checkupId: String? = null
)

data class CheckXrayUrlRequest(
    @SerializedName("url")
    val url: String? = null
)

data class AddXrayImageRequest(
    @SerializedName("checkup")
    val checkupId: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("image_type")
    val imageType: String? = "2",
    @SerializedName("last_image")
    val lastImage: String? = "1"
)

data class GetSDKTokenRequest(
    @SerializedName("app_id")
    val appId: String? = null,
    @SerializedName("package_name")
    val packageName: String? = null
)

data class AddDentalIssueRequest(
    @SerializedName("tooth_number")
    val toothNumber: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("cause")
    val cause: Int? = null,
    @SerializedName("pain")
    val pain: Int? = null
)

data class AddSeveralTeethToCheckup(
    @SerializedName("unique_field")
    val uniqueId: String? = null,
    @SerializedName("checkup")
    val checkupId: String? = null,
    @SerializedName("data")
    val data: ArrayList<AddToothToCheckupRequest>? = null,
)

data class AddToothToCheckupSuccessResponse(
    @SerializedName("tooth_number")
    val toothNumber: Int? = null
)

data class AddToothToDentalIssueSuccessResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("data")
    val `data`: Data = Data(),
    @SerializedName("message")
    val message: String = ""
) {
    data class Data(
        @SerializedName("cause")
        val cause: String = "",
        @SerializedName("created_at")
        val createdAt: String = "",
        @SerializedName("duration")
        val duration: String = "",
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("pain")
        val pain: String = "",
        @SerializedName("tooth_number")
        val toothNumber: String = "",
        @SerializedName("updated_at")
        val updatedAt: String = "",
        @SerializedName("user")
        val user: String = ""
    )
}

data class UpdateToothInCheckupSuccessResponse(
    @SerializedName("tooth_number")
    val toothNumber: Int? = null
)

data class DeleteToothInCheckupSuccessResponse(
    @SerializedName("tooth_number")
    val toothNumber: Int? = null
)