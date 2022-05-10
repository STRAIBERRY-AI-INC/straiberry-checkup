package com.straiberry.android.checkup.checkup.presentation.view.result

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemIllustrationViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupProblemIllustrationBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.goneWithAnimation
import com.straiberry.android.common.extensions.visibleWithAnimation
import com.straiberry.android.common.model.JawPosition

class FragmentCheckupProblemIllustration : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupProblemIllustrationBinding
    private val checkupResultProblemIllustrationViewModel by activityViewModels<CheckupResultProblemIllustrationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupProblemIllustrationBinding.inflate(inflater, container, false).also {
            binding = it
            // Scale up the jaws
            binding.layoutUpperJaw.root.apply {
                scaleX = ScaleUpJaws
                scaleY = ScaleUpJaws
            }
            binding.layoutLowerJaw.root.apply {
                scaleX = ScaleUpJaws
                scaleY = ScaleUpJaws
            }
        }.root
    }

    /**
     * Change current position and checkout teeth based on selected page when
     * fragment is recreate.
     */
    override fun onResume() {
        super.onResume()
        // Choose latest problem that user selected for current jaw and checkout the teeth.
        checkupResultProblemIllustrationViewModel.submitStateToothCurrentPosition.observe(
            viewLifecycleOwner,
            { jawPosition ->
                when (jawPosition) {
                    JawPosition.FrontTeethUpper -> checkupResultProblemIllustrationViewModel.submitStateToothWithProblemFrontTeethUpper.observe(
                        viewLifecycleOwner,
                        { listOfTeeth ->
                            checkoutToothWithProblems(listOfTeeth)
                        })
                    JawPosition.FrontTeethLower -> checkupResultProblemIllustrationViewModel.submitStateToothWithProblemFrontTeethLower.observe(
                        viewLifecycleOwner,
                        { listOfTeeth ->
                            checkoutToothWithProblems(listOfTeeth)
                        })
                    JawPosition.LowerJaw -> checkupResultProblemIllustrationViewModel.submitStateToothWithProblemLowerJaw.observe(
                        viewLifecycleOwner,
                        { listOfTeeth ->
                            checkoutToothWithProblems(listOfTeeth)
                        })
                    JawPosition.UpperJaw -> checkupResultProblemIllustrationViewModel.submitStateToothWithProblemUpperJaw.observe(
                        viewLifecycleOwner,
                        { listOfTeeth ->
                            checkoutToothWithProblems(listOfTeeth)
                        })

                }
                // Set current position and show correct jaw
                lastSelectedJaw = jawPosition!!
            })

        // When user switch's between front_lower and front_upper then show
        // correct jaw again
        checkupResultProblemIllustrationViewModel.submitStateSwitchBetweenFrontJaws.observe(
            viewLifecycleOwner,
            { frontJawPosition ->
                showCorrectJaw(frontJawPosition)
            })

    }

    /**
     * Show correct jaw based on view pager position:
     * Page 1 -> Includes two type of front: upper and lower
     * Page 2 -> Is upper jaw
     * Page 3 -> Is lower jaw
     */
    private fun showCorrectJaw(jawPosition: JawPosition?) {
        if (jawPosition == JawPosition.LowerJaw
            || jawPosition == JawPosition.FrontTeethLower
        ) {
            binding.layoutLowerJaw.root.visibleWithAnimation()
            binding.layoutUpperJaw.root.goneWithAnimation()
        } else {
            binding.layoutLowerJaw.root.goneWithAnimation()
            binding.layoutUpperJaw.root.visibleWithAnimation()
        }
    }

    private fun checkoutToothWithProblems(teethWithProblem: ArrayList<Int>) {
        val listOfIndicator = listOf(
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

        showCorrectJaw(lastSelectedJaw)

        // Restart all indicator and change the stroke color
        listOfIndicator.forEach { indicator ->
            indicator.apply {
                clearAnimation()
                strokeColor = ContextCompat.getColor(requireContext(), R.color.secondaryLight)
            }
        }

        // Apply animation
        teethWithProblem.forEach { index ->
            listOfIndicator[index].apply {
                visibleWithAnimation()
                ObjectAnimator.ofPropertyValuesHolder(
                    this,
                    PropertyValuesHolder.ofFloat("scaleX", AnimationScaleUp),
                    PropertyValuesHolder.ofFloat("scaleY", AnimationScaleUp)
                ).apply {
                    duration = AnimationDuration
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = AnimationRepeatCount
                    start()
                }.doOnEnd {
                    this.apply {
                        this.animate().scaleX(AnimationScaleDown).scaleY(AnimationScaleDown).start()
                    }
                }
            }
        }
    }

    companion object {
        private var lastSelectedJaw: JawPosition? = null
        private const val AnimationDuration = 800L
        private const val AnimationRepeatCount = 4
        private const val AnimationScaleUp = 1.08f
        private const val AnimationScaleDown = 0.8f
        private const val ScaleUpJaws = 1.2f
    }
}