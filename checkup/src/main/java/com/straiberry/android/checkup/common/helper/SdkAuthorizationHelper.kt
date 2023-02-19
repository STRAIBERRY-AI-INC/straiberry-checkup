package com.straiberry.android.checkup.common.helper

import android.content.Context
import com.straiberry.android.checkup.BuildConfig
import com.straiberry.android.common.helper.Prefs

private const val SDK_ACCESS_TOKEN = "SdkAccessToken"
private const val SDK_REFRESH_TOKEN = "SdkRefreshToken"

class SdkAuthorizationHelper(private val context: Context) {
    fun setHeaders(): MutableMap<String, String?> {
        val header: MutableMap<String, String?> = HashMap()
        header["Authorization"] = "Bearer " + StraiberryCheckupSdkInfo.getToken()
        header["Connection"] = "close"
        header["os"] = OS_TYPE
        header["lang"] = language()
        return header
    }

    fun setToken(context: Context, access: String, refresh: String) {
        Prefs.putString(context, SDK_ACCESS_TOKEN, access)
        Prefs.putString(context, SDK_REFRESH_TOKEN, refresh)
    }

    /**
     * @return current language that user is applying
     * 0 -> Farsi
     * 1 -> English
     */
    fun language(): String {
        val language = Prefs.getString(context, SelectedLanguage) ?: ""

        return when {
            language.isEmpty() && BuildConfig.IS_FARSI -> "0"
            language.isNotEmpty() -> {
                if (language == "fa")
                    "0"
                else
                    "1"
            }
            else -> "1"
        }

    }

    companion object {
        private const val OS_TYPE = "0"
        private const val SelectedLanguage = "SelectedLanguage"
    }
}

