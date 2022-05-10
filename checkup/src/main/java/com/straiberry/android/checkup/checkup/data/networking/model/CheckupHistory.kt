package com.straiberry.android.checkup.checkup.data.networking.model

import com.google.gson.annotations.SerializedName

data class CheckupHistorySuccessResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("data")
    val `data`: List<Data> = listOf(),
    @SerializedName("message")
    val message: String = ""
) {
    data class Data(
        @SerializedName("checkup_type")
        val checkupType: String = "",
        @SerializedName("complete")
        val complete: Boolean = false,
        @SerializedName("created_at")
        val createdAt: String = "",
        @SerializedName("id")
        val id: String = "",
        @SerializedName("images")
        val images: List<Image> = listOf(),
        @SerializedName("overal_score")
        val overalScore: String = "",
        @SerializedName("overal_score_value")
        val overalScoreValue: Int = 0,
        @SerializedName("description")
        val description: Description = Description(),
        @SerializedName("whitening_score")
        val whiteningScore: Any = Any(),
        @SerializedName("updated_at")
        val updatedAt: String = "",
        @SerializedName("user")
        val user: String = ""
    ) {
        data class Description(
            @SerializedName("fa")
            val persian: String = "",
            @SerializedName("en")
            val english: String = "",
        )

        data class Image(
            @SerializedName("checkup")
            val checkup: String = "",
            @SerializedName("created_at")
            val createdAt: String = "",
            @SerializedName("id")
            val id: Int = 0,
            @SerializedName("image")
            val image: String = "",
            @SerializedName("image_type")
            val imageType: String = "",
            @SerializedName("result")
            val result: List<Result> = listOf(),
            @SerializedName("updated_at")
            val updatedAt: String = ""
        ) {
            data class Result(
                @SerializedName("checkup_image")
                val checkupImage: Int = 0,
                @SerializedName("created_at")
                val createdAt: String = "",
                @SerializedName("id")
                val id: Int = 0,
                @SerializedName("oral_hygien_score")
                val oralHygienScore: Int = 0,
                @SerializedName("problems")
                val problems: List<Problems> = listOf(),
                @SerializedName("updated_at")
                val updatedAt: String = "",
                @SerializedName("whitening_score")
                val whiteningScore: Int = 0
            )

            data class Problems(
                @SerializedName("cavity_class")
                val cavityClass: Int,
                @SerializedName("conf")
                val conf: Double,
                @SerializedName("h")
                val h: Double,
                @SerializedName("tooth_class")
                val toothClass: List<String> = listOf(),
                @SerializedName("w")
                val w: Double,
                @SerializedName("x_center")
                val xCenter: Double,
                @SerializedName("y_center")
                val yCenter: Double
            )
        }

        class Teeth
    }
}