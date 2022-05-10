package com.straiberry.android.checkup.checkup.presentation.view.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import coil.load
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
import com.straiberry.android.checkup.checkup.presentation.view.camera.CameraAnalyzer.Companion.MinimumConfidence
import com.straiberry.android.checkup.common.extentions.getImage
import com.straiberry.android.checkup.common.extentions.getPath
import com.straiberry.android.checkup.common.extentions.modifyOrientation
import com.straiberry.android.checkup.databinding.FragmentCameraBinding
import com.straiberry.android.checkup.di.*
import com.straiberry.android.common.base.*
import com.straiberry.android.common.custom.spotlight.*
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.extensions.*


class FragmentCamera : DetectionJaw(), IsolatedKoinComponent {
    // Permissions
    private val cameraPermissions = listOf(Manifest.permission.CAMERA)
    private val readStoragePermissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var pickImageFromGalleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var readStoragePermissionListener: ActivityResultLauncher<String>
    private lateinit var cameraPermissionListener: ActivityResultLauncher<String>
    private lateinit var spotLight: Spotlight

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCameraBinding.inflate(inflater, container, false).also {
            binding = it

            if (guideTourViewModel.getGuideTourStatus().cameraGuideTour.not())
                setupGuideTour()

            // Setup click for button take photo
            binding.layoutTakePhotoOrChoose.buttonTakePhoto.onClick {
                hideLayoutChoosePhoto()
                startDetectionModel()
            }

            // Setup choose camera
            binding.layoutTakePhotoOrChoose.buttonChoosePhoto.onClick {
                hideLayoutChoosePhoto()
                checkForReadStoragePermission()
            }

            // Setup button cancel
            binding.layoutTakePhotoOrChoose.buttonCancel.onClick { findNavController().popBackStack() }

            // Setup button close
            binding.textViewClose.onClick {
                jawDetectionViewModel.resetNumberOfUploadedJaw()
                checkupQuestionViewModel.resetSelectedJaw()
                stopDetectionModel()
                findNavController().popBackStack()
            }

            // Setup button upload
            binding.imageButtonUpload.onClick {
                hideLayoutChoosePhoto()
                checkForReadStoragePermission()
            }

            binding.layoutInsertImage.imageViewFrontTeeth.onClick {
                hideAllGuide()
                binding.imageViewFrontSample.visibleWithAnimation()
                setupNextJaw(Front)
            }

            binding.layoutInsertImage.imageViewFrontTeethCenter.onClick {
                hideAllGuide()
                binding.imageViewFrontSample.visibleWithAnimation()
                setupNextJaw(Front)
            }

            binding.layoutInsertImage.imageViewUpperJaw.onClick {
                hideAllGuide()
                binding.imageViewUpperSample.visibleWithAnimation()
                setupNextJaw(Upper)
            }

            binding.layoutInsertImage.imageViewLowerJaw.onClick {
                hideAllGuide()
                binding.imageViewLowerSample.visibleWithAnimation()
                setupNextJaw(Lower)
            }

            binding.layoutInsertImage.imageViewLowerJawCenter.onClick {
                hideAllGuide()
                binding.imageViewLowerSample.visibleWithAnimation()
                setupNextJaw(Lower)
            }


            checkoutNecessaryUploadedImage()
            showCapturedImageBasedOnSelectedJaw()
            setupSelectedJawsBasedOnUserSelection()
            showWaitingBarForGettingResult()
            /**
             * Observe the tf model recognition:
             *  - Measure the cropped image base on model rect output
             *  - Take picture and do the transaction
             */
            jawDetectionViewModel.recognitionPosition.observe(viewLifecycleOwner,
                { jawRecognition ->
                    // Check if current detected image is not uploading and
                    // its one of selected jaws by user.
                    if (getDetectionModelState()
                        && !when {
                            jawRecognition.label == Front && frontJawIsUploading -> true
                            jawRecognition.label == Lower && lowerJawIsUploading -> true
                            jawRecognition.label == Upper && upperJawIsUploading -> true
                            else -> false
                        }
                        && listOfSelectedJaws.contains(jawRecognition.label)
                        && jawRecognition.confidence >= MinimumConfidence
                    ) {
                        if (jawRecognition.frame != null)
                            ImageAnalyzer(
                                requireContext(),
                                jawRecognition.frame
                            ) { _, imageIsCorrect, _, _ ->
                                if (imageIsCorrect) {
                                    showImagesThatUserMustCapture()
                                    takePhotoFromDetectedJaw(jawRecognition.label)
                                }
                            }.analyze()

                    }
                })

            checkupSubmitImageViewModel.submitStateAddImage.subscribe(
                viewLifecycleOwner,
                ::handleViewStateAddImageToCheckup
            )

            checkupSubmitImageViewModel.submitStateUpdateImage.subscribe(
                viewLifecycleOwner,
                ::handleViewStateUpdateImageInCheckup
            )
        }.root
    }

    private fun hideAllGuide() {
        binding.apply {
            imageViewLowerSample.goneWithAnimation()
            imageViewUpperSample.goneWithAnimation()
            imageViewFrontSample.goneWithAnimation()
        }
    }

    private fun setupNextJaw(nextJaw: String) {
        setCurrentDetectedJaw(nextJaw)
        setNextDetectedJaw(nextJaw)
        startDetectionModel()
        clearView()
    }

    /** Handel view state for adding captured image to checkup */
    private fun handleViewStateAddImageToCheckup(loadable: Loadable<AddImageToCheckupSuccessModel>) {
        if (loadable != Loading) {
            getCurrentCapturedJaw().enable()
            getCurrentProgressBar().goneWithAnimation()
        }
        when (loadable) {
            is Success -> {
                setUploadedImageId(loadable.data.imageId, loadable.data.jawType)
                getCheckupResult()
            }
            is Failure -> {
                showError(
                    getString(
                        R.string.the_selected_picture_is_incorrect,
                        getCurrentDetectedJaw()
                    ),
                    false
                )
                jawDetectionViewModel.photosUploadedFailed()
                showHideLoading(false)
                getCurrentCapturedJaw().load(R.drawable.ic_add_another)
            }
            Loading -> {
                getCurrentCapturedJaw().disable()
                getCurrentProgressBar().visibleWithAnimation()
            }
            NotLoading -> {

            }
        }
    }


    /** Handel view state for updating captured image in checkup */
    private fun handleViewStateUpdateImageInCheckup(loadable: Loadable<UpdateImageInCheckupSuccessModel>) {
        if (loadable != Loading) {
            getCurrentCapturedJaw().enable()
            getCurrentProgressBar().goneWithAnimation()
        }
        when (loadable) {
            is Success -> {
                jawDetectionViewModel.photosUploaded()
            }
            is Failure -> {
                showError(
                    getString(
                        R.string.the_selected_picture_is_incorrect,
                        getCurrentDetectedJaw()
                    ), false
                )
                getCurrentCapturedJaw().load(R.drawable.ic_add_another)
            }
            Loading -> {
                jawDetectionViewModel.photosUploadedFailed()
                getCurrentCapturedJaw().disable()
                getCurrentProgressBar().visibleWithAnimation()
            }
            NotLoading -> {

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
        /** Get selected image from gallery */
        pickImageFromGalleryResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    checkoutNextDetectionJaw()
                    // Start detection
                    startDetectionModel()
                    // Get image as bitmap
                    val savedBitmap =
                        result.data?.data?.getImage(requireContext())!!.modifyOrientation(
                            result.data?.data!!.getPath(requireContext())
                        )!!

                    // Analyze image. If image is not an correct jaw, then
                    // show an error.
                    try {
                        ImageAnalyzer(
                            context = requireContext(),
                            bitmap = savedBitmap,
                        ) { label, isImageCorrect, _, finalImage ->
                            if (!listOfSelectedJaws.contains(label) || !isImageCorrect) {
                                showError(
                                    getString(
                                        R.string.the_selected_picture_is_incorrect,
                                        getNextDetectedJaw()
                                    ),
                                    false
                                )
                                getCurrentProgressBar().goneWithAnimation()
                                getCurrentCapturedJaw().enable()
                            } else {
                                // Update current captured jaw
                                setCurrentDetectedJaw(label, true)
                                // Insert current captured image
                                setLastCapturedJaw(finalImage!!)
                                saveCapturedJaws(finalImage)
                                currentCapturedImage = finalImage
                                showInsertedImageBasedOnSelectedJaws()
                                insertImageFromGalleryBasedOnJaw(label)
                                // Upload or update image in checkup
                                addImageToCheckup()
                            }
                        }.analyze()
                    } catch (e: java.lang.Exception) {
                        //Firebase.crashlytics.recordException(e)
                        getCurrentProgressBar().goneWithAnimation()
                        getCurrentCapturedJaw().enable()
                        showError(
                            getString(
                                R.string.the_selected_picture_is_incorrect,
                                getCurrentDetectedJaw()
                            ), false
                        )
                    }

                    binding.layoutInsertImage.root.visibleWithAnimation()
                }
            }

        /** Listener for storage permission */
        readStoragePermissionListener =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it)
                    openGalleryForPickImage()
            }

        /** Listener for camera permission */
        cameraPermissionListener =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it)
                    startCamera()
                else
                    findNavController().popBackStack()
            }
    }

    /** Open gallery for user to select an image*/
    private fun openGalleryForPickImage() =
        pickImageFromGalleryResultLauncher.launch(Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        })


    /** Check for read storage permission to open gallery*/
    private fun checkForReadStoragePermission() =
        if (!hasReadStoragePermissions(requireContext()))
            readStoragePermissionListener.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        else
            openGalleryForPickImage()

    override fun onResume() {
        super.onResume()
        // Request permissions each time the app resumes, since they can be revoked at any time
        if (!hasCameraPermissions(requireActivity())) {
            cameraPermissionListener.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    /** Convenience method used to check if all permissions required for camera are granted */
    private fun hasCameraPermissions(context: Context) = cameraPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Convenience method used to check if all permissions required for read storage are granted */
    private fun hasReadStoragePermissions(context: Context) = readStoragePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupGuideTour() {

        binding.root.doOnPreDraw {
            val uploadPhotosTarget = Target.Builder()
                .setAnchor(binding.imageButtonUpload)
                .setShape(Circle((20).dp(requireContext()).toFloat()))
                .setDescription(getString(R.string.tap_here_to_upload_tooth_photos))
                .notClickable(false)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onClick() {
                        spotLight.show(1)
                    }

                })
                .showCasePosition(ShowCasePosition.BottomCenter)
                .build()

            val takePhotosTarget = Target.Builder()
                .setAnchor(binding.takePhotoSpotlightTarget)
                .setShape(Circle((20).dp(requireContext()).toFloat()))
                .setDescription(getString(R.string.you_should_put_your_main_camera_in_front_of_your_mouth_to_detect_and_take_a_photo))
                .notClickable(false)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onClick() {
                        spotLight.finish()
                        guideTourViewModel.cameraGuideTourIsFinished()
                    }
                })
                .showCasePosition(ShowCasePosition.BottomCenter)
                .build()

            spotLight = Spotlight.Builder(requireActivity())
                .setTargets(
                    arrayListOf(
                        uploadPhotosTarget,
                        takePhotosTarget
                    )
                )
                .setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryOpacity95
                    )
                )
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onSkip() {
                        spotLight.finish()
                        guideTourViewModel.cameraGuideTourIsFinished()
                    }
                })
                .build()
            spotLight.start()
        }
    }

    companion object {
        private const val Front = "front"
        private const val Upper = "upper"
        private const val Lower = "lower"
    }
}