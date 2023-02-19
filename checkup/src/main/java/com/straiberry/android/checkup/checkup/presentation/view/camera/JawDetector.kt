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
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.straiberry.android.checkup.BuildConfig
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
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
import com.straiberry.android.common.tflite.DetectorFactory
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
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
    val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
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
    val listOfSelectedJaws = ArrayList<JawPosition>()
    val cameraXTargetResolution = Size(1024, 1024)

    @androidx.camera.core.ExperimentalGetImage
    override fun  startCamera() {
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
                        cameraExecutor,
                        { imageProxy ->
                            if (getDetectionModelState()) {
                                jawDetectionViewModel.detectFromImageProxy(imageProxy, detector)
                            } else
                                imageProxy.close()
                        }
                    )

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
                Firebase.crashlytics.recordException(exc)
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
                else -> ImageAnalyzer(
                    context = requireContext(),
                    bitmap = capturedImage,
                    checkingFrame = false
                ) { label, _, imageIsCorrect, _, finalImage ->
                    when {
                        // Captured image is not one of selected jaws
                        !listOfSelectedJaws.contains(label.convertLabelToJawType()) -> {
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
                            jawDetectionViewModel.capturedImageIsCorrect(
                                CorrectCapturedImage(
                                    true,
                                    finalImage,
                                    label.convertLabelToJawType()
                                )
                            )
                        } catch (e: Exception) {
                            Firebase.crashlytics.recordException(e)
                            startDetectionModel()
                            showError(getString(R.string.retake_the_photo))
                        }
                        else ->
                            showError(getString(R.string.retake_the_photo))
                    }

                }.analyze()
            }
        } catch (e: java.lang.Exception) {
            Firebase.crashlytics.recordException(e)
            startDetectionModel()
            showError(getString(R.string.retake_the_photo))
            if (BuildConfig.IS_FARSI)
                BottomSheetOnlineSupport().show(childFragmentManager, "")
        }


    }


    fun getProgressBarRelatedToUploadedJaw(relatedJaw: JawPosition): ProgressBar {
        return when (relatedJaw) {
            JawPosition.FrontTeeth -> binding.layoutInsertImage.progressBarFrontTeeth
            JawPosition.LowerJaw -> binding.layoutInsertImage.progressBarLowerTeeth
            JawPosition.UpperJaw -> binding.layoutInsertImage.progressBarUpperTeeth
            else -> binding.layoutInsertImage.progressBarFrontTeeth
        }
    }

    fun currentUploadJawLayout(currentJaw: JawPosition): ImageView {
        return when (currentJaw) {
            JawPosition.FrontTeeth -> binding.layoutInsertImage.imageViewFrontTeeth
            JawPosition.LowerJaw -> binding.layoutInsertImage.imageViewLowerJaw
            JawPosition.UpperJaw -> binding.layoutInsertImage.imageViewUpperJaw
            else -> binding.layoutInsertImage.imageViewFrontTeeth
        }
    }

    fun clearCapturedImage(jawType: JawPosition) {
        when (jawType) {
            JawPosition.UpperJaw -> {
                binding.layoutInsertImage.imageViewUpperJaw.load(com.straiberry.android.common.R.drawable.ic_add_another)
                binding.layoutInsertImage.cardViewUpper.isSelected = false
            }
            JawPosition.LowerJaw -> {
                binding.layoutInsertImage.imageViewLowerJaw.load(com.straiberry.android.common.R.drawable.ic_add_another)
                binding.layoutInsertImage.cardViewLower.isSelected = false
            }
            JawPosition.FrontTeeth -> {
                binding.layoutInsertImage.imageViewFrontTeeth.load(com.straiberry.android.common.R.drawable.ic_add_another)
                binding.layoutInsertImage.cardViewFront.isSelected = false
            }
            else -> {}
        }
    }

    override fun addImageToCheckup() {
        // Check if current captured jaw already uploaded by its id.
        // If so then update the image otherwise upload image.
        if (getCurrentJawId() != 0 && getCurrentJawId() != -1)
            updateJaw(currentCapturedImage!!, getCurrentJawId())
        else
            uploadJaw(currentCapturedImage!!)

        // Checkout current uploading image
        when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> frontJawIsUploading = true
            JawPosition.UpperJaw -> upperJawIsUploading = true
            JawPosition.LowerJaw -> lowerJawIsUploading = true
            else -> frontJawIsUploading = true
        }
    }

    override fun getCurrentJawId(): Int {
        return when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> uploadedFrontJawId
            JawPosition.UpperJaw -> uploadedUpperJawId
            JawPosition.LowerJaw -> uploadedLowerJawId
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
                }
                startDetectionModel()
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
            JawPosition.FrontTeeth, JawPosition.FrontTeethLower, JawPosition.FrontTeethUpper -> {
                setNextDetectedJaw(JawPosition.UpperJaw)
            }
            JawPosition.UpperJaw -> {
                setNextDetectedJaw(JawPosition.LowerJaw)
            }

            JawPosition.LowerJaw -> {
                setNextDetectedJaw(JawPosition.LowerJaw)
            }
            null -> {}
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
                                            startDetectionModel()
                                            if (lastImage == LastImage
                                            ) {
                                                showHideLoading(true)
                                                clearView()
                                            }
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
            JawPosition.FrontTeeth -> jawDetectionViewModel.updateRecognitionFrontJaw(
                savedBitmap
            )
            JawPosition.UpperJaw -> jawDetectionViewModel.updateRecognitionUpperJaw(
                savedBitmap
            )
            JawPosition.LowerJaw -> jawDetectionViewModel.updateRecognitionLowerJaw(
                savedBitmap
            )
            else -> {
            }
        }


    override fun setupSelectedJawsBasedOnUserSelection() {
        when (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value) {
            CheckupType.Regular, CheckupType.Others -> {
                listOfSelectedJaws.addAll(
                    listOf(
                        JawPosition.FrontTeeth,
                        JawPosition.UpperJaw,
                        JawPosition.LowerJaw
                    )
                )
                binding.imageViewFrontSample.visibleWithAnimation()
                setNextDetectedJaw(JawPosition.FrontTeeth)
            }
            CheckupType.Whitening -> {
                listOfSelectedJaws.add(JawPosition.FrontTeeth)
                binding.imageViewFrontSample.visibleWithAnimation()
                setNextDetectedJaw(JawPosition.FrontTeeth)
            }

            CheckupType.Sensitivity, CheckupType.Treatments -> {
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (_, jawPosition) ->
                    when (jawPosition) {
                        JawPosition.FrontTeeth -> listOfSelectedJaws.add(JawPosition.FrontTeeth)
                        JawPosition.LowerJaw -> listOfSelectedJaws.add(JawPosition.LowerJaw)
                        JawPosition.UpperJaw -> listOfSelectedJaws.add(JawPosition.UpperJaw)
                        else -> listOfSelectedJaws.add(JawPosition.FrontTeeth)
                    }
                }

                when (listOfSelectedJaws.first()) {
                    JawPosition.FrontTeeth, JawPosition.FrontTeethUpper, JawPosition.FrontTeethLower -> binding.imageViewFrontSample.visibleWithAnimation()
                    JawPosition.LowerJaw -> binding.imageViewLowerSample.visibleWithAnimation()
                    JawPosition.UpperJaw -> binding.imageViewUpperSample.visibleWithAnimation()
                }

                setNextDetectedJaw(listOfSelectedJaws.first())
            }
            else -> {}
        }
    }

    override fun selectCorrectPositionForCapturedImage() {
        // Checkout current layout to place jaw image
        val totalSelectedJaw = jawDetectionViewModel.submitStateSelectedJaws.value?.size
        layoutForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewFront
            }
            JawPosition.UpperJaw -> binding.viewUpper
            JawPosition.LowerJaw -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewLower
            }

            else -> binding.viewFront
        }

        // Checkout next layout
        layoutForNextCapturedJaw = when (getNextDetectedJaw()) {
            JawPosition.FrontTeeth, JawPosition.FrontTeethUpper, JawPosition.FrontTeethLower -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewFront
            }
            JawPosition.UpperJaw -> binding.viewUpper
            JawPosition.LowerJaw -> {
                if (totalSelectedJaw == 1)
                    binding.viewUpper
                else
                    binding.viewLower
            }
            else -> binding.viewLower
        }

        // Checkout current image view to place jaw image
        imageViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> binding.layoutInsertImage.imageViewFrontTeeth
            JawPosition.UpperJaw -> binding.layoutInsertImage.imageViewUpperJaw
            JawPosition.LowerJaw -> binding.layoutInsertImage.imageViewLowerJaw
            else -> binding.layoutInsertImage.imageViewFrontTeeth
        }


        // Checkout current card view to place jaw image
        cardViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> binding.layoutInsertImage.cardViewFront
            JawPosition.UpperJaw -> binding.layoutInsertImage.cardViewUpper
            JawPosition.LowerJaw -> binding.layoutInsertImage.cardViewLower
            else -> binding.layoutInsertImage.cardViewFront
        }

        // Checkout current text view to place jaw image
        textViewForCurrentCapturedJaw = when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth -> binding.layoutInsertImage.textViewFrontTeethTitle
            JawPosition.UpperJaw -> binding.layoutInsertImage.textViewUpperJawTitle
            JawPosition.LowerJaw -> binding.layoutInsertImage.textViewLowerJawTitle
            else -> binding.layoutInsertImage.textViewFrontTeethTitle
        }
    }

    override fun setNextDetectedJaw(nextDetectedJaw: JawPosition?) =
        jawDetectionViewModel.updateNextDetectedJaw(nextDetectedJaw)

    override fun getNextDetectedJaw(): JawPosition? =
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

    override fun setCurrentDetectedJaw(currentDetectionJaw: JawPosition, isMainThread: Boolean) =
        jawDetectionViewModel.updateCurrentDetectedJaw(currentDetectionJaw, isMainThread)

    override fun getCurrentDetectedJaw(): JawPosition =
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
        clearView()
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
        if (getNextDetectedJaw() == JawPosition.FrontTeeth)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.FrontTeeth)!!)
                    JawPosition.FrontTeeth
                else
                    JawPosition.UpperJaw
            )

        if (getNextDetectedJaw() == JawPosition.UpperJaw)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.UpperJaw)!!)
                    JawPosition.UpperJaw
                else
                    JawPosition.LowerJaw
            )

        if (getNextDetectedJaw() == JawPosition.LowerJaw)
            setNextDetectedJaw(
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.LowerJaw)!!)
                    JawPosition.LowerJaw
                else
                    JawPosition.LowerJaw
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
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (_, jawPosition) ->
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
                checkupQuestionViewModel.submitStateSelectedJaw.value?.forEach { (_, jawPosition) ->
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
                setNextDetectedJaw(JawPosition.UpperJaw)
                binding.imageViewFrontSample.goneWithAnimation()
            }
            Upper -> {
                binding.layoutInsertImage.imageViewUpperJaw.load(currentCapturedImage)
                binding.imageViewUpperSample.goneWithAnimation()
                binding.layoutInsertImage.cardViewUpper.isSelected = true
                binding.layoutInsertImage.textViewUpperJawTitle.enable()
                setNextDetectedJaw(JawPosition.LowerJaw)
            }
            Lower -> {
                binding.layoutInsertImage.imageViewLowerJaw.load(currentCapturedImage)
                binding.layoutInsertImage.cardViewLower.isSelected = true
                binding.layoutInsertImage.textViewLowerJawTitle.enable()
                binding.imageViewLowerSample.goneWithAnimation()
                setNextDetectedJaw(JawPosition.LowerJaw)
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
            binding.cardViewError.gone()
            hideLayoutChoosePhoto()
        }
    }

    fun checkIfLastImageToUpload() {
        lastImage =
            if (jawDetectionViewModel.stateListOfUploadedJaws.value?.equals(jawDetectionViewModel.submitStateSelectedJaws.value) == true)
                LastImage
            else
                NotLastImage
    }


    override fun uploadJaw(image: Bitmap) {
        // Check if current uploaded image is last image
        jawDetectionViewModel.photosUploaded(getCurrentDetectedJaw())
        checkIfLastImageToUpload()
        when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth, JawPosition.FrontTeethUpper, JawPosition.FrontTeethLower -> {
                checkupSubmitImageViewModel.addFrontImageToCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    lastImage = lastImage
                )
                checkupSubmitImageViewModel.submitStateAddFrontImageToCheckup.observe(
                    viewLifecycleOwner
                ) {
                    if (it is Failure) {
                        uploadedFrontJawId = 0
                        frontJawIsUploading = false
                        clearCapturedImage(JawPosition.FrontTeeth)
                    }

                    handleViewStateAddImageToCheckup(it, JawPosition.FrontTeeth)
                }
            }
            JawPosition.UpperJaw -> {
                checkupSubmitImageViewModel.addUpperImageToCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    lastImage = lastImage
                )
                checkupSubmitImageViewModel.submitStateAddUpperImageToCheckup.observe(
                    viewLifecycleOwner
                ) {
                    if (it is Failure) {
                        uploadedUpperJawId = 0
                        upperJawIsUploading = false
                        clearCapturedImage(JawPosition.UpperJaw)
                    }

                    handleViewStateAddImageToCheckup(it, JawPosition.UpperJaw)
                }
            }
            JawPosition.LowerJaw -> {
                checkupSubmitImageViewModel.addLowerImageToCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    lastImage = lastImage
                )
                checkupSubmitImageViewModel.submitStateAddLowerImageToCheckup.observe(
                    viewLifecycleOwner
                ) {
                    if (it is Failure) {
                        uploadedLowerJawId = 0
                        lowerJawIsUploading = false
                        clearCapturedImage(JawPosition.LowerJaw)
                    }

                    handleViewStateAddImageToCheckup(it, JawPosition.LowerJaw)
                }
            }
        }
    }

    /** Handel view state for adding front jaw image to checkup */
    private fun handleViewStateAddImageToCheckup(
        loadable: Loadable<AddImageToCheckupSuccessModel>,
        jawType: JawPosition
    ) {
        if (loadable != Loading) {
            currentUploadJawLayout(jawType).enable()
            getProgressBarRelatedToUploadedJaw(jawType).goneWithAnimation()
        }
        when (loadable) {
            is Success -> {
                setUploadedImageId(loadable.data.imageId, loadable.data.jawType)
                getCheckupResult()
            }
            is Failure -> {
                showError(
                    getString(
                        R.string.problem_with_uploading_photo,
                    ),
                    false
                )
                showHideLoading(false)
                if (BuildConfig.IS_FARSI)
                    BottomSheetOnlineSupport().show(childFragmentManager, "")
            }
            Loading -> {
                currentUploadJawLayout(jawType).disable()
                getProgressBarRelatedToUploadedJaw(jawType).visibleWithAnimation()
            }
            NotLoading -> {

            }
        }
    }


    override fun updateJaw(image: Bitmap, imageId: Int) {
        checkIfLastImageToUpload()
        when (getCurrentDetectedJaw()) {
            JawPosition.FrontTeeth, JawPosition.FrontTeethLower, JawPosition.FrontTeethUpper -> {
                checkupSubmitImageViewModel.updateFrontImageInCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    imageId = imageId
                )
                checkupSubmitImageViewModel.submitStateUpdateFrontImageInCheckup.observe(
                    viewLifecycleOwner
                ) {
                    handleViewStateUpdateImageInCheckup(it, JawPosition.FrontTeeth)
                }
            }
            JawPosition.UpperJaw -> {
                checkupSubmitImageViewModel.updateUpperImageInCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    imageId = imageId
                )
                checkupSubmitImageViewModel.submitStateUpdateUpperImageInCheckup.observe(
                    viewLifecycleOwner
                ) {
                    handleViewStateUpdateImageInCheckup(it, JawPosition.UpperJaw)
                }
            }
            JawPosition.LowerJaw -> {
                checkupSubmitImageViewModel.updateLowerImageInCheckup(
                    checkupId = chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                    image = image.convertToFile(requireContext()),
                    imageId = imageId
                )
                checkupSubmitImageViewModel.submitStateUpdateLowerImageInCheckup.observe(
                    viewLifecycleOwner
                ) {
                    handleViewStateUpdateImageInCheckup(it, JawPosition.LowerJaw)
                }
            }
        }
    }

    /** Handel view state for updating captured image in checkup */
    private fun handleViewStateUpdateImageInCheckup(
        loadable: Loadable<UpdateImageInCheckupSuccessModel>,
        jawType: JawPosition
    ) {
        if (loadable != Loading) {
            currentUploadJawLayout(jawType).enable()
            getProgressBarRelatedToUploadedJaw(jawType).goneWithAnimation()
        }
        when (loadable) {
            is Success -> {
                jawDetectionViewModel.photosUploaded(jawType)
            }
            is Failure -> {
                showError(
                    getString(
                        R.string.problem_with_uploading_photo
                    ), false
                )
            }
            Loading -> {
                jawDetectionViewModel.photosUploadedFailed(jawType)
                currentUploadJawLayout(jawType).disable()
                getProgressBarRelatedToUploadedJaw(jawType).visibleWithAnimation()
            }
            NotLoading -> {

            }
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
        private const val AnimationDuration = 300L
        private const val AnimationStartDelayForShowImage = 100L
        private const val AnimationStartDelayForMoveImage = 100L
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



