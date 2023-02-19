package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.domain.model.AddToothToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionSubmitTeethViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupQuestionBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.convertToothIdToDental
import com.straiberry.android.common.extensions.onClick
import com.straiberry.android.common.extensions.subscribe
import com.straiberry.android.core.base.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCheckupQuestion : CheckupQuestion(), IsolatedKoinComponent {
    private val checkupQuestionSubmitTeethViewModel by viewModel<CheckupQuestionSubmitTeethViewModel>()
    //private val retryConnectionViewModel by activityViewModels<RetryConnectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupQuestionBinding.inflate(inflater, container, false).also {
            binding = it

            if (checkupGuideTourViewModel.getGuideTourStatus().questionGuideTour.not()) {
                CheckupQuestionsSpotLights(
                    binding,
                    requireContext(),
                    spotLight,
                    requireActivity(),
                    checkupGuideTourViewModel
                ).setSpotLights()

                isSpotlightShowing = true

                checkupQuestionViewModel.submitStateIsAllThreeAnswer.observe(viewLifecycleOwner, {
                    if (it == 1)
                        spotLight.hide()
                })


                checkupQuestionViewModel.submitStateIsAllThreeAnswer.observe(viewLifecycleOwner, {
                    if (it == 3) {
                        spotLight.visible()
                        spotLight.show(3)
                    }
                })
            }


            checkupQuestionViewModel.resetAnswer()

            teethList = listOf(
                binding.layoutUpperJaw.imageButtonTeethOne,
                binding.layoutUpperJaw.imageButtonTeethTwo,
                binding.layoutUpperJaw.imageButtonTeethThree,
                binding.layoutUpperJaw.imageButtonTeethFour,
                binding.layoutUpperJaw.imageButtonTeethFive,
                binding.layoutUpperJaw.imageButtonTeethSix,
                binding.layoutUpperJaw.imageButtonTeethSeven,
                binding.layoutUpperJaw.imageButtonTeethEight,
                binding.layoutUpperJaw.imageButtonTeethNine,
                binding.layoutUpperJaw.imageButtonTeethTen,
                binding.layoutUpperJaw.imageButtonTeethEleven,
                binding.layoutUpperJaw.imageButtonTeethTwelve,
                binding.layoutUpperJaw.imageButtonTeethThirteen,
                binding.layoutUpperJaw.imageButtonTeethFourteen,
                binding.layoutUpperJaw.imageButtonTeethFifteen,
                binding.layoutUpperJaw.imageButtonTeethSixteen,
                binding.layoutLowerJaw.imageButtonTeethSeventeen,
                binding.layoutLowerJaw.imageButtonTeethNineteen,
                binding.layoutLowerJaw.imageButtonTeethTwenty,
                binding.layoutLowerJaw.imageButtonTeethTwentyOne,
                binding.layoutLowerJaw.imageButtonTeethTwentyTwo,
                binding.layoutLowerJaw.imageButtonTeethTwentyThree,
                binding.layoutLowerJaw.imageButtonTeethTwentyFour,
                binding.layoutLowerJaw.imageButtonTeethTwentyFive,
                binding.layoutLowerJaw.imageButtonTeethTwentySix,
                binding.layoutLowerJaw.imageButtonTeethTwentySeven,
                binding.layoutLowerJaw.imageButtonTeethTwentyEight,
                binding.layoutLowerJaw.imageButtonTeethTwentyNine,
                binding.layoutLowerJaw.imageButtonTeethThirty,
                binding.layoutLowerJaw.imageButtonTeethThirtyOne,
                binding.layoutLowerJaw.imageButtonTeethThirtyTwo,
                binding.layoutLowerJaw.imageButtonTeethThirtyThree
            )
            indicatorList = listOf(
                binding.layoutUpperJaw.indicatorOne.root,
                binding.layoutUpperJaw.indicatorTwo.root,
                binding.layoutUpperJaw.indicatorThree.root,
                binding.layoutUpperJaw.indicatorFour.root,
                binding.layoutUpperJaw.indicatorFive.root,
                binding.layoutUpperJaw.indicatorSix.root,
                binding.layoutUpperJaw.indicatorSeven.root,
                binding.layoutUpperJaw.indicatorEight.root,
                binding.layoutUpperJaw.indicatorNine.root,
                binding.layoutUpperJaw.indicatorTen.root,
                binding.layoutUpperJaw.indicatorEleven.root,
                binding.layoutUpperJaw.indicatorTwelve.root,
                binding.layoutUpperJaw.indicatorThirteen.root,
                binding.layoutUpperJaw.indicatorFourteen.root,
                binding.layoutUpperJaw.indicatorFifteen.root,
                binding.layoutUpperJaw.indicatorSixteen.root,
                binding.layoutLowerJaw.indicatorSeventeen.root,
                binding.layoutLowerJaw.indicatorNineteen.root,
                binding.layoutLowerJaw.indicatorTwenty.root,
                binding.layoutLowerJaw.indicatorTwentyOne.root,
                binding.layoutLowerJaw.indicatorTwentyTwo.root,
                binding.layoutLowerJaw.indicatorTwentyThree.root,
                binding.layoutLowerJaw.indicatorTwentyFour.root,
                binding.layoutLowerJaw.indicatorTwentyFive.root,
                binding.layoutLowerJaw.indicatorTwentySix.root,
                binding.layoutLowerJaw.indicatorTwentySeven.root,
                binding.layoutLowerJaw.indicatorTwentyEight.root,
                binding.layoutLowerJaw.indicatorTwentyNine.root,
                binding.layoutLowerJaw.indicatorThirty.root,
                binding.layoutLowerJaw.indicatorThirtyOne.root,
                binding.layoutLowerJaw.indicatorThirtyTwo.root,
                binding.layoutLowerJaw.indicatorThirtyThree.root
            )

            layoutEditDeleteList = listOf(
                binding.layoutUpperJaw.layoutEditDeleteOne.root,
                binding.layoutUpperJaw.layoutEditDeleteTwo.root,
                binding.layoutUpperJaw.layoutEditDeleteThree.root,
                binding.layoutUpperJaw.layoutEditDeleteFour.root,
                binding.layoutUpperJaw.layoutEditDeleteFive.root,
                binding.layoutUpperJaw.layoutEditDeleteSix.root,
                binding.layoutUpperJaw.layoutEditDeleteSeven.root,
                binding.layoutUpperJaw.layoutEditDeleteEight.root,
                binding.layoutUpperJaw.layoutEditDeleteNine.root,
                binding.layoutUpperJaw.layoutEditDeleteTen.root,
                binding.layoutUpperJaw.layoutEditDeleteEleven.root,
                binding.layoutUpperJaw.layoutEditDeleteTwelve.root,
                binding.layoutUpperJaw.layoutEditDeleteThirteen.root,
                binding.layoutUpperJaw.layoutEditDeleteFourteen.root,
                binding.layoutUpperJaw.layoutEditDeleteFifteen.root,
                binding.layoutUpperJaw.layoutEditDeleteSixteen.root,
                binding.layoutLowerJaw.layoutEditDeleteEighteen.root,
                binding.layoutLowerJaw.layoutEditDeleteNineteen.root,
                binding.layoutLowerJaw.layoutEditDeleteTwenty.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyOne.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyTwo.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyThree.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyFour.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyFive.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentySix.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentySeven.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyEight.root,
                binding.layoutLowerJaw.layoutEditDeleteTwentyNine.root,
                binding.layoutLowerJaw.layoutEditDeleteThirty.root,
                binding.layoutLowerJaw.layoutEditDeleteThirtyOne.root,
                binding.layoutLowerJaw.layoutEditDeleteThirtyTwo.root,
                binding.layoutLowerJaw.layoutEditDeleteThirtyThree.root
            )

            disableSelectedTooth()
            selectTooth()

            binding.imageButtonGo.onClick {
                setupViewPager()
                showQuestions()
                if (isSpotlightShowing)
                    spotLight.show(2)
            }

            binding.textViewCancel.onClick {
                findNavController().popBackStack()
            }

            // Change done layout for dental issue
            binding.apply {
                if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!) {
                    textViewDone.text = getString(R.string.no_further_complaints)
                    buttonDoTheCheckup.text = getString(R.string.done)
                }
            }

            // If all question is answered then hide the questions
            checkupQuestionViewModel.submitStateIsAllThreeAnswer.observe(viewLifecycleOwner,
                { allQuestions ->
                    // If tooth is in edit mode then do not hide the question's
                    if (!isToothInEditMode)
                        if (allQuestions == AllQuestions) {
                            questionIsDone()
                            if (isSpotlightShowing)
                                spotLight.show(3)
                        }
                })

            // Do the checkup
            binding.buttonDoTheCheckup.onClick {
                // Check if user comes from dental issue
                if (chooseCheckupTypeViewModel.submitStateIsComeFromDentalIssue.value!!)
                    findNavController().popBackStack()
                // If false then do the checkup
                else {
                    doTheCheckup()
                    if (isSpotlightShowing) {
                        spotLight.finish()
                        checkupGuideTourViewModel.questionGuideTourIsFinished()
                    }
                }
            }

            dentalIssuesViewModel.submitStateDeleteDentalIssues.observe(viewLifecycleOwner, {
                // Delete dental issue in remote
                if (it is ReadableSuccess) {
                    remoteDeleteDentalIssue(it.data)
                }
            })

            getAllDentalIssues()
        }.root
    }

    /** Handel view state for adding all selected tooth to checkup */
    private fun handleViewStateAddToothToCheckup(loadable: Loadable<AddToothToCheckupSuccessModel>) {
        if (loadable != Loading) showHideLoading(false)
        when (loadable) {
            is Success -> {
                selectedJawForCheckup()
                checkupQuestionViewModel.removeAllAnswers()
                findNavController().navigate(R.id.action_fragmentCheckupQuestion_to_fragmentCamera)
            }
            is Failure -> {
            }
            Loading -> showHideLoading(true)
            NotLoading -> {
            }
        }
    }

    private fun doTheCheckup() {
        val arrayListSelectedTooth = ArrayList<AddToothToCheckupRequest>()
        listOfSelectedTeeth.forEach { selectedToothId ->
            arrayListSelectedTooth.add(
                AddToothToCheckupRequest(
                    toothNumber = selectedToothId.convertToothIdToDental(),
                    duration = checkupQuestionViewModel.getAnswerOne(selectedToothId),
                    cause = checkupQuestionViewModel.getAnswerTwo(selectedToothId),
                    pain = checkupQuestionViewModel.getAnswerThree(selectedToothId),
                )
            )
        }
        checkupQuestionSubmitTeethViewModel.addTeethToCheckup(
            checkupId = chooseCheckupTypeViewModel.submitStateCreateCheckupId.value!!,
            data = arrayListSelectedTooth
        )

        checkupQuestionSubmitTeethViewModel.submitStateAddTooth.subscribe(
            viewLifecycleOwner,
            ::handleViewStateAddToothToCheckup
        )
    }

    companion object {
        const val AllQuestions = 3
    }
}