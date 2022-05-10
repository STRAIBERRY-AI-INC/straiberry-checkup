package com.straiberry.android.checkup.checkup.data.networking.model

import com.google.gson.annotations.SerializedName

data class AddImageToCheckupRequest(
    @SerializedName("checkup")
    val checkupId: String? = null,
    @SerializedName("image_type")
    val imageType: Int? = null,
)

data class AddImageToCheckupSuccessResponse(
    @SerializedName("code")
    val code: String?,
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?
) {
    data class Data(
        @SerializedName("checkup")
        val checkup: String?,
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("id")
        val id: Int?,
        @SerializedName("image")
        val image: String?,
        @SerializedName("image_type")
        val imageType: String?,
        @SerializedName("result")
        val result: Any?,
        @SerializedName("updated_at")
        val updatedAt: String?
    )
}

data class UpdateImageInCheckupSuccessResponse(
    @SerializedName("checkup")
    val checkupId: String? = null
)