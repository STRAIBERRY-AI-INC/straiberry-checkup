package com.straiberry.android.checkup.checkup.data.networking.model

import com.google.gson.annotations.SerializedName

data class CheckupResultSuccessResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("data")
    val `data`: CheckupHistorySuccessResponse.Data = CheckupHistorySuccessResponse.Data(),
    @SerializedName("message")
    val message: String = ""
)