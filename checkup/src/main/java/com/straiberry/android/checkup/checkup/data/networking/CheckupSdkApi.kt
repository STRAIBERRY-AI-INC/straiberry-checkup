package com.straiberry.android.checkup.checkup.data.networking

import com.straiberry.android.checkup.checkup.data.networking.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface CheckupSdkApi {
    @POST("${API_VERSION}checkup/")
    suspend fun createCheckup(
        @Body createCheckupRequest: CreateCheckupRequest,
        @HeaderMap header: Map<String, String?>
    )
            : CreateCheckupSuccessResponse

    @GET("${API_VERSION}checkup/all/")
    suspend fun checkupHistory(
        @Query("page") page: Int,
        @Query("unique_field") uniqueId: String,
        @HeaderMap header: Map<String, String?>
    )
            : CheckupHistorySuccessResponse

    @POST("${API_VERSION}get-token/")
    suspend fun getSDKToken(
        @Body getSDKTokenRequest: GetSDKTokenRequest
    )
            : SdkTokenSuccessResponse


    @Multipart
    @POST("${API_VERSION}checkup-image/")
    suspend fun addImageToCheckup(
        @Part checkupId: MultipartBody.Part,
        @Part imageType: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @Part lastImage: MultipartBody.Part,
        @HeaderMap header: Map<String, String?>
    )
            : AddImageToCheckupSuccessResponse


    @GET("${API_VERSION}checkup/{id}/")
    suspend fun getCheckup(
        @Path("id") checkupId: String,
        @Query("unique_field") uniqueId: String,
        @HeaderMap header: Map<String, String?>
    )
            : CheckupResultSuccessResponse

    @POST("${API_VERSION}bulk-tooth/")
    suspend fun addSeveralToothToCheckup(
        @Body addSeveralTeethToCheckup: AddSeveralTeethToCheckup,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToCheckupSuccessResponse
}