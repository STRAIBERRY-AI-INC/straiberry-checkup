package com.straiberry.android.checkup.checkup.presentation.view.checkup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.CreateCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.SdkTokenSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupGuideTourViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CreateCheckupViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.UserInfoViewModel
import com.straiberry.android.checkup.common.extentions.convertToCheckupType
import com.straiberry.android.checkup.common.helper.SdkAuthorizationHelper
import com.straiberry.android.checkup.common.helper.StraiberryCheckupSdkInfo
import com.straiberry.android.checkup.common.helper.TokenKeys
import com.straiberry.android.checkup.databinding.FragmentCheckupBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.custom.spotlight.OnSpotlightListener
import com.straiberry.android.common.custom.spotlight.OnTargetListener
import com.straiberry.android.common.custom.spotlight.ShowCasePosition
import com.straiberry.android.common.custom.spotlight.Spotlight
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.helper.FirebaseAppEvents
import com.straiberry.android.common.helper.ResizeViewWithAnimation
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCheckup : Fragment(), IsolatedKoinComponent {


    private lateinit var binding: FragmentCheckupBinding
    private lateinit var spotLight:Spotlight
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val createCheckupViewModel by viewModel<CreateCheckupViewModel>()
    private val guideTourViewModel by viewModel<CheckupGuideTourViewModel>()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private var selectedCheckupIndex = CheckupType.Regular
    private var selectedCheckupEventName = ""
    private var tokenKeys = TokenKeys()
    private var isSpotlightShowing = false
    private var buttonGoPositionX = 0f
    private var buttonGoPositionY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupBinding.inflate(inflater, container, false).also {
            binding = it

            // Getting checkup sdk token
            tokenKeys = StraiberryCheckupSdkInfo.getTokenInfo()
            userInfoViewModel.setUserName(StraiberryCheckupSdkInfo.getDisplayName())
            userInfoViewModel.setUserAvatar(
                requireContext(),
                StraiberryCheckupSdkInfo.getUserAvatar()
            )
            createCheckupViewModel.getCheckupSdkToken(tokenKeys.appId, tokenKeys.packageName)
            createCheckupViewModel.submitStateCheckupSdkToken.subscribe(
                viewLifecycleOwner,
                ::handleViewStateGetCheckupSdkToken
            )

            // Setup go to checkup
            binding.imageButtonGo.onClick {
                // Send selected type of checkup to firebase and log current event
//                FirebaseAppEvents.onSelectedCheckup(selectedCheckupEventName)
                // Create the checkup
                createCheckupViewModel.createCheckup(
                    selectedCheckupIndex
                )
                createCheckupViewModel.submitStateCreateCheckup.subscribe(
                    viewLifecycleOwner,
                    ::handleViewStateCreateCheckup
                )

                binding.targetDoCheckup.apply {
                    viewTreeObserver
                        .addOnGlobalLayoutListener(object :
                            ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                // Get the real position on screen
                                val posXY = IntArray(2)
                                getLocationInWindow(posXY)
                                // Move indicator
                                buttonGoPositionX = posXY[0] + binding.imageButtonGo.width / 2f
                                buttonGoPositionY = posXY[1] + binding.imageButtonGo.height / 2f

                                binding.targetDoCheckup.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )
                            }
                        })
                }

                if (isSpotlightShowing) {
                    spotLight.finish()
                    guideTourViewModel.checkupGuideTourIsFinished()
                }
            }

            if (guideTourViewModel.getGuideTourStatus().checkupGuideTour.not()) {
                setupGuideTour()
                isSpotlightShowing = true
            }

        }.root
    }

    override fun onResume() {
        super.onResume()
        setupView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden.not())
            if (guideTourViewModel.getGuideTourStatus().checkupGuideTour.not()) {
                setupGuideTour()
                isSpotlightShowing = true
            }
        setupView()
    }

    private fun setupView() {
        binding.radioGroup.clearCheck()
        binding.lottieAnimation.visibleWithAnimation()
        binding.imageViewQuestionDescription.hideWithAnimation()
        resetImageButtonGo()
        val listOfFireBaseEventName = listOf(
            RegularCheckupEvent,
            TeethWhiteningEvent,
            ToothacheAndToothSensitivityEvent,
            ProblemWithPreviousTreatmentEvent,
            CheckupOthersEvent,
            CheckupXraysEvent
        )
        // Setup radio button
        val listOfDescription = listOf(
            binding.textViewDescriptionRegularCheckup,
            binding.textViewDescriptionTeethWhitening,
            binding.textViewDescriptionToothSensitivity,
            binding.textViewDescriptionProblemsTreatment,
            binding.textViewDescriptionOthers,
            binding.textViewDescriptionXRay
        )
        listOf(
            binding.radioButtonRegularCheckup,
            binding.radioButtonWhiteningTest,
            binding.radioButtonToothache,
            binding.radioButtonGeneralProblem,
            binding.radioButtonTreatmentIssue,
            binding.radioButtonXray
        ).forEachIndexed { index, appCompatRadioButton ->
            // Show hide description
            listOfDescription[index].hideWithoutAnimation()
            appCompatRadioButton.setOnCheckedChangeListener { _, isChecked ->
                binding.lottieAnimation.hideWithAnimation()
                binding.imageViewQuestionDescription.visibleWithAnimation()
                // Show hide description
                listOfDescription[index].hideWithoutAnimation()
                if (!binding.imageButtonGo.isClickable)
                    animationImageButtonGotToHomeScaleUp()
                if (isChecked) {
                    binding.root.post {
                        binding.root.fullScroll(View.FOCUS_DOWN)
                    }
                    appCompatRadioButton.isChecked = true
                    selectedCheckupEventName = listOfFireBaseEventName[index]
                    selectedCheckupIndex = (index - 1).convertToCheckupType()
                    listOfDescription[index].visibleWithAnimation()
                    // Set selected checkup
                    chooseCheckupViewModel.setSelectedCheckup(appCompatRadioButton.text.toString())
                    // Set selected checkup index
                    chooseCheckupViewModel.setSelectedCheckupIndex(CheckupType.values()[index])
                }
            }
        }
    }


    /** Handel view state for create a new checkup */
    private fun handleViewStateCreateCheckup(loadable: Loadable<CreateCheckupSuccessModel>) {
        if (loadable != Loading) showHideLoading(false)
        when (loadable) {
            is Success -> {
                chooseCheckupViewModel.setCheckupId(loadable.data.checkupId)
                setupNext(selectedCheckupIndex)
            }
            is Failure -> {
                val m = loadable.throwable
            }
            Loading -> showHideLoading(true)
            NotLoading -> {
            }
        }
    }

    /** Handel view state for getting sdk token */
    private fun handleViewStateGetCheckupSdkToken(loadable: Loadable<SdkTokenSuccessModel>) {
        if (loadable is Success) {
            SdkAuthorizationHelper(requireContext()).setToken(
                requireContext(),
                loadable.data.access,
                loadable.data.refresh
            )
            StraiberryCheckupSdkInfo.setToken(loadable.data.access)
        }
    }

    private fun showHideLoading(showLoading: Boolean) = if (showLoading) {
        binding.imageButtonGo.goneWithAnimation()
        binding.progressBar.visibleWithAnimation()
    } else {
        binding.imageButtonGo.visibleWithAnimation()
        binding.progressBar.goneWithAnimation()
    }

    /**
     * If user choose one of the "toothache and tooth sensitivity"
     * or "problems with previous treatment" type, then send user to checkup question page
     * otherwise send user to camera page.
     * */
    private fun setupNext(index: CheckupType) {
        when (index) {
            CheckupType.XRays -> findNavController().navigate(R.id.action_fragmentCheckup_to_fragmentXRay)
            CheckupType.Sensitivity, CheckupType.Treatments -> findNavController().navigate(R.id.action_fragmentCheckup_to_fragmentCheckupQuestion)
            else -> findNavController().navigate(R.id.action_fragmentCheckup_to_fragmentCamera)
        }
        // User comes from checkup and goes to questions
        chooseCheckupViewModel.userComeFromDentalIssue(false)
    }

    /**
     * Increase with and height of image button go, with animation
     */
    private fun animationImageButtonGotToHomeScaleUp() {
        binding.buttonCheckup.animate().alpha(0f)
        ResizeViewWithAnimation(
            binding.imageButtonGo,
            ImageButtonGoSize.toFloat(),
            ImageButtonGoStartHeight.toFloat(),
            ImageButtonGoSize.toFloat(),
            ImageButtonStartWidth.toFloat(),
            AnimationDurationForTranslation
        ).apply {
            binding.imageButtonGo.apply {
                imageButtonGoHeight = height
                imageButtonGoWidth = width
                animate().alpha(1f)
                isClickable = true
            }.startAnimation(this)
        }
        if (isSpotlightShowing) {
            spotLight.next()
        }
    }

    private fun resetImageButtonGo() {
        binding.buttonCheckup.alpha = 1f
        binding.imageButtonGo.apply {
            layoutParams.height = imageButtonGoHeight
            layoutParams.width = imageButtonGoWidth
            alpha = 0f
            isClickable = false
        }
    }

    private fun setupGuideTour() {
        binding.root.doOnPreDraw {
            isSpotlightShowing = true
            val selectCheckupTarget = Target.Builder()
                .setAnchor(binding.radioGroup)
                .setShape(Circle((180).dp(requireContext()).toFloat()))
                .setDescription(getString(R.string.tap_here_and_select_a_checkup))
                .showCasePosition(ShowCasePosition.TopCenter)
                .build()

            val doCheckupTarget = Target.Builder()
                .setAnchor(binding.targetDoCheckup)
                .setShape(Circle((30).dp(requireContext()).toFloat()))
                .setDescription(getString(R.string.tap_here_and_run_checkup))
                .showCasePosition(ShowCasePosition.TopLeft)
                .build()

            spotLight = Spotlight.Builder(requireActivity())
                .setTargets(arrayListOf(selectCheckupTarget, doCheckupTarget))
                .setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.straiberry.android.common.R.color.primaryOpacity95
                    )
                )
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onSkip() {
                        spotLight.finish()
                        guideTourViewModel.checkupGuideTourIsFinished()
                    }
                })
                .build()
            spotLight.start()
        }

    }

    companion object {
        private const val CHECKUP_X_RAY_TYPE = 4
        private const val CHECKUP_TOOTH_SENSITIVITY_TYPE = 1
        private const val CHECKUP_PREVIOUS_TREATMENT_TYPE = 2
        private const val RegularCheckupEvent = "regular_checkup"
        private const val TeethWhiteningEvent = "teeth_whitening"
        private const val ToothacheAndToothSensitivityEvent = "toothache_and_tooth_sensitivity"
        private const val ProblemWithPreviousTreatmentEvent = "problem_with_previous_treatment"
        private const val CheckupOthersEvent = "others"
        private const val CheckupXraysEvent = "x-rays"
        private var imageButtonGoHeight = 0
        private var imageButtonGoWidth = 0
        private const val AnimationDurationForTranslation = 800L
        private val ImageButtonGoSize = 58.dp
        private val ImageButtonGoStartHeight = 38.dp
        private val ImageButtonStartWidth = 100.dp
    }
}

