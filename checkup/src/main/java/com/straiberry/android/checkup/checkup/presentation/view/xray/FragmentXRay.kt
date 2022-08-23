package com.straiberry.android.checkup.checkup.presentation.view.xray

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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.XrayViewModel
import com.straiberry.android.checkup.common.extentions.convertToFile
import com.straiberry.android.checkup.common.extentions.getImage
import com.straiberry.android.checkup.databinding.FragmentXRayBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.helper.ResizeViewWithAnimation
import com.straiberry.android.common.model.JawPosition
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentXRay : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentXRayBinding

    private val readStoragePermissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var readStoragePermissionListener: ActivityResultLauncher<String>
    private lateinit var pickImageFromGalleryResultLauncher: ActivityResultLauncher<Intent>

    private val xrayViewModel by viewModel<XrayViewModel>()
    private val checkupResultViewModel by viewModel<CheckupResultViewModel>()
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val detectionJawViewModel by activityViewModels<DetectionJawViewModel>()

    private var isShowingUploadFile = true
    private var layoutIsOnError = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentXRayBinding.inflate(layoutInflater, container, false).also {
            binding = it
            binding.editTextEnterFileUrl.hideEditText()
            binding.textViewSwitch.onClick {
                if (isShowingUploadFile)
                    binding.apply {
                        textViewTitle.text = getString(R.string.add_url)
                        textViewSwitch.text = getString(R.string.upload_file)
                        textViewDescription.text =
                            getString(R.string.please_insert_the_related_link)
                        editTextEnterFileUrl.showEditText()
                        frameLayoutUploadFile.gone()
                        isShowingUploadFile = false
                    }
                else
                    binding.apply {
                        resetImageButtonGo()
                        textViewTitle.text = getString(R.string.upload_file)
                        textViewSwitch.text = getString(R.string.add_url)
                        textViewDescription.text = getString(R.string.upload_x_ray_description)
                        editTextEnterFileUrl.hideEditText()
                        frameLayoutUploadFile.visible()
                        isShowingUploadFile = true
                    }
            }


            binding.textViewBack.onClick {
                if (layoutIsOnError)
                    binding.apply {
                        layoutInvalidOpgFile.goneWithAnimation()
                        frameLayoutUploadFile.visibleWithAnimation()
                        textViewDescription.visibleWithAnimation()
                        textViewSwitch.visibleWithAnimation()
                        textViewTitle.visibleWithAnimation()
                        layoutIsOnError = false
                        isShowingUploadFile = true
                    }
                else
                    findNavController().popBackStack()
            }

            binding.frameLayoutInvalidOpg.onClick {
                if (layoutIsOnError)
                    binding.apply {
                        layoutInvalidOpgFile.goneWithAnimation()
                        frameLayoutUploadFile.visibleWithAnimation()
                        textViewDescription.visibleWithAnimation()
                        textViewSwitch.visibleWithAnimation()
                        textViewTitle.visibleWithAnimation()
                        layoutIsOnError = false
                        isShowingUploadFile = true
                    }
            }

            binding.frameLayoutUploadFile.onClick {
                if (isShowingUploadFile)
                    checkForReadStoragePermission()
            }

            binding.editTextEnterFileUrl.getEditText().doOnTextChanged { text, _, _, _ ->
                if (text!!.isNotEmpty()) {
                    binding.imageButtonSend.visible()
                    binding.textViewSwitch.goneWithAnimation()
                } else {
                    binding.imageButtonSend.gone()
                    binding.textViewSwitch.visibleWithAnimation()
                }
                binding.layoutError.goneWithAnimation()
            }

            binding.imageButtonSend.onClick {
                xrayViewModel.checkXrayUrl(binding.editTextEnterFileUrl.getText())
                xrayViewModel.submitStateCheckXrayUrl.subscribe(
                    viewLifecycleOwner,
                    ::handleViewStateCheckXrayUrl
                )
            }

            binding.buttonShowResult.onClick {
                if (isShowingUploadFile) {
                    checkupResultViewModel.checkupResult(chooseCheckupViewModel.submitStateCreateCheckupId.value!!)
                    checkupResultViewModel.submitStateCheckupResult.subscribe(
                        viewLifecycleOwner,
                        ::handleViewStateGetCheckupResult
                    )
                } else {
                    xrayViewModel.addXrayImageFromUrl(
                        chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                        binding.editTextEnterFileUrl.getText()
                    )
                    xrayViewModel.submitStateAddXrayImageFromUrl.subscribe(
                        viewLifecycleOwner,
                        ::handleViewStateAddXrayImageFromUrlToCheckup
                    )
                }
            }

        }.root
    }

    /** Handel view state for getting checkup result */
    private fun handleViewStateAddXrayImageFromUrlToCheckup(loadable: Loadable<AddImageToCheckupSuccessModel>) {
        if (loadable != Loading) showHideLoadingGetCheckupResult(false)
        when (loadable) {
            is Success -> {
                checkupResultViewModel.checkupResult(chooseCheckupViewModel.submitStateCreateCheckupId.value!!)
                checkupResultViewModel.submitStateCheckupResult.subscribe(
                    viewLifecycleOwner,
                    ::handleViewStateGetCheckupResult
                )
            }
            is Failure -> {
            }
            Loading -> showHideLoadingGetCheckupResult(true)
            NotLoading -> {
            }
        }
    }

    /** Handel view state for getting checkup result */
    private fun handleViewStateGetCheckupResult(loadable: Loadable<CheckupResultSuccessModel>) {
        if (loadable != Loading) showHideLoadingGetCheckupResult(false)
        when (loadable) {
            is Success -> {
                detectionJawViewModel.setSelectedJaw(0, JawPosition.FrontTeeth)
                chooseCheckupViewModel.setCheckupResult(loadable.data)
                findNavController().navigate(R.id.action_fragmentXRay_to_fragmentCheckupResultBasic)
            }
            is Failure -> {
            }
            Loading -> showHideLoadingGetCheckupResult(true)
            NotLoading -> {
            }
        }
    }

    /** Handel view state for checking x-ray url */
    private fun handleViewStateCheckXrayUrl(loadable: Loadable<Unit>) {
        when (loadable) {
            is Success -> {
                binding.apply {
                    textViewTitle.goneWithAnimation()
                    editTextEnterFileUrl.hideEditText()
                    textViewUploadSuccessful.visibleWithAnimation()
                    imageButtonSend.goneWithAnimation()
                    progressCheckUrl.goneWithAnimation()
                    textViewSwitch.goneWithAnimation()
                    textViewDescription.goneWithAnimation()
                    buttonShowResult.visibleWithAnimation()
                }
            }
            is Failure -> {
                showHideLoadingCheckUrl(false)
                binding.layoutError.visibleWithAnimation()
            }
            Loading -> showHideLoadingCheckUrl(true)
            NotLoading -> {
            }
        }
    }

    /** Handel view state for uploading x-ray url */
    private fun handleViewStateUploadXrayImage(loadable: Loadable<AddImageToCheckupSuccessModel>) {
        if (loadable != Loading) binding.uploadAnimation.goneWithAnimation()
        when (loadable) {
            is Success -> {
                binding.apply {
                    textViewTitle.goneWithAnimation()
                    editTextEnterFileUrl.hideEditText()
                    imageButtonSend.goneWithAnimation()
                    progressCheckUrl.goneWithAnimation()
                    textViewSwitch.goneWithAnimation()
                    textViewDescription.goneWithAnimation()
                    buttonShowResult.visibleWithAnimation()
                    finishUploadAnimation.visibleWithAnimation()
                }
            }
            is Failure -> {
                binding.apply {
                    frameLayoutUploadFile.visibleWithAnimation()
                    uploadAnimation.goneWithAnimation()
                    textViewDescription.visibleWithAnimation()
                    textViewSwitch.visibleWithAnimation()
                }
            }
            Loading -> {
                binding.apply {
                    frameLayoutUploadFile.goneWithAnimation()
                    uploadAnimation.visibleWithAnimation()
                    textViewDescription.goneWithAnimation()
                    textViewSwitch.goneWithAnimation()
                }
            }
            NotLoading -> {
            }
        }
    }

    private fun showHideLoadingCheckUrl(show: Boolean) = if (show) {
        binding.imageButtonSend.goneWithAnimation()
        binding.progressCheckUrl.visibleWithAnimation()
    } else {
        binding.imageButtonSend.visibleWithAnimation()
        binding.progressCheckUrl.goneWithAnimation()
    }

    private fun showHideLoadingGetCheckupResult(show: Boolean) = if (show) {
        binding.progressGetResult.visibleWithAnimation()
        binding.buttonShowResult.hideWithAnimation()
    } else {
        binding.buttonShowResult.visibleWithAnimation()
        binding.progressGetResult.goneWithAnimation()
    }

    /**
     * Increase with and height of image button go, with animation
     */
    private fun animationImageButtonGotToHomeScaleUp() {
        ResizeViewWithAnimation(
            binding.imageButtonSend,
            ImageButtonGoSize.toFloat(),
            ImageButtonGoStartHeight.toFloat(),
            ImageButtonGoSize.toFloat(),
            ImageButtonStartWidth.toFloat(),
            AnimationDurationForTranslation
        ).apply {
            binding.imageButtonSend.apply {
                imageButtonGoHeight = height
                imageButtonGoWidth = width
                animate().alpha(1f)
                isClickable = true
            }.startAnimation(this)
        }
    }

    private fun resetImageButtonGo() {
        binding.imageButtonSend.apply {
            layoutParams.height = imageButtonGoHeight
            layoutParams.width = imageButtonGoWidth
            alpha = 0f
            isClickable = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())

        /** Get selected image from gallery */
        pickImageFromGalleryResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val image = result.data!!.data!!.getImage(requireContext())
                    OpgAnalyzer(requireContext(), image) { isOpg, opgImage ->
                        if (isOpg) {
                            xrayViewModel.uploadXrayImage(
                                chooseCheckupViewModel.submitStateCreateCheckupId.value!!,
                                opgImage!!.convertToFile(requireContext())
                            )
                            xrayViewModel.submitStateUploadXrayImage.subscribe(
                                viewLifecycleOwner,
                                ::handleViewStateUploadXrayImage
                            )
                        } else
                            binding.apply {
                                layoutInvalidOpgFile.visibleWithAnimation()
                                frameLayoutUploadFile.goneWithAnimation()
                                uploadAnimation.goneWithAnimation()
                                textViewDescription.goneWithAnimation()
                                textViewSwitch.goneWithAnimation()
                                textViewTitle.goneWithAnimation()
                                isShowingUploadFile = false
                                layoutIsOnError = true
                            }
                    }.analyze()
                }
            }

        /** Listener for storage permission */
        readStoragePermissionListener =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it)
                    openGalleryForPickImage()
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

    /** Convenience method used to check if all permissions required for read storage are granted */
    private fun hasReadStoragePermissions(context: Context) = readStoragePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private var imageButtonGoHeight = 0
        private var imageButtonGoWidth = 0
        private const val AnimationDurationForTranslation = 800L
        private val ImageButtonGoSize = 58.dp
        private val ImageButtonGoStartHeight = 38.dp
        private val ImageButtonStartWidth = 100.dp
    }
}