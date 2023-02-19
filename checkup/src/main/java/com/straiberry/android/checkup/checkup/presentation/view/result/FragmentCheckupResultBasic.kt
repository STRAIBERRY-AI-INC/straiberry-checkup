package com.straiberry.android.checkup.checkup.presentation.view.result

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupImageType
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.UserInfoViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupResultBasicBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.helper.FirebaseAppEvents
import com.straiberry.android.common.helper.ShareScreenshotHelper
import com.straiberry.android.common.model.JawPosition

class FragmentCheckupResultBasic : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupResultBasicBinding
    private var checkupResult = CheckupResultSuccessModel(CheckupHistorySuccessResponse.Data())

    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    private val jawDetectionViewModel by activityViewModels<DetectionJawViewModel>()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
    private var removeWhiteningScore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupResultBasicBinding.inflate(inflater, container, false).also {
            binding = it

            if (chooseCheckupViewModel.submitStateCheckupResult.value == null)
                findNavController().popBackStack()

            checkupResult = chooseCheckupViewModel.submitStateCheckupResult.value!!
            // Set the type of checkup based on selected choose
            listOf(
                getString(R.string.regular_checkup),
                getString(R.string.whitening_check_up),
                getString(R.string.toothache_amp_tooth_sensitivity_checkup),
                getString(R.string.problems_with_previous_treatment_checkup),
                getString(R.string.others_checkup),
                getString(R.string.x_rays)
            ).forEachIndexed { index, s ->
                if (index == chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!.ordinal)
                    binding.textViewCheckupResultTitle.apply {
                        text = getString(
                            R.string.your_regular_checkup_result,
                            s
                        )
                    }
            }

            // Set whitening and oral hygiene score
            binding.apply {
                textViewOralHygieneScoreCenter.text = checkupResult.data.overalScore
                textViewOralHygieneScore.text = checkupResult.data.overalScore
                textViewWhiteningScore.text =
                    checkupResult.data.whiteningScore.toStringOrZero().toFloat().toInt().toString()
            }

            // Get total problem count and set to textview
            var totalProblemCount = 0
            checkupResult.data.images.forEach { allImages ->
                allImages.result.forEach { result ->
                    if (result.problems.isNotEmpty())
                        result.problems.filter { problems -> problems.conf != ZERO_CONFIDENT }
                            .forEach {
                                totalProblemCount += it.toothClass.count()
                            }
                    else
                        totalProblemCount += 0
                }
            }

            if (totalProblemCount == 0)
                binding.apply {
                    buttonMore.text = getString(R.string.done)
                    textViewCheckupCountProblem.text =
                        getString(R.string.no_problem_found_in_checkup)
                    buttonMore.onClick {
                        findNavController().popBackStack()
                    }
                }
            else {
                // Set total problem
                binding.textViewCheckupCountProblem.gravity = Gravity.CENTER
                binding.textViewCheckupCountProblem.text = SpannableStringBuilder()
                    .append(getString(R.string.in_this_checkup))
                    .append(" ")
                    .color(ContextCompat.getColor(requireContext(), com.straiberry.android.common.R.color.primary)) {
                        append(
                            totalProblemCount.toString()
                        )
                    }
                    .append(" ")
                    .append(getString(R.string.problem_were_found_in_your_teeth))
                // More details
                binding.buttonMore.onClick {
                    jawDetectionViewModel.resetSelectedJaw()
                    // Save witch jaw has been selected by user
                    checkupResult.data.images.forEach {
                        when (it.imageType) {
                            CheckupImageType.FrontJaw -> jawDetectionViewModel.setSelectedJaw(
                                0,
                                JawPosition.FrontTeeth
                            )
                            CheckupImageType.XrayJaw -> jawDetectionViewModel.setSelectedJaw(
                                0,
                                JawPosition.FrontTeeth
                            )
                            CheckupImageType.UpperJaw -> jawDetectionViewModel.setSelectedJaw(
                                1,
                                JawPosition.UpperJaw
                            )
                            CheckupImageType.LowerJaw -> jawDetectionViewModel.setSelectedJaw(
                                2,
                                JawPosition.LowerJaw
                            )
                            else -> {}
                        }
                    }
                    findNavController().navigate(R.id.action_fragmentCheckupResultBasic_to_fragmentCheckupResultDetails)
                }
            }

            // If checkup type is not "regular checkup","other" and if is not include
            // a front teeth, then remove whitening score from layout
            if (chooseCheckupViewModel.submitStateSelectedCheckupIndex.value != CheckupType.Regular
                && chooseCheckupViewModel.submitStateSelectedCheckupIndex.value != CheckupType.Others
            )
            // Check for the checkup that is not include a front teeth
                if (checkupQuestionViewModel.submitStateSelectedJaw.value?.containsValue(JawPosition.FrontTeeth)!!
                        .not() ||
                    chooseCheckupViewModel.submitStateSelectedCheckupIndex.value == CheckupType.XRays
                )
                    removeWhiteningScore()


            // Set description for each oral hygiene score
            setupDescriptionForScores(
                checkupResult.data.overalScore.toStringOrEmpty().convertScoreToChartValue() - 1
            )

            // Setup share the result
            binding.apply {
                shareCheckupResult.setCheckupResult(
                    userAvatar = BitmapDrawable(
                        resources,
                        userInfoViewModel.userAvatarAsBitmap.value
                    ),
                    userName = userInfoViewModel.userName.value!!,
                    checkupResultTitle = textViewCheckupResultTitle.text.toString(),
                    oralHygieneScore = textViewOralHygieneScore.text.toString(),
                    whiteningScore = textViewWhiteningScore.text.toString(),
                    checkupProblems = textViewCheckupCountProblem.text.toString(),
                    checkupTip = textViewTips.text.toString(),
                    removeWhiteningScore = removeWhiteningScore
                )
                imageButtonShare.onClick {
                    // Log event when user shares his/her checkup result
                    FirebaseAppEvents.onShareCheckupResult()
                    ShareScreenshotHelper().shareResult(shareCheckupResult, requireActivity())
                }
            }

        }.root
    }

    /** Hide whitening score when checkup don't have a front teeth */
    private fun removeWhiteningScore() {
        removeWhiteningScore = true
        binding.apply {
            layoutWhiteningScore.gone()
            frameLayoutLine72.gone()
            layoutOralHygieneScore.gone()
            layoutOralHygieneScoreCenter.visible()
        }
    }

    /**
     * Each score has a description and base on the score the description changes.
     * some of them includes the name of user.
     */
    private fun setupDescriptionForScores(scoreIndex: Int) {
        listOf(
            R.string.score_description_a_plus,
            R.string.score_description_a,
            R.string.score_description_a_mines,
            R.string.score_description_b_plus,
            R.string.score_description_b,
            R.string.score_description_a_mines,
            R.string.score_description_c_plus,
            R.string.score_description_c,
            R.string.score_description_c_mines,
            R.string.score_description_d_plus,
            R.string.score_description_d,
            R.string.score_description_d_mines,
        ).reversed().forEachIndexed { index, i ->
            if (index == scoreIndex)
            // Text index that have a parameter
                if (index == 0 || index == 5 || index == 7 || index == 11)
                    binding.textViewTips.text =
                        getString(i, userInfoViewModel.userName.value)
                else
                    binding.textViewTips.text = getString(i)
        }
    }

    companion object {
        const val ZERO_CONFIDENT = 0.0
    }
}