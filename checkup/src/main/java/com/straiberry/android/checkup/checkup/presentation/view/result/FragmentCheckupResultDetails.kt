package com.straiberry.android.checkup.checkup.presentation.view.result

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayoutMediator
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupImageType
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.*
import com.straiberry.android.checkup.common.extentions.*
import com.straiberry.android.checkup.common.extentions.convertDentalToToothId
import com.straiberry.android.checkup.common.extentions.convertToToothId
import com.straiberry.android.checkup.common.extentions.convertToothClassToFrontJawPosition
import com.straiberry.android.checkup.common.extentions.dp
import com.straiberry.android.checkup.databinding.FragmentCheckupResultDetailsBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.model.JawPosition


class FragmentCheckupResultDetails : Fragment(), ToothProblemClick, IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupResultDetailsBinding
    private val checkupResultProblemIllustrationViewModel by activityViewModels<CheckupResultProblemIllustrationViewModel>()
    private val checkupResultProblemRealImageViewModel by activityViewModels<CheckupResultProblemRealImageViewModel>()
    private val checkupResultLastSelectedProblemViewModel by activityViewModels<CheckupResultLastSelectedProblemViewModel>()
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val detectionJawViewModel by activityViewModels<DetectionJawViewModel>()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private lateinit var adapterProblems: AdapterCheckupProblems
    private lateinit var checkupResult: CheckupResultSuccessModel
    private val listOfFrontProblems = arrayListOf<CheckupProblemsModel>()
    private val listOfUpperProblems = arrayListOf<CheckupProblemsModel>()
    private val listOfLowerProblems = arrayListOf<CheckupProblemsModel>()

    private var oralHygieneForUpperJaw = -1
    private var oralHygieneForLowerJaw = -1
    private var oralHygieneForFrontJaw = -1
    private var whiteningScore = 0
    private var lastSelectedItem = 0
    private var isShowIllustration = true
    private var currentFrontPositionIsUpper = true
    private var lastSelectedPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupResultDetailsBinding.inflate(inflater, container, false).also {
            binding = it
            checkupResult = chooseCheckupViewModel.submitStateCheckupResult.value!!

            // Set selected checkup type
            binding.textViewCheckupType.text =
                chooseCheckupViewModel.submitStateSelectedCheckup.value!!

            // Close button
            binding.textViewClose.onClick {
                findNavController().popBackStack()
            }

            getListOfProblems()


            // Getting the oral hygiene score and whitening score from result
            checkupResult.data.images.forEachIndexed { _, image ->
                when {
                    image.imageType == CheckupImageType.XrayJaw -> oralHygieneForFrontJaw =
                        image.result.first().oralHygienScore
                    image.imageType == CheckupImageType.UpperJaw -> oralHygieneForUpperJaw =
                        image.result.first().oralHygienScore
                    image.imageType == CheckupImageType.LowerJaw -> oralHygieneForLowerJaw =
                        image.result.first().oralHygienScore
                    image.imageType == CheckupImageType.FrontJaw -> {
                        oralHygieneForFrontJaw = image.result.first().oralHygienScore
                        whiteningScore = image.result.first().whiteningScore
                    }
                }

            }

            // Switch between real image and illustration
            binding.textViewSwitchToReal.onClick {
                isShowIllustration = !isShowIllustration
                // Change text based on current position
                binding.textViewSwitchToReal.text = if (isShowIllustration)
                    getString(R.string.show_real_image)
                else
                    getString(R.string.show_illustration)
                deactivateLowerAndUpperState(isShowIllustration)
                // Setup view pager again
                setupViewPager()
                binding.viewPager.currentItem = lastSelectedPage
            }

            // Switch between upper and lower in front teeth
            binding.imageViewLowerJawState.onClick {
                checkoutFrontTeethLower()
                unselectProblemAnimation(lastSelectedItem)
                if (listOfFrontProblems.firstOrNull { it.jawType == JawPosition.FrontTeethLower } != null)
                    selectProblemAnimation(checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontLowerIllustration.value!!)
            }
            binding.imageViewUpperJawState.onClick {
                checkoutFrontTeethUpper()
                unselectProblemAnimation(lastSelectedItem)
                if (listOfFrontProblems.firstOrNull { it.jawType == JawPosition.FrontTeethUpper } != null)
                    selectProblemAnimation(checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!)
            }

            // Show first jaw based on first problem
            val firstJawPosition =
                if (listOfFrontProblems.isNotEmpty())
                    listOfFrontProblems.first().jawType
                else
                    checkupResult.data.images.first().imageType!!.convertToJawPosition()

            // Check if first jaw is one of front teeth jaws
            currentFrontPositionIsUpper =
                firstJawPosition == JawPosition.FrontTeethUpper || firstJawPosition == JawPosition.UpperJaw

            binding.textViewJawType.text =
                checkupResult.data.images.first().imageType!!.convertSelectedJawToString(
                    requireContext()
                )


            checkForEmptyList(firstJawPosition)
            setupViewPager()
        }.root
    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset all live data
        checkupResultLastSelectedProblemViewModel.resetAll()
        checkupResultProblemRealImageViewModel.resetAll()
        checkupResultProblemIllustrationViewModel.resetAll()
        detectionJawViewModel.resetSelectedJaw()
        chooseCheckupViewModel.resetCheckupResult()
        detectionJawViewModel.resetNumberOfUploadedJaw()
    }

    private fun checkoutFrontTeethUpper() {
        checkupResultProblemIllustrationViewModel.switchBetweenFrontJaws(JawPosition.FrontTeethUpper)
        checkupResultProblemIllustrationViewModel.setCurrentPosition(JawPosition.FrontTeethUpper)
        binding.imageViewUpperJawState.isSelected = true
        binding.imageViewLowerJawState.isSelected = false
        currentFrontPositionIsUpper = true
        setupRecyclerViewProblems(JawPosition.FrontTeethUpper)
    }

    private fun checkoutFrontTeethLower() {
        checkupResultProblemIllustrationViewModel.switchBetweenFrontJaws(JawPosition.FrontTeethLower)
        checkupResultProblemIllustrationViewModel.setCurrentPosition(JawPosition.FrontTeethLower)
        binding.imageViewUpperJawState.isSelected = false
        binding.imageViewLowerJawState.isSelected = true
        currentFrontPositionIsUpper = false
        setupRecyclerViewProblems(JawPosition.FrontTeethLower)
    }

    private fun getListOfProblems() {

        checkupResult.data.images.forEach { checkupImages ->

            if (checkupImages.imageType == CheckupImageType.FrontJaw || checkupImages.imageType == CheckupImageType.XrayJaw) {
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfFrontProblems.isNotEmpty())
                        listOfFrontProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
                                chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                requireContext()
                            ) && it.jawType == problems.toothClass.first().convertDentalToToothId()
                                .convertToothClassToFrontJawPosition()
                        }
                    else
                        arrayListOf()

                    if (filterFront.isNotEmpty())
                        filterFront.first().apply {
                            this.listOfToothIndex.addAll(problems.toothClass.convertToToothId())
                            this.listOfToothPosition.add(
                                Pair(
                                    problems.xCenter,
                                    problems.yCenter
                                )
                            )
                            this.problemCount = this.listOfToothIndex.size
                        }
                    else if (problems.conf != 0.0)
                        listOfFrontProblems.add(
                            CheckupProblemsModel(
                                problems.cavityClass.convertCavityClassToDrawable(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.cavityClass.convertCavityClassToString(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.toothClass.size,
                                problems.toothClass.convertToToothId(),
                                arrayListOf(
                                    Pair(
                                        problems.xCenter,
                                        problems.yCenter
                                    )
                                ),
                                problems.toothClass.first().convertDentalToToothId()
                                    .convertToothClassToFrontJawPosition()
                            )
                        )

                }
            } else if (checkupImages.imageType == CheckupImageType.UpperJaw)
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfUpperProblems.isNotEmpty())
                        listOfUpperProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
                                chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                requireContext()
                            )
                        }
                    else
                        arrayListOf()

                    if (filterFront.isNotEmpty())
                        filterFront.first().apply {
                            this.listOfToothIndex.addAll(problems.toothClass.convertToToothId())
                            this.listOfToothPosition.add(
                                Pair(
                                    problems.xCenter,
                                    problems.yCenter
                                )
                            )
                            this.problemCount = this.listOfToothIndex.size
                        }
                    else if (problems.conf != 0.0)
                        listOfUpperProblems.add(
                            CheckupProblemsModel(
                                problems.cavityClass.convertCavityClassToDrawable(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.cavityClass.convertCavityClassToString(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.toothClass.size,
                                problems.toothClass.convertToToothId(),
                                arrayListOf(
                                    Pair(
                                        problems.xCenter,
                                        problems.yCenter
                                    )
                                ),
                                JawPosition.UpperJaw
                            )
                        )

                }
            else if (checkupImages.imageType == CheckupImageType.LowerJaw)
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfLowerProblems.isNotEmpty())
                        listOfLowerProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
                                chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                requireContext()
                            )
                        }
                    else
                        arrayListOf()

                    if (filterFront.isNotEmpty())
                        filterFront.first().apply {
                            this.listOfToothIndex.addAll(problems.toothClass.convertToToothId())
                            this.listOfToothPosition.add(
                                Pair(
                                    problems.xCenter,
                                    problems.yCenter
                                )
                            )
                            this.problemCount = this.listOfToothIndex.size
                        }
                    else if (problems.conf != 0.0)
                        listOfLowerProblems.add(
                            CheckupProblemsModel(
                                problems.cavityClass.convertCavityClassToDrawable(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.cavityClass.convertCavityClassToString(
                                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!,
                                    requireContext()
                                ),
                                problems.toothClass.size,
                                problems.toothClass.convertToToothId(),
                                arrayListOf(
                                    Pair(
                                        problems.xCenter,
                                        problems.yCenter
                                    )
                                ),
                                JawPosition.LowerJaw
                            )
                        )

                }
        }

    }

    private fun listOfProblemsIsEmpty(isEmpty: Boolean, jawPosition: JawPosition) =
        if (isEmpty)
            binding.apply {
                recyclerViewProblems.goneWithAnimation()
                textViewProblemMainTitle.goneWithAnimation()
                textViewProblemDescribe.goneWithAnimation()
                textviewNoProblemFound.visibleWithAnimation()
                textviewNoProblemFound.text = when (jawPosition) {
                    JawPosition.FrontTeethLower ->
                        getString(R.string.no_problem_found_in_lower_jaw_of_your_front_teeth)
                    JawPosition.FrontTeethUpper ->
                        getString(R.string.no_problem_found_in_upper_jaw_of_front_teeth)
                    else -> getString(
                        R.string.empty_problems_on_checkup_result,
                        userInfoViewModel.userName.value,
                        textViewJawType.text.toString().lowercase()
                    )
                }
            }
        else
            binding.apply {
                textviewNoProblemFound.goneWithAnimation()
                recyclerViewProblems.visibleWithAnimation()
                textViewProblemMainTitle.visibleWithAnimation()
                textViewProblemDescribe.visibleWithAnimation()
            }


    private fun checkForEmptyList(currentPosition: JawPosition) {

        // Get the correct list for each jaw position
        val currentList = when (currentPosition) {
            JawPosition.FrontTeethLower -> listOfFrontProblems.filter { it.jawType == JawPosition.FrontTeethLower }
            JawPosition.FrontTeethUpper -> listOfFrontProblems.filter { it.jawType == JawPosition.FrontTeethUpper }
            JawPosition.FrontTeeth -> listOfFrontProblems
            JawPosition.UpperJaw -> listOfUpperProblems
            JawPosition.LowerJaw -> listOfLowerProblems
        }

        // Hide recyclerview if one of front jaws has no problem
        if (currentList.isEmpty())
            if (currentPosition == JawPosition.FrontTeethLower || currentPosition == JawPosition.FrontTeethUpper) {
                if (listOfFrontProblems.isEmpty())
                    listOfProblemsIsEmpty(true, JawPosition.FrontTeeth)
                else
                    listOfProblemsIsEmpty(true, currentPosition)
            } else
                listOfProblemsIsEmpty(true, currentPosition)
        else
            listOfProblemsIsEmpty(false, currentPosition)
    }

    private fun setupRecyclerViewProblems(currentPosition: JawPosition) {
        // Get the correct list for each jaw position
        val currentList = when (currentPosition) {
            JawPosition.FrontTeethLower -> listOfFrontProblems.filter { it.jawType == JawPosition.FrontTeethLower }
            JawPosition.FrontTeethUpper -> listOfFrontProblems.filter { it.jawType == JawPosition.FrontTeethUpper }
            JawPosition.FrontTeeth -> listOfFrontProblems
            JawPosition.UpperJaw -> listOfUpperProblems
            JawPosition.LowerJaw -> listOfLowerProblems
        }

        checkForEmptyList(currentPosition)

        // Setup recyclerview
        binding.recyclerViewProblems.apply {
            adapterProblems = AdapterCheckupProblems(
                context = requireContext(),
                data = currentList,
                toothProblemClick = this@FragmentCheckupResultDetails,
                checkupResultLastSelectedProblemViewModel = checkupResultLastSelectedProblemViewModel,
                currentJawPosition = currentPosition,
                isIllustration = isShowIllustration
            )
            adapter = adapterProblems
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

    }

    /**
     * Set the problem description base on selected problem
     */
    private fun setupCheckupProblemDescription(selectedProblem: Int) {
        listOf(
            getString(R.string.amalgam_filling_problem_description),
            getString(R.string.calcules_problem_description),
            getString(R.string.carries_problem_description),
            getString(R.string.white_spot_problem_description),
            getString(R.string.amalgam_restoration_overhang_problem_description),
            getString(R.string.bone_loss_problem_description),
            getString(R.string.dental_impant_problem_description),
            getString(R.string.infectious_apical_lesion_problem_description),
            getString(R.string.malposed_wisdom_tooth_problem_description),
            getString(R.string.remained_dental_root_problem_description),
            getString(R.string.tooth_with_missing_counterpart_problem_description)
        ).forEachIndexed { index, s ->
            if (index == selectedProblem)
                binding.textViewProblemDescribe.text = s
        }
    }

    /**
     * Show or hide the upper/lower state if current position is not
     * front teeth.
     */
    private fun showUpperAndLowerState(isShow: Boolean) = if (isShow) {
        binding.apply {
            imageViewLowerJawState.visibleWithAnimation()
            imageViewUpperJawState.visibleWithAnimation()
        }
    } else
        binding.apply {
            imageViewLowerJawState.hideWithAnimation()
            imageViewUpperJawState.hideWithAnimation()
        }

    /**
     * When real image is selected to show problem then lower/upper switch
     * state must be disabled.
     */
    private fun deactivateLowerAndUpperState(isActive: Boolean) = if (isActive) {
        binding.apply {
            if (currentFrontPositionIsUpper)
                imageViewUpperJawState.isSelected = true
            else
                imageViewLowerJawState.isSelected = true
            imageViewLowerJawState.isEnabled = true
            imageViewUpperJawState.isEnabled = true
        }
    } else {
        binding.apply {
            imageViewLowerJawState.isEnabled = false
            imageViewLowerJawState.isSelected = false
            imageViewUpperJawState.isEnabled = false
            imageViewUpperJawState.isSelected = false
        }
    }


    private fun setupViewPager() {
        // Set the adapter based on *isShowIllustration*
        binding.viewPager.adapter =
            if (isShowIllustration) AdapterCheckupProblemIllustrationSlidePager(
                detectionJawViewModel.submitStateSelectedJaws.value!!,
                childFragmentManager,
                lifecycle
            )
            else
                AdapterCheckupProblemRealImageSlidePager(
                    detectionJawViewModel.submitStateSelectedJaws.value!!,
                    childFragmentManager,
                    lifecycle
                )

        TabLayoutMediator(binding.tabLayoutDots, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                lastSelectedPage = position
                // Get the current jaw based on current position
                val currentSelectedJawBasedOnPosition =
                    detectionJawViewModel.submitStateSelectedJaws.value?.toList()
                        ?.get(position)?.second

                val currentPosition =
                    if (currentSelectedJawBasedOnPosition == JawPosition.FrontTeeth
                    ) {
                        if (isShowIllustration)
                            if (currentFrontPositionIsUpper)
                                JawPosition.FrontTeethUpper
                            else
                                JawPosition.FrontTeethLower
                        else
                            JawPosition.FrontTeeth
                    } else {
                        currentSelectedJawBasedOnPosition
                    }


                // Change title with animation based on page position
                binding.textViewJawType.apply {
                    changeTextWithAnimation()
                    text = currentSelectedJawBasedOnPosition!!.convertSelectedJawToString(
                        requireContext()
                    )
                }

                // Setting oral hygiene score based on selected jaw
                val isXray =
                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value == CheckupType.XRays
                when (currentPosition) {
                    JawPosition.UpperJaw -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForUpperJaw.convertToOralHygieneScore(isXray)
                    }
                    JawPosition.LowerJaw -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForLowerJaw.convertToOralHygieneScore(isXray)
                    }
                    JawPosition.FrontTeeth -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore(isXray)
                    }
                    JawPosition.FrontTeethUpper -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore(isXray)
                    }
                    JawPosition.FrontTeethLower -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore(isXray)
                    }
                    null -> {
                    }
                }

                // Show upper\lower switch only in front teeth page
                showUpperAndLowerState(
                    currentPosition == JawPosition.FrontTeethLower
                            || currentPosition == JawPosition.FrontTeethUpper
                            || currentPosition == JawPosition.FrontTeeth
                )

                // Rest recycler view when current jaw is not frontTeethLower or frontTeethUpper
                when (currentPosition) {
                    JawPosition.FrontTeethUpper -> checkoutFrontTeethUpper()
                    JawPosition.FrontTeethLower -> checkoutFrontTeethLower()
                    else -> setupRecyclerViewProblems(currentPosition!!)
                }

                checkupResultProblemIllustrationViewModel.setCurrentPosition(currentPosition)
            }
        })
    }


    override fun toothProblemOnCLick(
        position: Int,
        listOfToothIndex: ArrayList<Int>,
        listOfToothPosition: ArrayList<Pair<Double, Double>>,
        problemTitle: String,
        jawType: JawPosition
    ) {
        // Change problem title
        binding.textViewProblemMainTitle.text = problemTitle
        // Change problem description
        setupCheckupProblemDescription(problemTitle.convertCavityClassToIntPosition(requireContext()))
        // unselect other problems with animation
        unselectProblemAnimation(lastSelectedItem)
        // Apply animation to current selected problem
        selectProblemAnimation(position)
        checkupResultProblemIllustrationViewModel.resetAll()
        checkupResultProblemIllustrationViewModel.setCurrentPosition(jawType)

        // Save selected problem for each position and send list to show the teeth
        when (checkupResultProblemIllustrationViewModel.submitStateToothCurrentPosition.value!!) {
            JawPosition.FrontTeeth -> {
                checkupResultProblemRealImageViewModel.setToothWithProblemsFrontTeeth(
                    listOfToothPosition
                )
                checkupResultLastSelectedProblemViewModel.setSelectedProblemFrontRealImage(
                    position
                )
            }
            JawPosition.FrontTeethUpper -> {
                checkupResultProblemRealImageViewModel.setToothWithProblemsFrontTeeth(
                    listOfToothPosition
                )
                checkupResultProblemIllustrationViewModel.setToothWithProblemsFrontTeethUpper(
                    listOfToothIndex
                )
                checkupResultLastSelectedProblemViewModel.setSelectedProblemFrontUpperIllustration(
                    position
                )
            }
            JawPosition.FrontTeethLower -> {
                checkupResultProblemRealImageViewModel.setToothWithProblemsFrontTeeth(
                    listOfToothPosition
                )
                checkupResultProblemIllustrationViewModel.setToothWithProblemsFrontTeethLower(
                    listOfToothIndex
                )
                checkupResultLastSelectedProblemViewModel.setSelectedProblemFrontLowerIllustration(
                    position
                )
            }
            JawPosition.UpperJaw -> {
                checkupResultProblemIllustrationViewModel.setToothWithProblemsUpperJaw(
                    listOfToothIndex
                )
                checkupResultProblemRealImageViewModel.setToothWithProblemsUpperJaw(
                    listOfToothPosition
                )
                if (isShowIllustration)
                    checkupResultLastSelectedProblemViewModel.setSelectedProblemUpperIllustration(
                        position
                    )
                else
                    checkupResultLastSelectedProblemViewModel.setSelectedProblemUpperRealImage(
                        position
                    )
            }
            JawPosition.LowerJaw -> {
                checkupResultProblemIllustrationViewModel.setToothWithProblemsLowerJaw(
                    listOfToothIndex
                )
                checkupResultProblemRealImageViewModel.setToothWithProblemsLowerJaw(
                    listOfToothPosition
                )
                if (isShowIllustration)
                    checkupResultLastSelectedProblemViewModel.setSelectedProblemLowerIllustration(
                        position
                    )
                else
                    checkupResultLastSelectedProblemViewModel.setSelectedProblemLowerRealImage(
                        position
                    )
            }
        }

    }


    /**
     * Apply animation to selected problem
     */
    private fun selectProblemAnimation(position: Int) {
        binding.recyclerViewProblems.postDelayed({
            try {
                // Find view in a position in recyclerview
                val itemView =
                    binding.recyclerViewProblems.layoutManager!!.findViewByPosition(position)
                val cardViewToothProblem: MaterialCardView =
                    itemView!!.findViewById(R.id.cardViewToothProblem)
                val textViewProblemTitle: TextView =
                    itemView.findViewById(R.id.textViewProblemTitle)
                val textViewProblemCount: TextView =
                    itemView.findViewById(R.id.textViewProblemCount)

                textViewProblemTitle.goneWithAnimation()
                textViewProblemCount.goneWithAnimation()
                cardViewToothProblem.isSelected = true
                textViewProblemCount.isEnabled = true
                textViewProblemTitle.isEnabled = true

                // Animation
                ValueAnimator.ofInt(
                    cardViewToothProblem.width,
                    MinimumSizeOfProblemBox
                )
                    .apply {
                        duration = AnimationDuration
                        addUpdateListener {
                            cardViewToothProblem.layoutParams.width =
                                it.animatedValue as Int
                            cardViewToothProblem.requestLayout()
                        }
                        doOnEnd {
                            ValueAnimator.ofInt(
                                cardViewToothProblem.width,
                                MaximumSizeOfProblemBox
                            )
                                .apply {
                                    duration = AnimationDuration
                                    startDelay = AnimationDuration
                                    addUpdateListener {
                                        cardViewToothProblem.layoutParams.width =
                                            it.animatedValue as Int
                                        cardViewToothProblem.requestLayout()
                                    }
                                    doOnEnd {
                                        textViewProblemTitle.visibleWithAnimation()
                                        textViewProblemCount.visibleWithAnimation()
                                    }
                                }.start()
                        }
                    }.start()
                lastSelectedItem = position

            } catch (e: Exception) {
            }
        }, 80)

    }

    private fun unselectProblemAnimation(position: Int) {
        binding.recyclerViewProblems.postDelayed({
            val itemView = binding.recyclerViewProblems.layoutManager!!.findViewByPosition(position)
            if (itemView != null) {
                itemView.findViewById<CardView>(R.id.cardViewToothProblem).isSelected = false
                itemView.findViewById<TextView>(R.id.textViewProblemTitle).isEnabled = false
                itemView.findViewById<TextView>(R.id.textViewProblemCount).isEnabled = false
            }
        }, 80)

    }


    companion object {
        const val X_RAY_JAW = 2
        const val FRONT_JAW = 1
        const val UPPER_JAW = -1
        const val LOWER_JAW = 0
        private const val AnimationDuration = 500L
        private val MinimumSizeOfProblemBox = 50.dp
        private val MaximumSizeOfProblemBox = 134.dp
    }
}