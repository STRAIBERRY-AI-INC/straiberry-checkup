package com.straiberry.android.checkup.common.helper

import android.content.Context
import com.straiberry.android.common.helper.Prefs

private const val SDK_ACCESS_TOKEN = "SdkAccessToken"
private const val SDK_REFRESH_TOKEN = "SdkRefreshToken"

object SdkAuthorizationHelper {
    fun setHeaders(context: Context): MutableMap<String, String?> {
        val header: MutableMap<String, String?> = HashMap()
        header["Authorization"] = "Bearer " + StraiberryCheckupSdkInfo.getToken()
        header["Connection"] = "close"
        return header
    }

    fun setToken(context: Context, access: String, refresh: String) {
        Prefs.putString(context, SDK_ACCESS_TOKEN, access)
        Prefs.putString(context, SDK_REFRESH_TOKEN, refresh)
    }
}