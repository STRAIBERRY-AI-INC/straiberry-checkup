package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.util.LayoutDirection
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.checkup.checkup.presentation.view.questions.FragmentCheckupQuestion.Companion.AllQuestions
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.checkup.presentation.viewmodel.*
import com.straiberry.android.checkup.databinding.FragmentCheckupQuestionBinding
import com.straiberry.android.common.custom.spotlight.OnTargetListener
import com.straiberry.android.common.custom.spotlight.ShowCasePosition
import com.straiberry.android.common.custom.spotlight.Spotlight
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.helper.FirebaseAppEvents
import com.straiberry.android.common.model.JawPosition
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel


enum class ButtonCancelFunctional { IsClose, IsCancel, IsBack }

open class CheckupQuestion : Fragment(), CheckupQuestionContract {

    lateinit var binding: FragmentCheckupQuestionBinding
    lateinit var spotLight: Spotlight

    val checkupGuideTourViewModel by viewModel<CheckupGuideTourViewModel>()
    val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    val chooseCheckupTypeViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    val remoteDentalIssueViewModel by viewModel<RemoteDentalIssueViewModel>()
    val dentalIssuesViewModel by viewModel<DentalIssuesViewModel>()

    var isSpotlightShowing = false

    // List of all teeth
    lateinit var teethList: List<ImageButton>

    // List of all edit/delete layout
    lateinit var layoutEditDeleteList: List<ConstraintLayout>

    // List of all tooth indicator
    lateinit var indicatorList: List<CardView>

    // List of all completed tooth
    val listOfSelectedTeeth = ArrayList<Int>()

    // List of all dental issues
    private val listOfDentalIssues = ArrayList<DentalIssueQuestionsModel>()

    // Check for current edit/delete is visible so user can hide it with another touch on tooth
    private var isEditAndDeleteLayoutVisible = false

    private var lastSelectedForEdit = EMPTY

    // Get the last selected tooth
    private var lastSelectedTooth = EMPTY

    private var isUpperJawSelected = false

    var isToothInEditMode = false

    var isUserAddedADentalIssue = false

    /**
     * Show questions box
     */
    fun showQuestions() {
        // Hide unnecessary view
        showQuestionBox()
        // change selected tooth index
        checkupQuestionViewModel.selectedTooth(lastSelectedTooth)
        // Change button cancel to close
        setupButtonCancel(ButtonCancelFunctional.IsClose)
        // Move primary indicator to last selected tooth
        movePrimaryIndicator()
        // Hide image button next with animation
        animateImageButtonNext(false)
        // Move indicator to left,center of title box with animation
        animateIndicatorAlongsideQuestionTitleBox()
        // Disable all teeth
        disableAllTooth(teethList)
        // Hide all completed tooth
        hideAllSelectedTooth()
        // Show last selected indicator witch is incomplete
        indicatorList[lastSelectedTooth].visibleWithAnimation()
    }


    override fun animateImageButtonNext(isShow: Boolean) {
        if (!isShow) {
            binding.imageButtonGo.apply {
                gone()
                translationX = -(binding.imageButtonGo.width * 2).toFloat()
                alpha = ZERO_ALPHA
            }
        } else {
            binding.imageButtonGo.animate()
                .translationX(ZERO_ALPHA)
                .alpha(FULL_ALPHA)
                .setDuration(ANIMATION_DURATION)
                .start()
        }
    }

    /**
     * Hide lower jaw and move lower jaw to center
     */
    override fun inactiveUpperJaw() {
        binding.apply {
            layoutLowerJaw.root.gone()
            textViewUpperJawTitle.visible()
            layoutUpperJaw.root.animate().translationY(-(15).dp(requireContext()).toFloat()).start()
        }
    }

    /**
     * Hide lower jaw and move upper jaw to center
     */
    override fun inactiveLowerJaw() {
        binding.apply {
            layoutUpperJaw.root.gone()
            textViewLowerJawTitle.visible()
            layoutLowerJaw.root.animate().translationY((20).dp(requireContext()).toFloat()).start()
        }
    }

    /**
     * If user selects on of sub answer then move title box to top
     */
    override fun moveQuestionTitleBox(PagePosition: Int) {
        when (PagePosition) {
            0 -> checkupQuestionViewModel.submitStateAnswerOneIsSubAnswer.observe(viewLifecycleOwner,
                {
                    if (it) {
                        binding.viewWithSubAnswer.visible()
                    } else {
                        binding.viewWithSubAnswer.gone()
                    }
                })
            1 -> checkupQuestionViewModel.submitStateAnswerTwoIsSubAnswer.observe(viewLifecycleOwner,
                {
                    if (it) {
                        binding.viewWithSubAnswer.visible()
                    } else {
                        binding.viewWithSubAnswer.gone()
                    }
                })
            2 -> {
                binding.viewWithSubAnswer.gone()
            }
        }
    }

    /**
     * Move primary indicator to last selected teeth
     */
    override fun movePrimaryIndicator() {
        indicatorList[lastSelectedTooth].apply {
            hideWithAnimation()
            viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // Get the real position on screen
                        val posXY = IntArray(NUMBER_OF_INT_ARRAY)
                        getLocationInWindow(posXY)
                        // Move indicator
                        var indicatorHeight = 0
                        var indicatorWidth = 0
                        if (isUpperJawSelected) {
                            indicatorHeight = binding.indicator.height
                            indicatorWidth = binding.indicator.width / 2
                        } else {
                            indicatorHeight = binding.indicator.height / 2
                            indicatorWidth = binding.indicator.width / 2
                        }
                        binding.indicator.apply {
                            x =
                                posXY.first().toFloat() - indicatorWidth
                            y = posXY.last().toFloat() - indicatorHeight
                        }
                        indicatorList[lastSelectedTooth].viewTreeObserver.removeOnGlobalLayoutListener(
                            this
                        )
                    }
                })
        }
    }

    /**
     * When title box is moving to top or bottom then move indicator
     */
    override fun moveIndicatorAlongsideQuestionTitleBox() {
        // Check for device direction and calculate the x for indicator
        // If layout direction is rtl then indicator must be in start of question box
        // otherwise indicator must be at the end of question box
        val measureX = if (ViewCompat.getLayoutDirection(binding.root) == LayoutDirection.LTR)
            (binding.indicator.width / 2)
        else
            -(binding.cardViewQuestionTitle.width - binding.indicator.width / 2)


        binding.cardViewQuestionTitle.viewTreeObserver
            .addOnGlobalLayoutListener {
                // Get the real position on screen
                val posXY = IntArray(NUMBER_OF_INT_ARRAY)
                binding.cardViewQuestionTitle.getLocationOnScreen(posXY)
                binding.indicator.apply {
                    x = posXY.first().toFloat() - measureX
                    y = binding.cardViewQuestionTitle.y
                }
            }
    }

    /**
     * Animate primary indicator from last selected teeth to
     * left center of title box
     */
    override fun animateIndicatorAlongsideQuestionTitleBox() {
        // Check for device direction and calculate the x for indicator
        // If layout direction is rtl then indicator must be in start of question box
        // otherwise indicator must be at the end of question box
        val measureX = if (ViewCompat.getLayoutDirection(binding.root) == LayoutDirection.LTR)
            (binding.indicator.width / 2)
        else
            -(binding.cardViewQuestionTitle.width - binding.indicator.width / 2)

        binding.cardViewQuestionTitle.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Get the real position on screen
                    val posXY = IntArray(NUMBER_OF_INT_ARRAY)
                    binding.cardViewQuestionTitle.getLocationOnScreen(posXY)
                    // Animate the alpha
                    binding.indicator.animate()
                        .alpha(FULL_ALPHA)
                        .setStartDelay(ANIMATION_DELAY_100)
                        .setDuration(ANIMATION_DURATION)
                        .start()
                    // Animate transition
                    binding.indicator.animate()
                        .x(posXY.first().toFloat() - measureX)
                        .y(binding.cardViewQuestionTitle.y)
                        .setStartDelay(ANIMATION_DELAY_800)
                        .setDuration(ANIMATION_DURATION)
                        .withEndAction { moveIndicatorAlongsideQuestionTitleBox() }
                        .start()
                    binding.cardViewQuestionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }

    /**
     * Scale up selected tooth with animation
     */
    override fun animateToothScaleUp(imageButton: ImageButton) {
        imageButton.apply {
            isSelected = true
            isEnabled = false
            animate().scaleY(SCALE_Y_UP).start()
        }
    }

    /**
     * Scale down unselected tooth with animation
     */
    override fun animateToothScaleDown(imageButton: ImageButton) {
        imageButton.apply {
            isSelected = false
            isEnabled = true
            animate().scaleY(SCALE_Y_DOWN).start()
        }
    }

    /**
    Setup three type for cancel button:
    Close-> closing the questions
    Back-> remove selected tooth
    Cancel-> back to previous page
     */
    override fun setupButtonCancel(buttonCancelFunctional: ButtonCancelFunctional) {
        when (buttonCancelFunctional) {
            // Close
            ButtonCancelFunctional.IsClose -> {
                binding.textViewCancel.apply {
                    text = getString(R.string.close)
                    onClick {
                        if (!listOfSelectedTeeth.contains(lastSelectedTooth))
                            checkupQuestionViewModel.removeCanceledAnswers(lastSelectedTooth)

                        if (listOfSelectedTeeth.isNotEmpty())
                            showHideDone(true)

                        indicatorList[lastSelectedTooth].hideWithAnimation()
                        enableAllSelectedToothIndicator()
                        animateToothScaleDown(teethList[lastSelectedTooth])
                        hideQuestionBox()
                        setupButtonCancel(ButtonCancelFunctional.IsCancel)
                        checkupQuestionViewModel.resetSubAnswer()
                        checkupQuestionViewModel.resetSelectedJaw()
                    }
                }
            }
            // Cancel
            ButtonCancelFunctional.IsCancel -> binding.textViewCancel.apply {
                text = getString(R.string.cancel)
                onClick {
                    checkupQuestionViewModel.removeAllAnswers()
                    findNavController().popBackStack()
                }
            }
            // Back
            ButtonCancelFunctional.IsBack -> binding.textViewCancel.apply {
                text = getString(R.string.back)
                disableAllSelectedToothIndicator()
                onClick {
                    setupButtonCancel(ButtonCancelFunctional.IsCancel)
                    if (listOfSelectedTeeth.isNotEmpty())
                        showHideDone(true)
                    enableAllSelectedToothIndicator()
                    showEditAndDeleteLayout()
                    if (listOfSelectedTeeth.contains(lastSelectedTooth).not()) {
                        indicatorList[lastSelectedTooth].hideWithAnimation()
                        animateToothScaleDown(teethList[lastSelectedTooth])
                    }
                    animateImageButtonNext(false)
                }
            }
        }
    }

    private fun disableAllSelectedToothIndicator() {
        listOfSelectedTeeth.forEach { indicatorList[it].disable() }
    }

    private fun enableAllSelectedToothIndicator() {
        listOfSelectedTeeth.forEach { indicatorList[it].enable() }
    }

    /**
     * Remove all saved answers when back button is pressed.
     */
    override fun onDetach() {
        super.onDetach()
        checkupQuestionViewModel.removeAllAnswers()
    }


    fun setupViewPager() {
        val titleList = listOf(
            getString(R.string._1_how_long_has_it_been_hurting),
            getString(R.string.check_question_title_two),
            getString(R.string.checkup_question_title_three)
        )
        binding.viewPager.adapter = AdapterCheckupQuestionSlidePager(
            childFragmentManager,
            lifecycle
        )
        TabLayoutMediator(binding.tabLayoutDots, binding.viewPager) { _, _ -> }.attach()
        checkupQuestionViewModel.resetAnswer()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Based on selected page move question box if
                // sub answer is selected
                //moveQuestionTitleBox(position)
                // Change title based on page position
                binding.textViewQuestionTitle.apply {
                    alpha = ZERO_ALPHA
                    animate().alpha(FULL_ALPHA).duration =
                        ANIMATION_DURATION
                    // Change size of text
                    if (position == 1)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)
                    else
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)
                    text = titleList[position]
                }
            }
        })
    }


    /** Selected tooth gets an elevation and move the indicator */
    fun selectTooth() {
        teethList.forEachIndexed { index, imageButton ->
            if (listOfSelectedTeeth.contains(index).not())
                imageButton.onClick {
                    // Reset all sub answers
                    checkupQuestionViewModel.resetSubAnswer()
                    inactiveTooth(teethList)
                    showAllSelectedTooth()
                    animateImageButtonNext(true)
                    showHideDone(false)
                    activeIndicator(index)
                    checkupQuestionViewModel.resetAnswer()
                    hideAllEditDeleteLayout()
                    hideReselectedTooth()
                    isEditAndDeleteLayoutVisible = false
                    lastSelectedTooth = index
                    isUpperJawSelected = index <= 15
                    binding.apply {
                        imageButtonGo.visible()
                        layoutDone.gone()
                    }
                    if (isSpotlightShowing) {
                        val posXY = IntArray(NUMBER_OF_INT_ARRAY)
                        teethList[lastSelectedTooth].getLocationOnScreen(posXY)
                        createGuideTarget(posXY.first().toFloat(), posXY.last().toFloat())
                    }
                    animateToothScaleUp(imageButton)
                    // Got to next guide tour
                    if (isSpotlightShowing)
                        spotLight.show(1)
                    // If no tooth is selected then change button cancel to "back"
                    if (listOfSelectedTeeth.isNotEmpty())
                        setupButtonCancel(ButtonCancelFunctional.IsBack)
                }
        }
    }

    private fun createGuideTarget(x: Float, y: Float) {
        val editDeleteToothTarget = Target.Builder()
            .setAnchor(x - (20).dp / 2, y - (25).dp)
            .setShape(Circle((20).dp(requireContext()).toFloat()))
            .setDescription(getString(R.string.tap_here_to_edit_or_delete_pain_tooth))
            .notClickable(false)
            .setOnTargetListener(object : OnTargetListener {
                override fun onClick() {
                    spotLight.show(4)
                }

            })
            .showCasePosition(ShowCasePosition.TopCenter)
            .build()
        spotLight.addTarget(editDeleteToothTarget, 3)
    }

    /**
     * Hide tooth when it's incomplete and user reselect it.
     */
    private fun hideReselectedTooth() {
        indicatorList.forEachIndexed { index, cardView ->
            cardView.onClick {
                if (index == lastSelectedTooth
                    && listOfSelectedTeeth.contains(lastSelectedTooth).not()
                ) {
                    cardView.apply {
                        isSelected = false
                        hideWithAnimation()
                    }
                    if (listOfSelectedTeeth.isNotEmpty())
                        showHideDone(true)
                    animateToothScaleDown(teethList[lastSelectedTooth])
                    animateImageButtonNext(false)
                    enableAllSelectedToothIndicator()
                    showEditAndDeleteLayout()
                    setupButtonCancel(ButtonCancelFunctional.IsCancel)
                }

            }
        }
    }

    fun disableSelectedTooth() {
        listOfSelectedTeeth.forEach { teethList[it].disable() }
    }

    /** Active indicator where teeth is selected and inactive others  */
    override fun activeIndicator(index: Int) {
        indicatorList.forEachIndexed { index_, cardView ->
            if (index_ == index)
                cardView.visibleWithAnimation()
            else if (!listOfSelectedTeeth.contains(index_))
                cardView.hideWithAnimation()
        }
    }


    /**
     * Inactive the selected tooth
     */
    override fun inactiveTooth(teethList: List<ImageButton>) =
        teethList.forEachIndexed { index, imageButton ->
            if (index == lastSelectedTooth)
                animateToothScaleDown(imageButton)
        }

    /**
     * Enable all teeth
     */
    override fun enableAllTooth(teethList: List<ImageButton>) =
        teethList.forEachIndexed { index, imageButton ->
            if (listOfSelectedTeeth.contains(index).not())
                imageButton.enable()
        }

    /**
     * Disable all teeth
     */
    override fun disableAllTooth(teethList: List<ImageButton>) =
        teethList.forEach {
            it.disable()
        }


    /**
     * Show all completed tooth
     */
    override fun showAllSelectedTooth() {
        listOfSelectedTeeth.forEach {
            animateToothScaleUp(teethList[it])
            indicatorList[it].apply {
                isSelected = true
                visibleWithAnimation()
            }
        }
    }


    /**
     * Hide all completed tooth
     */
    override fun hideAllSelectedTooth() {
        listOfSelectedTeeth.forEach {
            animateToothScaleDown(teethList[it])
            indicatorList[it].apply {
                isSelected = false
                hideWithAnimation()
            }
        }
    }


    /**
     * When user click on a completed tooth then show edit,delete button
     */
    override fun showEditAndDeleteLayout() {
        listOfSelectedTeeth.forEachIndexed { index, selectedToothIndex ->
            indicatorList[selectedToothIndex].isClickable = false
            teethList[selectedToothIndex].apply { enable() }.onClick {
                // First hide all the visible edit/delete layout
                hideAllEditDeleteLayout()
                layoutEditDeleteList[selectedToothIndex].apply {

                    // Switch between show/hide the edit/delete layout
                    // check if edit/delete layout already showing on selected tooth
                    // if so, then hide
                    lastSelectedForEdit = if (lastSelectedForEdit != selectedToothIndex) {
                        visibleWithAnimation()
                        selectedToothIndex
                    } else {
                        hideWithAnimation()
                        EMPTY
                    }

                    this.findViewById<TextView>(R.id.textViewEdit)
                        .onClick { editTooth(selectedToothIndex) }
                    this.findViewById<TextView>(R.id.textViewDelete)
                        .onClick { deleteTooth(selectedToothIndex) }
                }
            }
        }
    }

    override fun hideAllEditDeleteLayout() {
        layoutEditDeleteList.forEach { it.hideWithAnimation() }
    }


    override fun editTooth(element: Int) {
        isUpperJawSelected = element <= 15
        hideAllEditDeleteLayout()
        // Change last selected tooth to this element
        lastSelectedForEdit = element
        lastSelectedTooth = element
        // Edit/delete layout is not visible
        isEditAndDeleteLayoutVisible = false
        // Move primary indicator to last selected tooth for edit
        movePrimaryIndicator()
        animateToothScaleDown(teethList[lastSelectedTooth])
        // Layout is in edit mode
        isToothInEditMode = true
        // Hide edit, delete layout
        layoutEditDeleteList[element].hideWithAnimation()
        // Change current selected tooth
        checkupQuestionViewModel.selectedTooth(element)
        setupViewPager()
        showQuestions()
        animateToothScaleUp(teethList[element])
    }


    override fun deleteTooth(element: Int) {
        hideAllEditDeleteLayout()
        // Remove all answers
        checkupQuestionViewModel.removeCanceledAnswers(element)
        // Reset all sub answers
        checkupQuestionViewModel.resetSubAnswer()
        // Remove tooth from list of selected tooth
        listOfSelectedTeeth.remove(element)
        // Hide edit delete layout
        layoutEditDeleteList[element].hideWithAnimation()
        // Scale down deleted tooth
        animateToothScaleDown(teethList[element])
        // Delete dental issue if its questions
        deleteDentalIssue(element)
        // Hide deleted tooth indicator
        indicatorList[element].apply {
            isSelected = false
            hideWithAnimation()
        }
        // If list of selected tooth is empty then hide "Done" layout
        if (listOfSelectedTeeth.isEmpty())
            showHideDone(false)

        selectTooth()
    }


    /**
     * When user complete a selected tooth then show done layout,
     * otherwise hide it.
     */
    override fun showHideDone(isDone: Boolean) {
        if (isDone) {
            binding.apply {
                animateImageButtonNext(false)
                layoutDone.visible()
                textViewSelectToothTitle.visible()
                textViewSelectToothTitle.text = getText(R.string.any_other_teeth_hurt)
            }
        } else
            binding.apply {
                animateImageButtonNext(true)
                layoutDone.gone()
                textViewSelectToothTitle.visible()
                textViewSelectToothTitle.text = getText(R.string.please_select_a_tooth_that_hurts)
            }
    }


    /** User is answered to all three questions for selected tooth
     * */
    fun questionIsDone() {
        // Add tooth to list of selected tooth
        listOfSelectedTeeth.add(lastSelectedTooth)
        // Hide question layout
        hideQuestionBox()
        // Change back button to cancel
        setupButtonCancel(ButtonCancelFunctional.IsCancel)
        // Show "Done" layout
        showHideDone(true)
        // Show's a layout that user can edit or delete a tooth
        showEditAndDeleteLayout()
    }

    /** If user came from questions then save all dental issue that user selects in local */
    override fun saveDentalIssue(toothId: Int) {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            if (checkupQuestionViewModel.submitStateIsAllThreeAnswer.value == AllQuestions) {
                val dentalIssue = DentalIssueQuestionsModel(
                    remoteTeethId = toothId,
                    teethId = lastSelectedTooth,
                    answerOneIndex = checkupQuestionViewModel.getLocalAnswerOne(lastSelectedTooth),
                    answerTwoIndex = checkupQuestionViewModel.getLocalAnswerTwo(lastSelectedTooth),
                    answerThreeIndex = checkupQuestionViewModel.getLocalAnswerThree(
                        lastSelectedTooth
                    )
                )
                dentalIssuesViewModel.addDentalIssue(dentalIssue)
                listOfDentalIssues.add(dentalIssue)
            }
        }
    }

    /** Deleting a dental issue from local */
    override fun deleteDentalIssue(toothId: Int) {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            dentalIssuesViewModel.deleteDentalIssues(toothId)
        }
    }

    /** If user came from questions then show all dental issues */
    override fun getAllDentalIssues() {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            dentalIssuesViewModel.getDentalIssues()
            dentalIssuesViewModel.submitStateGetDentalIssues.observe(viewLifecycleOwner, {
                if (it is ReadableSuccess) {
                    it.data.forEach { dentalIssues ->
                        listOfSelectedTeeth.add(dentalIssues.teethId)
                        checkupQuestionViewModel.saveAnswerOne(
                            dentalIssues.answerOneIndex,
                            dentalIssues.teethId
                        )
                        checkupQuestionViewModel.saveAnswerTwo(
                            dentalIssues.answerTwoIndex,
                            dentalIssues.teethId
                        )
                        checkupQuestionViewModel.saveAnswerThree(
                            dentalIssues.answerThreeIndex,
                            dentalIssues.teethId
                        )
                    }
                    listOfDentalIssues.clear()
                    listOfDentalIssues.addAll(it.data)
                    showAllSelectedTooth()
                    enableAllSelectedToothIndicator()
                    showEditAndDeleteLayout()
                }
            })
        }
    }

    /** Delete dental issue in remote */
    override fun remoteDeleteDentalIssue(toothId: Int) {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            remoteDentalIssueViewModel.remoteDeleteDentalIssue(toothId)
            remoteDentalIssueViewModel.submitStateDeleteDentalIssue.observe(viewLifecycleOwner) {
                showHideLoading(it == Loading)
            }
        }
    }

    /** Update dental issue in remote */
    override fun remoteUpdateDentalIssue(dentalIssue: DentalIssueQuestionsModel) {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            remoteDentalIssueViewModel.remoteUpdateDentalIssue(
                toothId = dentalIssue.remoteTeethId,
                toothNumber = dentalIssue.teethId.convertToothIdToDental(),
                duration = checkupQuestionViewModel.getAnswerOne(lastSelectedTooth),
                cause = checkupQuestionViewModel.getAnswerTwo(lastSelectedTooth),
                pain = checkupQuestionViewModel.getAnswerThree(lastSelectedTooth)
            )
            remoteDentalIssueViewModel.submitStateUpdateDentalIssue.observe(viewLifecycleOwner) {
                showHideLoading(it == Loading)
            }
        }
    }

    /** Save dental issue in remote */
    override fun remoteSaveDentalIssue() {
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
            remoteDentalIssueViewModel.remoteAddDentalIssue(
                toothNumber = lastSelectedTooth.convertToothIdToDental(),
                duration = checkupQuestionViewModel.getAnswerOne(lastSelectedTooth),
                cause = checkupQuestionViewModel.getAnswerTwo(lastSelectedTooth),
                checkupQuestionViewModel.getAnswerThree(lastSelectedTooth)
            )
            remoteDentalIssueViewModel.submitStateAddDentalIssue.observe(viewLifecycleOwner, {
                showHideLoading(it == Loading)
                when (it) {
                    is Failure -> {
                    }
                    Loading -> {
                    }
                    NotLoading -> {
                    }
                    is Success -> {
                        // Save in local
                        saveDentalIssue(it.data.toothId)
                        // Log and event when user created a dental issue
                        FirebaseAppEvents.onCreatedDentalIssue()
                    }
                }
            })
        }
    }

    /** When tooth is in edit mode then just update dental issue in local
     * and remote. If it's not then save dental issue in remote.
     * */
    fun updateOrSaveDentalIssue() {
        if (listOfSelectedTeeth.lastOrNull { it == lastSelectedTooth } != null) {
            isUserAddedADentalIssue = true
            if (isToothInEditMode) {
                val updatedDentalIssue = listOfDentalIssues.last { it.teethId == lastSelectedTooth }
                remoteUpdateDentalIssue(updatedDentalIssue)
                saveDentalIssue(updatedDentalIssue.remoteTeethId)
            } else {
                remoteSaveDentalIssue()
            }
        }
    }

    fun showHideLoading(showLoading: Boolean) = if (showLoading) {
        binding.buttonDoTheCheckup.goneWithAnimation()
        binding.progressBar.visibleWithAnimation()
    } else {
        binding.buttonDoTheCheckup.visibleWithAnimation()
        binding.progressBar.goneWithAnimation()
    }

    override fun hideQuestionBox() {
        // Show all selected tooth
        showAllSelectedTooth()
        // Active the jaw
        if (isUpperJawSelected) activeUpperJaw() else activeLowerJaw()
        // Enable all teeth
        enableAllTooth(teethList)
        enableAllSelectedToothIndicator()
        showEditAndDeleteLayout()
        // Check if user comes from dental issue
        if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!)
        // Update/Save dental issue
            updateOrSaveDentalIssue()
        // Hide/show views
        binding.apply {
            viewPager.gone()
            tabLayoutDots.gone()
            textViewSelectToothTitle.visible()
            layoutGuidLine.visible()
            textViewUpperJawTitle.gone()
            textViewLowerJawTitle.gone()
            cardViewQuestionTitle.gone()
            indicator.hideWithAnimation()
            isToothInEditMode = false
            // Visible both jaw
            binding.apply {
                layoutLowerJaw.root.visible()
                layoutUpperJaw.root.visible()
            }
        }
    }

    /**
     * Change the position of upper jaw to top, bottom of
     * text view title
     */
    override fun activeUpperJaw() {
        binding.layoutUpperJaw.root.animate().translationY(0F).start()
    }

    /**
     * Change the position of lower jaw to top, bottom of
     * upper jaw layout
     */
    override fun activeLowerJaw() {
        binding.layoutLowerJaw.root.animate().translationY(0F).start()
    }

    override fun showQuestionBox() {
        binding.apply {
            textViewSelectToothTitle.gone()
            layoutDone.gone()
            layoutGuidLine.gone()
            viewPager.visible()
            tabLayoutDots.visible()
            indicator.visible()
            cardViewQuestionTitle.visible()
            // Active selected jaw
            if (isUpperJawSelected) inactiveUpperJaw() else inactiveLowerJaw()
        }
    }

    override fun selectedJawForCheckup() {
        var isUpper = false
        var isLower = false
        var isFrontUpper = false
        var isFrontLower = false

        listOfSelectedTeeth.forEach {
            if (it in 3..12)
                isFrontUpper = true
            else if (it in 20..28)
                isFrontLower = true
            if (it in 0..2 || it in 13..15)
                isUpper = true
            if (it in 16..19 || it in 29..32)
                isLower = true
        }

        if (isFrontLower || isFrontUpper)
            checkupQuestionViewModel.selectedJawForCheckup(FRONT_JAW, JawPosition.FrontTeeth)
        if (isUpper || isFrontUpper)
            checkupQuestionViewModel.selectedJawForCheckup(UPPER_JAW, JawPosition.UpperJaw)
        if (isLower || isFrontLower)
            checkupQuestionViewModel.selectedJawForCheckup(LOWER_JAW, JawPosition.LowerJaw)
    }

    companion object {
        private const val ANIMATION_DURATION = 800L
        private const val ANIMATION_DELAY_800 = 800L
        private const val ANIMATION_DELAY_100 = 100L
        private const val ZERO_ALPHA = 0f
        private const val FULL_ALPHA = 1f
        private const val NUMBER_OF_INT_ARRAY = 2
        private const val ZERO = 0
        private const val SCALE_Y_UP = 1.1f
        private const val SCALE_Y_DOWN = 1f
        private const val EMPTY = -1
    }
}
