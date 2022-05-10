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
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.*
import com.straiberry.android.checkup.common.extentions.convertCavityClassToDrawable
import com.straiberry.android.checkup.common.extentions.convertCavityClassToString
import com.straiberry.android.checkup.common.extentions.convertDentalToToothId
import com.straiberry.android.checkup.common.extentions.convertToJawPosition
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
    val listOfFrontProblems = arrayListOf<CheckupProblemsModel>()
    val listOfUpperProblems = arrayListOf<CheckupProblemsModel>()
    val listOfLowerProblems = arrayListOf<CheckupProblemsModel>()

    private var isListOfFrontUpperEmpty = false
    private var isListOfFrontLowerEmpty = false
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
                // Reset all live data
                checkupResultLastSelectedProblemViewModel.resetAll()
                checkupResultProblemIllustrationViewModel.resetAll()
                detectionJawViewModel.resetSelectedJaw()
                chooseCheckupViewModel.resetCheckupResult()
                detectionJawViewModel.resetNumberOfUploadedJaw()
                findNavController().popBackStack()
            }

            getListOfProblems()


            // Getting the oral hygiene score and whitening score from result
            checkupResult.data.images.forEachIndexed { _, image ->
                when {
                    image.imageType.toInt() == UPPER_JAW -> oralHygieneForUpperJaw =
                        image.result.first().oralHygienScore
                    image.imageType.toInt() == LOWER_JAW -> oralHygieneForLowerJaw =
                        image.result.first().oralHygienScore
                    image.imageType.toInt() == FRONT_JAW -> {
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
                    checkupResult.data.images.first().imageType.toInt().convertToJawPosition()

            currentFrontPositionIsUpper =
                firstJawPosition == JawPosition.FrontTeethUpper || firstJawPosition == JawPosition.UpperJaw
            binding.textViewJawType.text = convertSelectedJawToString(
                checkupResult.data.images.first().imageType.toInt().convertToJawPosition()
            )
            setupRecyclerViewProblems(firstJawPosition)
            setupViewPager()
        }.root
    }

    private fun checkoutFrontTeethUpper() {
        checkupResultProblemIllustrationViewModel.switchBetweenFrontJaws(JawPosition.FrontTeethUpper)
        binding.imageViewUpperJawState.isSelected = true
        binding.imageViewLowerJawState.isSelected = false
        currentFrontPositionIsUpper = true
    }

    private fun checkoutFrontTeethLower() {
        checkupResultProblemIllustrationViewModel.switchBetweenFrontJaws(JawPosition.FrontTeethLower)
        binding.imageViewUpperJawState.isSelected = false
        binding.imageViewLowerJawState.isSelected = true
        currentFrontPositionIsUpper = false
    }

    private fun getListOfProblems() {

        checkupResult.data.images.forEach { checkupImages ->

            if (checkupImages.imageType.toInt() == FRONT_JAW || checkupImages.imageType.toInt() == X_RAY_JAW) {
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfFrontProblems.isNotEmpty())
                        listOfFrontProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
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
                                problems.cavityClass.convertCavityClassToDrawable(requireContext()),
                                problems.cavityClass.convertCavityClassToString(requireContext()),
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
            } else if (checkupImages.imageType.toInt() == UPPER_JAW)
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfUpperProblems.isNotEmpty())
                        listOfUpperProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
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
                                problems.cavityClass.convertCavityClassToDrawable(requireContext()),
                                problems.cavityClass.convertCavityClassToString(requireContext()),
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
            else if (checkupImages.imageType.toInt() == LOWER_JAW)
                checkupImages.result.first().problems.forEach { problems ->
                    val filterFront = if (listOfLowerProblems.isNotEmpty())
                        listOfLowerProblems.filter {
                            problems.conf != 0.0 &&
                                    it.title == problems.cavityClass.convertCavityClassToString(
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
                                problems.cavityClass.convertCavityClassToDrawable(requireContext()),
                                problems.cavityClass.convertCavityClassToString(requireContext()),
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


        listOfFrontProblems.forEach { frontProblems ->
            if (frontProblems.listOfToothIndex.count { it > 15 } == 0)
                isListOfFrontLowerEmpty = true
            if (frontProblems.listOfToothIndex.count { it <= 15 } == 0)
                isListOfFrontUpperEmpty = true
        }
    }

    private fun listOfProblemsIsEmpty(isEmpty: Boolean) = if (isEmpty)
        binding.apply {
            recyclerViewProblems.goneWithAnimation()
            textViewProblemMainTitle.goneWithAnimation()
            textViewProblemDescribe.goneWithAnimation()
            textviewEmptyProblems.text = getString(
                R.string.empty_problems_on_checkup_result,
                userInfoViewModel.userName.value,
                textViewJawType.text.toString().lowercase()
            )
            textviewEmptyProblems.visibleWithAnimation()
        }
    else
        binding.apply {
            textviewEmptyProblems.goneWithAnimation()
            recyclerViewProblems.visibleWithAnimation()
            textViewProblemMainTitle.visibleWithAnimation()
            textViewProblemDescribe.visibleWithAnimation()
        }

    private fun setupRecyclerViewProblems(currentPosition: JawPosition) {
        // Get the correct list for each jaw position
        val currentList = when (currentPosition) {
            JawPosition.FrontTeethLower -> listOfFrontProblems
            JawPosition.FrontTeethUpper -> listOfFrontProblems
            JawPosition.FrontTeeth -> listOfFrontProblems
            JawPosition.UpperJaw -> listOfUpperProblems
            JawPosition.LowerJaw -> listOfLowerProblems
        }

        if (currentList.isEmpty())
            listOfProblemsIsEmpty(true)
        else
            listOfProblemsIsEmpty(false)

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
            getString(R.string.tooth_sensitivity_problem_description),
            getString(R.string.cracked_cusp_tooth_problem_description),
            getString(R.string.reversible_pulpitis_problem_description),
            getString(R.string.ireversible_pulpitis_problem_description),
            getString(R.string.necrotic_nonvital_problem_description),
            getString(R.string.tmd_problem_description),
            getString(R.string.white_spot_problem_description),
            getString(R.string.amalgam_filling_problem_description),
            getString(R.string.calcules_problem_description),
            getString(R.string.carries_problem_description),
            getString(R.string.sinus_inflection_problem_description)
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

    private fun convertSelectedJawToString(jawType: JawPosition): String {
        return when (jawType) {
            JawPosition.LowerJaw -> getString(R.string.lower_jaw)
            JawPosition.FrontTeeth -> getString(R.string.front_teeth)
            JawPosition.UpperJaw -> getString(R.string.upper_jaw)
            else -> getString(R.string.lower_jaw)
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
                    text = convertSelectedJawToString(currentSelectedJawBasedOnPosition!!)
                }

                // Setting oral hygiene score based on selected jaw
                when (currentPosition) {
                    JawPosition.UpperJaw -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForUpperJaw.convertToOralHygieneScore()
                    }
                    JawPosition.LowerJaw -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForLowerJaw.convertToOralHygieneScore()
                    }
                    JawPosition.FrontTeeth -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore()
                    }
                    JawPosition.FrontTeethUpper -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore()
                    }
                    JawPosition.FrontTeethLower -> {
                        binding.textViewOralHygieneScore.text =
                            oralHygieneForFrontJaw.convertToOralHygieneScore()
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
                setupRecyclerViewProblems(currentPosition!!)

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
        setupCheckupProblemDescription(position)
        // unselect other problems with animation
        unselectProblemAnimation(lastSelectedItem)
        // Apply animation to current selected problem
        selectProblemAnimation(position)
        checkupResultProblemIllustrationViewModel.setCurrentPosition(jawType)

        // Checkout jaw related to selected problem if user is on illustration
        if (isShowIllustration) {
            if (jawType == JawPosition.FrontTeethUpper)
                checkoutFrontTeethUpper()
            else if (jawType == JawPosition.FrontTeethLower)
                checkoutFrontTeethLower()
        }

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
                checkupResultProblemIllustrationViewModel.setToothWithProblemsFrontTeethUpper(
                    listOfToothIndex
                )
                checkupResultLastSelectedProblemViewModel.setSelectedProblemFrontUpperIllustration(
                    position
                )
            }
            JawPosition.FrontTeethLower -> {
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
        private val MaximumSizeOfProblemBox = 117.dp
    }
}