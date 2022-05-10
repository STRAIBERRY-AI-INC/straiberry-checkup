package com.straiberry.android.checkup.checkup.presentation.view.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

interface Detector {
    fun startCamera()

    /** Takes a photo when model detects a jaw **/
    fun takePhoto(currentDetectedJaw: String)

    /**
     * Check the captured image again with detection model
     * to see if it's complete. if not then show an error.
     * @param imageProxy from cameraX
     * @param capturedImage converted image proxy to bitmap
     */
    fun analyzeCapturedImage(
        currentDetectedJaw: String,
        imageProxy: ImageProxy,
        capturedImage: Bitmap
    )

    /** @return current uploaded jaw id **/
    fun getCurrentJawId(): Int

    /** Upload or update images in checkup **/
    fun addImageToCheckup()

    /**
     * Show error for incorrect captured jaw.
     * @param error message of error
     * @param isMirror gets a boolean for making a textview mirroring
     */
    fun showError(error: String, isMirror: Boolean = true)

    /**
     * Setting up the next jaw that user should expect based on selected jaw
     */
    fun setupNextJaw()

    /**
     * Start an animation to change the position between captured jaw
     * and inserted image. Inserted image is a bottom sheet that opens up
     * when jaw captured is successful.
     */
    fun startCapturedImageTransaction()

    /**
     * Rest and move all view for jaw animation to first position
     */
    fun resetCapturedImageTransaction()

    /**
     * Save the bitmap of current captured jaw.
     * @param savedBitmap a bitmap of current captured jaw.
     */
    fun saveCapturedJaws(savedBitmap: Bitmap)

    /**
     * Save last captured jaw. We will use this for animations
     * after capturing a jaw.
     * @param lastCapturedJaw a bitmap of latest captured jaw.
     */
    fun setLastCapturedJaw(lastCapturedJaw: Bitmap)

    /** @return a bitmap of last captured jaw **/
    fun getLastCapturedJaw(): Bitmap

    /** Setup the first jaw sample that should show on page based on user selection. */
    fun setupSelectedJawsBasedOnUserSelection()

    /**
     * Figure out the correct position to move captured jaw animation.
     * There is three position: upper jaw, lower jaw and front teeth. This
     * positions are in a bottom sheet that opens up when captured jaw is successful.
     */
    fun selectCorrectPositionForCapturedImage()

    /**
     * Save next jaw that user should captured.
     * For example if user captured front jaw then next jaw should
     * be upper jaw.
     * @param nextDetectedJaw a string of next jaw.(front,upper,lower)
     */
    fun setNextDetectedJaw(nextDetectedJaw: String?)

    /** @return a string of next jaw **/
    fun getNextDetectedJaw(): String?

    /** Show the dialog box of choose/capture image **/
    fun showLayoutChoosePhoto()

    /** Hide the dialog box of choose/capture image **/
    fun hideLayoutChoosePhoto()

    /** Stop detecting **/
    fun stopDetectionModel()

    /** Start detecting **/
    fun startDetectionModel()

    /** @return a boolean of detecting status **/
    fun getDetectionModelState(): Boolean

    /** Save current detected jaw.
     * @param currentDetectionJaw a string of current jaw.(front,upper,lower)
     * @param isMainThread if current function is called in a main thread
     */
    fun setCurrentDetectedJaw(currentDetectionJaw: String, isMainThread: Boolean = false)

    /** @return a string of current detected jaw **/
    fun getCurrentDetectedJaw(): String

    fun enableCapturedJaw()

    /**
     * When model detects a jaw from camera a pulse animation
     * must appear on screen that's also includes a vibrate and
     * a one second waiting animation.
     * @param currentDetectedJaw detected jaw
     */
    fun showPulseAnimation(currentDetectedJaw: String)

    fun checkoutNextDetectionJaw()

    /**
     * Show image positions on insert image dialog based on
     * selected jaws by user.
     * See (res/layout/camera_insert_image.xml)
     */
    fun showCapturedImageBasedOnSelectedJaw()

    /**
     * Show all images on insert image dialog that user
     * must upload for current checkup.
     * See (res/layout/camera_insert_image.xml)
     */
    fun showImagesThatUserMustCapture()

    /**
     * When model detected a jaw then we take a photo.
     * @param detectedJawLabel a string of current detected jaw.(front,upper,lower)
     */
    fun takePhotoFromDetectedJaw(detectedJawLabel: String)

    /**
     * When whitening checkup is selected then all
     * inserted image must be remove except front jaw.
     * See (res/layout/camera_insert_image.xml)
     */
    fun hideCapturedImageForWhiteningCheckup()

    /**
     * Show all inserted jaw.(front,upper,lower)
     * See (res/layout/camera_insert_image.xml)
     */
    fun showAllCapturedJaw()

    /** Clear animation and inserted image view */
    fun clearView()

    /**
     * Upload an image into checkup
     * @param image a bitmap of captured jaw.
     */
    fun uploadJaw(image: Bitmap)

    /**
     * Update an image in checkup
     * @param image a bitmap of captured jwa
     * @param imageId id of uploaded image
     */
    fun updateJaw(image: Bitmap, imageId: Int)

    /**
     * save uploaded image id and hide uploaded
     * jaw progress bar.
     * @param imageId id of uploaded image
     * @param jawType (front,upper,lower)
     */
    fun setUploadedImageId(imageId: Int, jawType: Int)

    /** When user select the last photo for checkup, we show a waiting bar */
    fun showWaitingBarForGettingResult()

    /** Getting checkup result and send user to result page */
    fun getCheckupResult()

    fun checkoutNecessaryUploadedImage()
}