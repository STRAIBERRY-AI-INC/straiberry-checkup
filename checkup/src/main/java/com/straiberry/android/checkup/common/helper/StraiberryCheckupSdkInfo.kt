package com.straiberry.android.checkup.common.helper

import io.paperdb.Paper

data class TokenKeys(val appId: String = "", val packageName: String = "")
object StraiberryCheckupSdkInfo {
    private const val CHECKUP_SDK_INFO = "CheckupSdkInfo"
    private const val DISPLAY_NAME = "DisplayName"
    private const val CHECKUP_LANGUAGE = "CheckupLanguage"
    private const val USER_AVATAR = "UserAvatar"
    private const val UNIQUE_ID = "UniqueId"
    private const val TOKEN = "token"
    fun setDisplayName(displayName: String) {
        Paper.book().write(DISPLAY_NAME, displayName)
    }

    fun getDisplayName(): String = Paper.book().read(DISPLAY_NAME) ?: ""

    fun setTokenInfo(appId: String, packageName: String) {
        Paper.book().write(CHECKUP_SDK_INFO, TokenKeys(appId, packageName))
    }

    fun setToken(accessToken: String) {
        Paper.book().write(TOKEN, accessToken)
    }

    fun getToken(): String = Paper.book().read(TOKEN)!!

    fun getTokenInfo(): TokenKeys = Paper.book().read(CHECKUP_SDK_INFO)!!

    fun setUserAvatar(imageUrl: String) {
        Paper.book().write(USER_AVATAR, imageUrl)
    }

    fun getUserAvatar(): String = Paper.book().read(USER_AVATAR) ?: ""

    fun setUniqueId(uniqueId: String) {
        Paper.book().write(UNIQUE_ID, uniqueId)
    }

    fun getUniqueId(): String = Paper.book().read(UNIQUE_ID)!!

    fun setSelectedLanguage(language: String) {
        Paper.book().write(CHECKUP_LANGUAGE, language)
    }

    fun getSelectedLanguage(): String = Paper.book().read(CHECKUP_LANGUAGE)!!
}