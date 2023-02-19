package com.straiberry.android.checkup.checkup.data.networking.model

import com.google.gson.annotations.SerializedName

data class CreateCheckupRequest(
    @SerializedName("checkup_type")
    val checkupType: CheckupType? = null,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("unique_field")
    val uniqueId: String? = null
)

enum class CheckupType {
    @SerializedName("-1")
    Regular,

    @SerializedName("0")
    Whitening,

    @SerializedName("1")
    Sensitivity,

    @SerializedName("2")
    Treatments,

    @SerializedName("3")
    Others,

    @SerializedName("4")
    XRays,
}

data class SdkTokenSuccessResponse(
    @SerializedName("access_token")
    val access: String? = null,
    @SerializedName("refresh_token")
    val refresh: String? = null
)

data class CreateCheckupSuccessResponse(
    @SerializedName("code")
    val code: String?,
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?
) {
    data class Data(
        @SerializedName("checkup_type")
        val checkupType: CheckupType?,
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("id")
        val id: String?,
        @SerializedName("images")
        val images: List<Any?>?,
        @SerializedName("teeth")
        val teeth: List<Any?>?,
        @SerializedName("updated_at")
        val updatedAt: String?,
        @SerializedName("user")
        val user: String?
    )
}

data class DeleteCheckupSuccessResponse(
    @SerializedName("checkup_type")
    val checkupType: Int? = null
)
