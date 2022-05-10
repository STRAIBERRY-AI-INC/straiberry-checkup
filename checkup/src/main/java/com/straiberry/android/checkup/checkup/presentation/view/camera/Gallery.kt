package com.straiberry.android.checkup.checkup.presentation.view.camera

interface Gallery {
    fun insertImageFromGalleryBasedOnJaw(detectedJaw: String)
    fun showInsertedImageBasedOnSelectedJaws()
}