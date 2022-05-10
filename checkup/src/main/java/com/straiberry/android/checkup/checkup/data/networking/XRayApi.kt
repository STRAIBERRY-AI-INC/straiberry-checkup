package com.straiberry.android.checkup.checkup.data.networking

import com.straiberry.android.checkup.checkup.data.networking.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface XRayApi {

    @POST("${API_VERSION}checkups/checkup/")
    suspend fun createCheckup(
        @Body createCheckupRequest: CreateCheckupRequest,
        @HeaderMap header: Map<String, String?>
    )
            : CreateCheckupSuccessResponse

    @Multipart
    @POST("${API_VERSION}checkups/checkup-image/")
    suspend fun addImageToXrayCheckup(
        @Part checkupId: MultipartBody.Part,
        @Part imageType: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @Part lastImage: MultipartBody.Part,
        @HeaderMap header: Map<String, String?>
    )
            : AddImageToCheckupSuccessResponse

    @POST("${API_VERSION}checkups/check-xray-url/")
    suspend fun checkXrayUrl(
        @Body checkXrayUrlRequest: CheckXrayUrlRequest,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToCheckupSuccessResponse

    @POST("${API_VERSION}checkups/checkup-image/")
    suspend fun addXrayImage(
        @Body addXrayImageRequest: AddXrayImageRequest,
        @HeaderMap header: Map<String, String?>
    )
            : AddImageToCheckupSuccessResponse
}