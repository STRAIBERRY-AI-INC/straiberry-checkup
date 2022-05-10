package com.straiberry.android.checkup.checkup.data.networking

import com.straiberry.android.checkup.checkup.data.networking.model.*
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
import okhttp3.MultipartBody
import retrofit2.http.*

const val API_VERSION = "api/v1/"

interface CheckupApi {

    @DELETE("${API_VERSION}checkups/{id}/")
    suspend fun deleteCheckup(
        @Path("id") checkupId: String,
        @HeaderMap header: Map<String, String?>
    )
            : DeleteCheckupSuccessResponse

    @GET("${API_VERSION}checkups/{id}/")
    suspend fun getCheckup(
        @Path("id") checkupId: String,
        @HeaderMap header: Map<String, String?>
    )
            : CheckupResultSuccessResponse

    @Multipart
    @POST("${API_VERSION}checkups/checkup-image/")
    suspend fun addImageToCheckup(
        @Part checkupId: MultipartBody.Part,
        @Part imageType: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @Part lastImage: MultipartBody.Part,
        @HeaderMap header: Map<String, String?>
    )
            : AddImageToCheckupSuccessResponse

    @Multipart
    @PATCH("${API_VERSION}checkups/checkup-image/{id}/")
    suspend fun updateImageInCheckup(
        @Path("id") imageId: Int,
        @Part checkupId: MultipartBody.Part,
        @Part imageType: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @HeaderMap header: Map<String, String?>
    )
            : UpdateImageInCheckupSuccessModel

    @POST("${API_VERSION}checkups/checkup-tooth/")
    suspend fun addToothToCheckup(
        @Body addToothToCheckupRequest: AddToothToCheckupRequest,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToCheckupSuccessResponse

    @POST("${API_VERSION}get-token/")
    suspend fun getSDKToken(
        @Body getSDKTokenRequest: GetSDKTokenRequest
    )
            : SdkTokenSuccessResponse

    @POST("${API_VERSION}checkups/bulk-tooth/")
    suspend fun addSeveralToothToCheckup(
        @Body addSeveralTeethToCheckup: AddSeveralTeethToCheckup,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToCheckupSuccessResponse

    @PATCH("${API_VERSION}checkups/checkup-tooth/{id}/")
    suspend fun updateToothInCheckup(
        @Path("id") toothId: Int,
        @Body addToothToCheckupRequest: AddToothToCheckupRequest,
        @HeaderMap header: Map<String, String?>
    )
            : UpdateToothInCheckupSuccessResponse

    @DELETE("${API_VERSION}checkups/checkup-tooth/{id}/")
    suspend fun deleteToothInCheckup(
        @Path("id") toothId: Int,
        @HeaderMap header: Map<String, String?>
    )
            : DeleteToothInCheckupSuccessResponse

    @GET("${API_VERSION}checkups/all/")
    suspend fun checkupHistory(
        @Query("page") page: Int,
        @HeaderMap header: Map<String, String?>
    )
            : CheckupHistorySuccessResponse

    @POST("${API_VERSION}users/profile/add-dentalissues/")
    suspend fun addDentalIssue(
        @Body addDentalIssueRequest: AddDentalIssueRequest,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToDentalIssueSuccessResponse

    @DELETE("${API_VERSION}users/profile/manage-dentalissues/{id}/")
    suspend fun deleteDentalIssue(
        @Path("id") dentalIssueId: Int,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToCheckupSuccessResponse

    @PATCH("${API_VERSION}users/profile/manage-dentalissues/{id}/")
    suspend fun updateDentalIssue(
        @Path("id") dentalIssueId: Int,
        @Body addDentalIssueRequest: AddDentalIssueRequest,
        @HeaderMap header: Map<String, String?>
    )
            : AddToothToDentalIssueSuccessResponse

    @POST("${API_VERSION}checkups/checkup/")
    suspend fun createCheckup(
        @Body createCheckupRequest: CreateCheckupRequest,
        @HeaderMap header: Map<String, String?>
    )
            : CreateCheckupSuccessResponse
}