package com.straiberry.android.checkup.checkup.presentation.view.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.straiberry.android.checkup.BuildConfig
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.checkup.presentation.viewmodel.*
import com.straiberry.android.checkup.common.extentions.*
import com.straiberry.android.checkup.common.helper.VibratorHelper
import com.straiberry.android.checkup.databinding.FragmentCameraBinding
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.features.support.BottomSheetOnlineSupport
import com.straiberry.android.common.model.JawPosition
import com.straiberry.android.common.tflite.Classifier
import com.straiberry.android.common.tflite.DetectorFactory
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors


open class JawDetector : Fragment(), Detector, Gallery {


    lateinit var binding: FragmentCameraBinding

    // CameraX variables
    private lateinit var preview: Preview
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    // View models
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val checkupResultViewModel by viewModel<CheckupResultViewModel>()
    val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    val jawDetectionViewModel by activityViewModels<DetectionJawViewModel>()
    val checkupSubmitImageViewModel by viewModel<CheckupSubmitImageViewModel>()
    val guideTourViewModel by viewModel<CheckupGuideTourViewModel>()
    val checkupHelpViewModel by viewModel<CheckupHelpViewModel>()

    // Positions of current captured jaw to insert it in right place
    private lateinit var cardViewForCurrentCapturedJaw: CardView
    private lateinit var textViewForCurrentCapturedJaw: TextView
    private lateinit var layoutForCurrentCapturedJaw: View
    private lateinit var layoutForNextCapturedJaw: View
    private lateinit var imageViewForCurrentCapturedJaw: ImageView

    // Save uploaded image id for each jaw
    var uploadedUpperJawId = -1
    var uploadedLowerJawId = -1
    var uploadedFrontJawId = -1

    // Check if a jaw image is uploading
    var frontJawIsUploading = false
    var lowerJawIsUploading = false
    var upperJawIsUploading = false

    var lastImage = 0

    // List of jaws that user selected for checkup
    val listOfSelectedJaws = ArrayList<String>()
    val cameraXTargetResolution = Size(1024, 1024)

    override fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()

            // Initializing the jaw detection model
            val detector = DetectorFactory.getDetector(
                requireContext().assets,
                MODEL_NAME
            )
            imageAnalyzer = ImageAnalysis.Builder()
                // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                // 2. go to the latest frame and may drop some frame. The default is 2.
                // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(
                        cameraExecutor
                    ) { imageProxy ->
                        if (getDetectionModelState()) {
                            val image = imageProxy.image

                            val frameAsBitmap = image!!.toBitmap()!!
                                .correctRotation(imageProxy.imageInfo.rotationDegrees)
                                .cropFromCenter()
                                .resize(detector.inputSize, detector.inputSize)

                            val results: ArrayList<Classifier.Recognition?> =
                                detector.recognizeImage(frameAsBitmap)
                            val detectedJaw =
                                results.firstOrNull { it!!.confidence!! > ImageAnalyzer.MinimumConfidence }

                            // Convert the model location to number between 1 and 0
                            if (detectedJaw != null) {
                                val left = detectedJaw.getLocation().left / imageProxy.width
                                val top = detectedJaw.getLocation().top / imageProxy.height
                                val right = detectedJaw.getLocation().right / imageProxy.width
                                val bottom =
                                    detectedJaw.getLocation().bottom / imageProxy.height
                                val location = detectedJaw.getLocation()

                                // Check the margin threshold between detected jaw location. This will avoid
                                // to detect uncompleted jaw.
                                if (left > MARGIN_THRESHOLD &&
                                    top > MARGIN_THRESHOLD &&
                                    (1 - right) > MARGIN_THRESHOLD &&
                                    (1 - bottom) > MARGIN_THRESHOLD
                                )
                                // Early exit: if prediction is not good enough, don't report it
                                    if (detectedJaw.confidence!! >= MinimumConfidence) {
                                        jawDetectionViewModel.updateRecognition(
                                            Recognition(
                                                detectedJaw.title!!,
                                                detectedJaw.confidence!!,
                                                location,
                                                frameAsBitmap
                                            )
                                        )
                                    }
                            }
                            imageProxy.close()
                        } else
                            imageProxy.close()
                    }

                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetResolution(cameraXTargetResolution)
                .setTargetRotation(binding.camera.display.rotation)
                .setFlashMode(ImageCapture.FLASH_MODE_ON)
                .build()

            // Select camera, back is the default. If it is not available, choose front camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, imageCapture, preview, imageAnalyzer
                )

                camera.cameraControl.cancelFocusAndMetering()
                // Attach the preview to preview view, aka View Finder
                preview.setSurfaceProvider(binding.camera.surfaceProvider)
            } catch (exc: Exception) {
                //Firebase.crashlytics.recordException(exc)
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun takePhoto(currentDetectedJaw: String) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Take a picture and stop detection model. After image is
        // taken then save cropped image to gallery
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val capturedBitmap =
                        image.toBitmap()!!.correctRotation(image.imageInfo.rotationDegrees)
                    // Check captured image with detection model
                    analyzeCapturedImage(
                        currentDetectedJaw = currentDetectedJaw,
                        imageProxy = image,
                        capturedImage = capturedBitmap
                    )
                    // Stop model
                    stopDetectionModel()
                    jawDetectionViewModel.resetRecognition()
                    // Close resource
                    image.close()
                }
            })
    }


    override fun analyzeCapturedImage(
        currentDetectedJaw: String,
        imageProxy: ImageProxy,
        capturedImage: Bitmap
    ) {
        // Checking for blurriness and darkness of captured picture
        try {
            // Analyze captured image. When captured image is not
            // one of selected image for checkup then we show an error. If prediction in captured
            // image is not good then show an error. Otherwise add image to checkup.
            ImageAnalyzer(
                context = requireContext(),
                bitmap = capturedImage,
                checkingFrame = false
            ) { label, _, imageIsCorrect, _, finalImage ->
                when {
                    capturedImage.calculateBrightnessEstimate(5) ->
                        showError(
                            getString(R.string.this_image_is_too_dark_please_take_another_one)
                        )
                    capturedImage.isBlurry() ->
                        showError(
                            getString(
                                R.string.this_image_have_a_low_qulity_please_retake_again,
                            )
                        )
                    // Captured image is not one of selected jaws
                    !listOfSelectedJaws.contains(label) -> {
                        showError(
                            getString(
                                R.string.the_selected_picture_is_incorrect,
                                getNextDetectedJaw()
                            )
                        )
                        if (BuildConfig.IS_FARSI)
                            BottomSheetOnlineSupport().show(childFragmentManager, "")
                    }
                    imageIsCorrect -> try {
                        setupNextJaw()
                        currentCapturedImage = finalImage
                        setLastCapturedJaw(finalImage!!)
                        saveCapturedJaws(finalImage)
                        startCapturedImageTransaction()
                        addImageToCheckup()
                    } catch (e: Exception) {
//                        Firebase.crashlytics.recordException(e)
                        startDetectionModel()
                        showError(getString(R.string.retake_the_photo))
                    }
                    else ->
                        showError(getString(R.string.retake_the_photo))
                }

            }.analyze()
        } catch (e: java.lang.Exception) {
//            Firebase.crashlytics.recordException(e)
            startDetectionModel()
            showError(getString(R.string.retake_the_photo))
            if (BuildConfig.IS_FARSI)
                BottomSheetOnlineSupport().show(childFragmentManager, "")
        }

    }


    fun getProgressBarRelatedToUploadedJaw(relatedJaw: Int): ProgressBar {
        return when (relatedJaw) {
            FRONT_JAW -> binding.layoutInsertImage.progressBarFrontTeeth
            LOWER_JAW -> binding.layoutInsertImage.progressBarLowerTeeth
            UPPER_JAW -> binding.layoutInsertImage.progressBarUpperTeeth
            else -> binding.layoutInsertImage.progressBarFrontTeeth
        }
    }

    fun currentUploadJawLayout(currentJaw: Int): ImageView {
        return when (currentJaw) {
            FRONT_JAW -> binding.layoutInsertImage.imageViewFrontTeeth
            LOWER_JAW -> binding.layoutInsertImage.imageViewLowerJaw
            UPPER_JAW -> binding.layoutInsertImage.imageViewUpperJaw
            else -> binding.layoutInsertImage.imageViewFrontTeeth
        }
    }

    override fun addImageToCheckup() {
        Log.e("CURRENT_JAW_ID", getCurrentJawId().toString())
        // Check if current captured jaw already uploaded by its id.
        // If so then update the image otherwise upload image.
        if (getCurrentJawId() != 0 && getCurrentJawId() != -1)
            updateJaw(currentCapturedImage!!, getCurrentJawId())
        else
            uploadJaw(currentCapturedImage!!)

        // Checkout current uploading image
        when (getCurrentDetectedJaw()) {
            Front -> frontJawIsUploading = true
            Upper -> upperJawIsUploading = true
            Lower -> lowerJawIsUploading = true
        }
    }

    override fun getCurrentJawId(): Int {
        Log.e("CURRENT_DETECTION_JAW", getCurrentDetectedJaw())
        return when (getCurrentDetectedJaw()) {
            Front -> uploadedFrontJawId
            Upper -> uploadedUpperJawId
            Lower -> uploadedLowerJawId
            else -> 0
        }
    }

    override fun showError(error: String, isMirror: Boolean) {
        clearView()
        requireActivity().runOnUiThread {
            binding.layoutInsertImage.root.visibleWithAnimation()
            var isMirrorL = true
            // Show error
            binding.apply {
                textViewError.text = error
                cardViewError.visibleWithAnimation()
                cardViewError.goneWithDelay(DelayForShowError) {
                    startDetectionModel()
                }
            }
            // Apply mirroring to textview
            binding.textViewError.apply {
                if (isMirror) {
                    mirroring()
                    onClick {
                        isMirrorL = if (isMirrorL) {
                            clearMirroring()
                            false
                        } else {
                            mirroring()
                            true
                        }
                    }
                }
            }
        }
    }


    override fun setupNextJaw() {
        when (getNextDetectedJaw()) {
            Front -> {
                setNextDetectedJaw(Upper)
            }
            Upper -> {
                setNextDetectedJaw(Lower)
            }

            Lower -> {
                setNextDetectedJaw(Lower)
            }
        }
    }


    override fun startCapturedImageTransaction() {
        requireActivity().runOnUiThread {
            // Reset all animation and move all view to first position
            resetCapturedImageTransaction()

            // Select correct position for insert captured jaw
            selectCorrectPositionForCapturedImage()

            // Load last captured image
            binding.imageViewCapturedJaw.load(currentCapturedImage)

            // Setup animation
            binding.layoutCapturedJaw.apply {
                visibleWithAnimation()
                animate().alpha(FullAlpha)
                    .setStartDelay(AnimationStartDelayForShowImage)
                    .withEndAction {
                        binding.layoutInsertImage.root.visibleWithAnimation()
                        binding.cardViewCapturedJaw.animate().scaleX(0.33f)
                            .scaleY(0.33f)
                            .setDuration(AnimationDuration)
                            .setStartDelay(AnimationStartDelayForMoveImage)
                            .x(layoutForCurrentCapturedJaw.x - binding.cardViewCapturedJaw.width / 2)
                            .y(layoutForCurrentCapturedJaw.y - binding.cardViewCapturedJaw.height / 2)
                            .withEndAction {
                                enableCapturedJaw()

                                binding.imageViewNextJaw.animate()
                                    .scaleY(0.35f)
                                    .scaleX(0.35f)
                                    .alpha(ZeroAlpha)
                                    .setDuration(AnimationDuration)
                                    .x(layoutForNextCapturedJaw.x - binding.cardViewCapturedJaw.width / 2)
                                    .y(layoutForNextCapturedJaw.y - binding.cardViewCapturedJaw.height / 2)
                                    .withEndAction {
                                        binding.pulseView.scaleUpAndMoveLogoToCenter {
                                            if (lastImage == LastImage
                                            ) {
                                                showHideLoading(true)
                                                clearView()
                                            } else
                                                startDetectionModel()
                                        }
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
        }
    }


    override fun resetCapturedImageTransaction() {
        binding.cardViewCapturedJaw.apply {
            scaleY = ZeroScale
            scaleX = ZeroScale
            x = binding.viewCenter.x - height / 2
            y = binding.viewCenter.y - width / 2
            alpha = FullAlpha
        }
        binding.imageViewNextJaw.apply {
            scaleX = ZeroScale
            scaleY = ZeroScale
            x = binding.viewCenter.x - height / 2
            y = binding.viewCenter.y - width / 2
            alpha = FullAlpha
        }
        binding.imageViewCapturedJaw.alpha = FullAlpha
        binding.imageViewPointer.alpha = FullAlpha
        binding.layoutCapturedJaw.apply {
            gone()
            alpha = ZeroAlpha
        }
        binding.cardViewCapturedJaw.apply {
            scaleY = ZeroScale
            scaleX = ZeroScale
            x = binding.viewCenter.x - height / 2
            y = binding.viewCenter.y - width / 2
            alpha = FullAlpha
        }
    }


    override fun saveCapturedJaws(savedBitmap: Bitmap) =
        when (getCurrentDetectedJaw()) {
            Front -> jawDetectionViewModel.updateRecognitionFrontJaw(
                savedBitmap
            )
            Upper -> jawDetectionViewModel.updateRecognitionUpperJaw(
                savedBitmap
            )
            Lower -> jawDetectionViewModel.updateRecognitionLowerJaw(
                savedBitmap
            )
            else -> {
            }
        }


    override fun setupSelectedJawsBasedOnUserSelection() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                listOfSelectedJaws.addAll(listOf(Front, Upper, Lower))
                binding.imageViewFrontSample.visibleWithAnimation()
                setNextDetectedJaw(Front)
                setCurrentDetectedJaw(Front)
            }
            CheckupType.Whitening -> {
                listOfSelectedJaws.add(Front)
                binding.imageViewFrontSample.visibleWithAnimation()
                setNextDetectedJaw(Front)
                setCurrentDetectedJaw(Front)
            }

            CheckupType.Sensitivity, CheckupType.Treatments -> {
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (i, jawPosition) ->
                    when (jawPosition) {
                        JawPosition.FrontTeeth -> listOfSelectedJaws.add(Front)
                        JawPosition.LowerJaw -> listOfSelectedJaws.add(Lower)
                        JawPosition.UpperJaw -> listOfSelectedJaws.add(Upper)
                        else -> listOfSelectedJaws.add(Front)
                    }
                }

                when (listOfSelectedJaws.first()) {
                    Front -> binding.imageViewFrontSample.visibleWithAnimation()
                    Upper -> binding.imageViewUpperSample.visibleWithAnimation()
                    Lower -> binding.imageViewLowerSample.visibleWithAnimation()
                }

                setNextDetectedJaw(listOfSelectedJaws.first())
                setCurrentDetectedJaw(listOfSelectedJaws.first())
            }
            else -> {}
        }
    }

    override fun selectCorrectPositionForCapturedImage() {
        // Checkout current layout to place jaw image
        val totalSelectedJaw = jawDetectionViewModel.submitStateSelectedJaws.value?.size
        layoutForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            Front -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewFront
            }
            Upper -> binding.viewUpper
            Lower -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewLower
            }

            else -> binding.viewFront
        }

        // Checkout next layout
        layoutForNextCapturedJaw = when (getNextDetectedJaw()) {
            Front -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewFront
            }
            Upper -> binding.viewUpper
            Lower -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewLower
            }
            else -> binding.viewLower
        }

        // Checkout current image view to place jaw image
        imageViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            Front -> binding.layoutInsertImage.imageViewFrontTeeth
            Upper -> binding.layoutInsertImage.imageViewUpperJaw
            Lower -> binding.layoutInsertImage.imageViewLowerJaw
            else -> binding.layoutInsertImage.imageViewFrontTeeth
        }

        // Checkout current card view to place jaw image
        cardViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            Front -> binding.layoutInsertImage.cardViewFront
            Upper -> binding.layoutInsertImage.cardViewUpper
            Lower -> binding.layoutInsertImage.cardViewLower
            else -> binding.layoutInsertImage.cardViewFront
        }

        // Checkout current text view to place jaw image
        textViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            Front -> binding.layoutInsertImage.textViewFrontTeethTitle
            Upper -> binding.layoutInsertImage.textViewUpperJawTitle
            Lower -> binding.layoutInsertImage.textViewLowerJawTitle
            else -> binding.layoutInsertImage.textViewFrontTeethTitle
        }
    }

    override fun setNextDetectedJaw(nextDetectedJaw: String?) =
        jawDetectionViewModel.updateNextDetectedJaw(nextDetectedJaw)

    override fun getNextDetectedJaw(): String? =
        jawDetectionViewModel.stateNextDetectedJaw.value!!

    override fun showLayoutChoosePhoto() =
        binding.layoutTakePhotoOrChoose.root.visibleWithAnimation()

    override fun hideLayoutChoosePhoto() =
        binding.layoutTakePhotoOrChoose.root.goneWithAnimation()

    override fun stopDetectionModel() =
        jawDetectionViewModel.stopDetectionModel()

    override fun startDetectionModel() =
        jawDetectionViewModel.startDetectionModel()

    override fun getDetectionModelState(): Boolean =
        jawDetectionViewModel.stateDetectionModel.value!!

    override fun setCurrentDetectedJaw(currentDetectionJaw: String, isMainThread: Boolean) =
        jawDetectionViewModel.updateCurrentDetectedJaw(currentDetectionJaw, isMainThread)

    override fun getCurrentDetectedJaw(): String =
        jawDetectionViewModel.stateCurrentDetectedJaw.value!!

    override fun setLastCapturedJaw(lastCapturedJaw: Bitmap) =
        jawDetectionViewModel.updateCurrentCapturedImage(lastCapturedJaw)

    override fun getLastCapturedJaw(): Bitmap =
        jawDetectionViewModel.recognitionCurrentCapturedImage.value!!

    override fun enableCapturedJaw() {
        binding.imageViewPointer.animate().alpha(ZeroAlpha)
        binding.cardViewCapturedJaw.animate().alpha(ZeroAlpha)
        cardViewForCurrentCapturedJaw.isSelected = true
        textViewForCurrentCapturedJaw.enable()
        imageViewForCurrentCapturedJaw.setImageBitmap(currentCapturedImage)
    }

    override fun showPulseAnimation(currentDetectedJaw: String) {
        hideSample()
        // Stop the model
        stopDetectionModel()
        // Vibrate
        VibratorHelper().vibrateForDetectingModel(requireActivity())
        // Setup pulse animation
        binding.pulseView.apply {
            setJawDetectedType(currentDetectedJaw)
            startPulseAnimation { waitForASecond ->
                if (waitForASecond) {
                    takePhoto(currentDetectedJaw)
                }
            }
            visibleWithAnimation()
        }
    }

    override fun checkoutNextDetectionJaw() {
        if (getNextDetectedJaw() == Front)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.FrontTeeth)!!)
                    Front
                else
                    Upper
            )

        if (getNextDetectedJaw() == Upper)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.UpperJaw)!!)
                    Upper
                else
                    Lower
            )

        if (getNextDetectedJaw() == Lower)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.LowerJaw)!!)
                    Lower
                else
                    Lower
            )
    }

    override fun checkoutNecessaryUploadedImage() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                jawDetectionViewModel.setSelectedJaw(AllJawsAreSelected)
                uploadedFrontJawId = 0
                uploadedLowerJawId = 0
                uploadedUpperJawId = 0
            }
            CheckupType.Whitening -> {
                uploadedFrontJawId = 0
                jawDetectionViewModel.setSelectedJaw(FRONT_JAW)
            }
            CheckupType.Sensitivity, CheckupType.Treatments -> {
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (i, jawPosition) ->
                    when (jawPosition) {
                        JawPosition.UpperJaw -> {
                            uploadedUpperJawId = 0
                            jawDetectionViewModel.setSelectedJaw(UPPER_JAW, JawPosition.UpperJaw)
                        }
                        JawPosition.FrontTeeth -> {
                            uploadedFrontJawId = 0
                            jawDetectionViewModel.setSelectedJaw(FRONT_JAW)
                        }
                        JawPosition.LowerJaw -> {
                            uploadedLowerJawId = 0
                            jawDetectionViewModel.setSelectedJaw(LOWER_JAW, JawPosition.LowerJaw)
                        }
                        else -> {
                            uploadedFrontJawId = 0
                            jawDetectionViewModel.setSelectedJaw(FRONT_JAW)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    override fun showCapturedImageBasedOnSelectedJaw() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                showAllCapturedJaw()
            }
            CheckupType.Whitening -> {
                hideCapturedImageForWhiteningCheckup()
            }
            CheckupType.Sensitivity, CheckupType.Treatments -> {
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (i, jawPosition) ->
                    when (jawPosition) {
                        JawPosition.FrontTeeth -> binding.layoutInsertImage.layoutFrontTeeth.visible()
                        JawPosition.LowerJaw -> binding.layoutInsertImage.layoutLowerJaw.visible()
                        JawPosition.UpperJaw -> binding.layoutInsertImage.layoutUpperJaw.visible()
                        else -> binding.layoutInsertImage.layoutFrontTeeth.visible()
                    }
                }

            }

            else -> {}
        }
    }


    override fun showImagesThatUserMustCapture() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                showAllCapturedJaw()
            }

            CheckupType.Whitening -> {
                hideCapturedImageForWhiteningCheckup()
            }
            CheckupType.Sensitivity -> {
                showCapturedImageBasedOnSelectedJaw()
                checkoutNextDetectionJaw()
            }
            CheckupType.Treatments -> {
                showCapturedImageBasedOnSelectedJaw()
                checkoutNextDetectionJaw()
            }
            else -> {}
        }
    }

    override fun takePhotoFromDetectedJaw(detectedJawLabel: String) {
        setCurrentDetectedJaw(detectedJawLabel)
        showPulseAnimation(detectedJawLabel)
    }

    private fun hideSample() = requireActivity().runOnUiThread {
        binding.imageViewFrontSample.goneWithAnimation()
        binding.imageViewUpperSample.goneWithAnimation()
        binding.imageViewLowerSample.goneWithAnimation()
    }

    override fun hideCapturedImageForWhiteningCheckup() {
        binding.layoutInsertImage.apply {
            layoutFrontTeeth.visible()
            layoutLowerJaw.gone()
            layoutUpperJaw.gone()
            isWhitening = true
        }
    }

    override fun showAllCapturedJaw() {
        binding.layoutInsertImage.apply {
            layoutFrontTeeth.visible()
            layoutLowerJaw.visible()
            layoutUpperJaw.visible()
            isWhitening = false
        }
    }

    override fun insertImageFromGalleryBasedOnJaw(detectedJaw: String) {
        when (detectedJaw) {
            Front -> {
                binding.layoutInsertImage.imageViewFrontTeeth.load(currentCapturedImage)
                binding.layoutInsertImage.cardViewFront.isSelected = true
                binding.layoutInsertImage.textViewFrontTeethTitle.enable()
                setNextDetectedJaw(Upper)
                binding.imageViewFrontSample.goneWithAnimation()
            }
            Upper -> {
                binding.layoutInsertImage.imageViewUpperJaw.load(currentCapturedImage)
                binding.imageViewUpperSample.goneWithAnimation()
                binding.layoutInsertImage.cardViewUpper.isSelected = true
                binding.layoutInsertImage.textViewUpperJawTitle.enable()
                setNextDetectedJaw(Lower)
            }
            Lower -> {
                binding.layoutInsertImage.imageViewLowerJaw.load(currentCapturedImage)
                binding.layoutInsertImage.cardViewLower.isSelected = true
                binding.layoutInsertImage.textViewLowerJawTitle.enable()
                binding.imageViewLowerSample.goneWithAnimation()
                setNextDetectedJaw(Lower)
            }
            Over -> return
        }
    }

    override fun showInsertedImageBasedOnSelectedJaws() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                showAllCapturedJaw()
            }
            CheckupType.Whitening -> {
                hideCapturedImageForWhiteningCheckup()
            }
            CheckupType.Sensitivity -> {
                checkoutNextDetectionJaw()
            }
            CheckupType.Treatments -> {
                checkoutNextDetectionJaw()
            }
            null -> {
            }
            else -> {}
        }
    }

    override fun clearView() {
        requireActivity().runOnUiThread {
            if (binding.pulseView.isVisible)
                binding.pulseView.clearView()
            binding.layoutInsertImage.root.hideWithAnimation()
            hideLayoutChoosePhoto()
        }
    }

    private fun checkIfLastImageToUpload() {
        lastImage =
            if (jawDetectionViewModel.stateListOfUploadedJaws.value?.equals(jawDetectionViewModel.submitStateSelectedJaws.value) == true)
                LastImage
            else
                NotLastImage
    }


    override fun uploadJaw(image: Bitmap) {
        // Check if current uploaded image is last image
        jawDetectionViewModel.photosUploaded(getCurrentDetectedJaw().convertJawToInt())
        checkIfLastImageToUpload()
        when (getCurrentDetectedJaw().convertJawToInt()) {
            FRONT_JAW -> checkupSubmitImageViewModel.addFrontImageToCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                lastImage = lastImage
            )
            UPPER_JAW -> checkupSubmitImageViewModel.addUpperImageToCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                lastImage = lastImage
            )
            LOWER_JAW -> checkupSubmitImageViewModel.addLowerImageToCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                lastImage = lastImage
            )
        }
    }


    override fun updateJaw(image: Bitmap, imageId: Int) {
        checkIfLastImageToUpload()
        when (getCurrentDetectedJaw()) {
            Front -> checkupSubmitImageViewModel.updateFrontImageInCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                imageId = imageId
            )
            Upper -> checkupSubmitImageViewModel.updateUpperImageInCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                imageId = imageId
            )
            Lower -> checkupSubmitImageViewModel.updateLowerImageInCheckup(
                checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                image = image.convertToFile(requireContext()),
                imageId = imageId
            )
        }
    }


    override fun setUploadedImageId(imageId: Int, jawType: Int) {
        when (jawType.convertIntToJaw()) {
            Front -> {
                uploadedFrontJawId = imageId
                binding.layoutInsertImage.imageViewFrontTeeth.enable()
                binding.layoutInsertImage.progressBarFrontTeeth.goneWithAnimation()
            }
            Upper -> {
                uploadedUpperJawId = imageId
                binding.layoutInsertImage.imageViewUpperJaw.enable()
                binding.layoutInsertImage.progressBarUpperTeeth.goneWithAnimation()
            }
            Lower -> {
                uploadedLowerJawId = imageId
                binding.layoutInsertImage.imageViewLowerJaw.enable()
                binding.layoutInsertImage.progressBarLowerTeeth.goneWithAnimation()
            }
            else -> {
            }
        }

        // Change the status of uploaded jaw
        when (jawType.convertIntToJaw()) {
            Front -> frontJawIsUploading = false
            Upper -> upperJawIsUploading = false
            Lower -> lowerJawIsUploading = false
        }
    }

    override fun showWaitingBarForGettingResult() =
        jawDetectionViewModel.stateListOfUploadedJaws.observe(viewLifecycleOwner, {
            if (it.equals(jawDetectionViewModel.submitStateSelectedJaws.value)) {
                showHideLoading(true)
            }
        })


    override fun getCheckupResult() {
        // If all necessary image are uploaded
        // then get the result and go to result page.
        if (uploadedFrontJawId != 0
            && uploadedLowerJawId != 0
            && uploadedUpperJawId != 0
        ) {
            stopDetectionModel()
            clearView()
            checkupResultViewModel.checkupResult(chooseCheckupViewModel.submitStateCreateCheckupId.value!!)
            checkupResultViewModel.submitStateCheckupResult.subscribe(
                viewLifecycleOwner,
                ::handleViewStateGetCheckupResult
            )
        }
    }

    /** Handel view state for getting checkup result */
    private fun handleViewStateGetCheckupResult(loadable: Loadable<CheckupResultSuccessModel>) {
        if (loadable != Loading) showHideLoading(false)
        when (loadable) {
            is Success -> {
                chooseCheckupViewModel.setCheckupResult(loadable.data)
                jawDetectionViewModel.resetNumberOfUploadedJaw()
                checkupQuestionViewModel.resetSelectedJaw()
                jawDetectionViewModel.resetSelectedJaw()
                stopDetectionModel()
                // Go to correct navigation
                if (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value == CheckupType.Whitening)
                    findNavController().navigate(R.id.action_fragmentCamera_to_fragmentCheckupResultWhitening)
                else
                    findNavController().navigate(R.id.action_fragmentCamera_to_fragmentCheckupResultBasic)

            }
            is Failure -> {
            }
            Loading -> {
                showHideLoading(true)
            }
            NotLoading -> {
            }
        }
    }

    /** Loading for getting checkup result **/
    fun showHideLoading(isShowing: Boolean) = if (isShowing)
        binding.apply {
            progressBar.visibleWithAnimation()
            layoutInsertImage.root.goneWithAnimation()
        }
    else
        binding.apply {
            progressBar.goneWithAnimation()
            layoutInsertImage.root.visibleWithAnimation()
        }

    companion object {
        const val MODEL_NAME = "yolov5n_320sz_jaw_detection_v3.tflite"
        private const val MARGIN_THRESHOLD = 0.06
        const val MinimumConfidence = 0.85
        private const val TAG = "TFL Jaw Detection"
        private const val Front = "front"
        private const val Upper = "upper"
        private const val Lower = "lower"
        private const val Over = "Over"
        private const val LastImage = 1
        private const val NotLastImage = 0
        private const val ZeroScale = 1f
        private const val FullAlpha = 1f
        private const val ZeroAlpha = 0f
        private const val AnimationDuration = 700L
        private const val AnimationStartDelayForShowImage = 2000L
        private const val AnimationStartDelayForMoveImage = 200L
        private const val DelayForShowError = 5000L
        private const val AllJawsAreSelected = 4

        var currentCapturedImage: Bitmap? = null
        var isWhitening = false
        var isLower = false

        // Values for cropping the captured image by camera
        var xCroppedImage = 0
        var yCroppedImage = 0
        var widthCroppedImage = 0
        var heightCroppedImage = 0
    }

}



