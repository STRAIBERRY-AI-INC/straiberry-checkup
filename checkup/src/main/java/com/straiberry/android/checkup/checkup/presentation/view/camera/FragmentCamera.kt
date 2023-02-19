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
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.view.help.FragmentCheckupInstructionDialog
import com.straiberry.android.checkup.common.extentions.*
import com.straiberry.android.checkup.databinding.FragmentCameraBinding
import com.straiberry.android.checkup.di.*
import com.straiberry.android.common.custom.spotlight.*
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.model.JawPosition
import com.straiberry.android.core.base.*
import java.lang.Exception


class FragmentCamera : JawDetector(), IsolatedKoinComponent {
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

            stopDetectionModel()
            hideLayoutChoosePhoto()

            // Show checkup instruction
            if (checkupHelpViewModel.shouldShowCheckupHelp())
                FragmentCheckupInstructionDialog().show(childFragmentManager, "")
            else {
                showLayoutChoosePhoto()
                if (guideTourViewModel.getGuideTourStatus().cameraGuideTour.not())
                    CameraSpotLights(
                        binding,
                        requireContext(),
                        spotLight,
                        requireActivity(),
                        guideTourViewModel
                    ).setSpotLights()
            }

            // Observe dismissing checkup instruction
            jawDetectionViewModel.submitStateDismissCheckupInstruction.observe(viewLifecycleOwner) {
                if (it) {
                    showLayoutChoosePhoto()
                    if (guideTourViewModel.getGuideTourStatus().cameraGuideTour.not())
                        CameraSpotLights(
                            binding,
                            requireContext(),
                            spotLight,
                            requireActivity(),
                            guideTourViewModel
                        ).setSpotLights()
                }
            }

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
            binding.layoutTakePhotoOrChoose.buttonCancel.onClick {
                jawDetectionViewModel.resetNumberOfUploadedJaw()
                checkupQuestionViewModel.resetSelectedJaw()
                jawDetectionViewModel.resetSelectedJaw()
                stopDetectionModel()
                findNavController().popBackStack()
            }

            // Setup button close
            binding.textViewClose.onClick {
                jawDetectionViewModel.resetNumberOfUploadedJaw()
                checkupQuestionViewModel.resetSelectedJaw()
                jawDetectionViewModel.resetSelectedJaw()
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
                setupNextJaw(JawPosition.FrontTeeth)
            }

            binding.layoutInsertImage.imageViewUpperJaw.onClick {
                hideAllGuide()
                binding.imageViewUpperSample.visibleWithAnimation()
                setupNextJaw(JawPosition.UpperJaw)
            }

            binding.layoutInsertImage.imageViewLowerJaw.onClick {
                hideAllGuide()
                binding.imageViewLowerSample.visibleWithAnimation()
                setupNextJaw(JawPosition.LowerJaw)
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
                        && listOfSelectedJaws.contains(jawRecognition.label.convertLabelToJawType())
                        && jawRecognition.confidence >= MinimumConfidence
                    ) {

                        showImagesThatUserMustCapture()
                        takePhotoFromDetectedJaw(jawRecognition.label)
                    }
                })

            jawDetectionViewModel.submitStateCapturedImageIsCorrect.observe(viewLifecycleOwner) { correctCapturedImage ->
                if (correctCapturedImage.isCorrect) {
                    setCurrentDetectedJaw(correctCapturedImage.label)
                    setupNextJaw()
                    currentCapturedImage = correctCapturedImage.capturedImage
                    setLastCapturedJaw(correctCapturedImage.capturedImage!!)
                    saveCapturedJaws(correctCapturedImage.capturedImage)
                    startCapturedImageTransaction()
                    addImageToCheckup()
                }
            }
        }.root
    }

    private fun hideAllGuide() {
        binding.apply {
            imageViewLowerSample.goneWithAnimation()
            imageViewUpperSample.goneWithAnimation()
            imageViewFrontSample.goneWithAnimation()
        }
    }

    private fun setupNextJaw(nextJaw: JawPosition) {
        setCurrentDetectedJaw(nextJaw)
        setNextDetectedJaw(nextJaw)
        startDetectionModel()
        clearView()
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun  onCreate(savedInstanceState: Bundle?) {
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
                            isCheckingImageFromGallery = true,
                            checkingFrame = false
                        ) { label, score, _, _, finalImage ->
                            if (!listOfSelectedJaws.contains(label.convertLabelToJawType()) || score < MinimumConfidence) {
                                showError(
                                    getString(
                                        R.string.the_selected_picture_is_incorrect,
                                        getNextDetectedJaw()
                                    ),
                                    false
                                )
                            } else {
                                // Update current captured jaw
                                setCurrentDetectedJaw(label.convertLabelToJawType(), true)
                                // Insert current captured image
                                setLastCapturedJaw(finalImage!!)
                                saveCapturedJaws(finalImage)
                                currentCapturedImage = finalImage
                                // Upload or update image in checkup
                                addImageToCheckup()
                                showInsertedImageBasedOnSelectedJaws()
                                insertImageFromGalleryBasedOnJaw(label)

                            }
                        }.analyze()
                    } catch (e: Exception) {
                        Firebase.crashlytics.recordException(e)
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

    @androidx.camera.core.ExperimentalGetImage
    override fun  onResume() {
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

    companion object {
        private const val Front = "front"
        private const val Upper = "upper"
        private const val Lower = "lower"
    }
}