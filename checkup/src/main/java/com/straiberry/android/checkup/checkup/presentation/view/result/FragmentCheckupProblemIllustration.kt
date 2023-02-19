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
import com.google.android.material.card.MaterialCardView
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemIllustrationViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupProblemIllustrationBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.goneWithAnimation
import com.straiberry.android.common.extensions.hideWithAnimation
import com.straiberry.android.common.extensions.visibleWithAnimation
import com.straiberry.android.common.model.JawPosition

class FragmentCheckupProblemIllustration : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupProblemIllustrationBinding
    private lateinit var listOfIndicator: List<MaterialCardView>
    private var objectAnimator: ObjectAnimator? = null

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

            listOfIndicator = listOf(
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
            ).onEach { indicator ->
                indicator.strokeColor =
                    ContextCompat.getColor(requireContext(), com.straiberry.android.common.R.color.secondaryLight)
            }

            // Scale up the jaws
            binding.layoutUpperJaw.root.apply {
                scaleX = SCALE_UP_JAWS
                scaleY = SCALE_UP_JAWS
            }
            binding.layoutLowerJaw.root.apply {
                scaleX = SCALE_UP_JAWS
                scaleY = SCALE_UP_JAWS
            }

            checkupResultProblemIllustrationViewModel.submitStateToothCurrentPosition.observe(
                viewLifecycleOwner
            ) { jawPosition ->
                // Hide all indicator when user changes jaw
                listOfIndicator.onEach { it.hideWithAnimation() }

                /**
                 * Show correct jaw based on view pager position:
                 * Page 1 -> Includes two type of front: upper and lower
                 * Page 2 -> Is upper jaw
                 * Page 3 -> Is lower jaw
                 */
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

            checkupResultProblemIllustrationViewModel.submitStateToothWithProblemFrontTeethUpper.observe(
                viewLifecycleOwner,
                { listOfTeeth ->
                    checkoutToothWithProblems(listOfTeeth)
                    binding.layoutLowerJaw.root.goneWithAnimation()
                    binding.layoutUpperJaw.root.visibleWithAnimation()
                })
            checkupResultProblemIllustrationViewModel.submitStateToothWithProblemLowerJaw.observe(
                viewLifecycleOwner,
                { listOfTeeth ->
                    checkoutToothWithProblems(listOfTeeth)
                    binding.layoutLowerJaw.root.visibleWithAnimation()
                    binding.layoutUpperJaw.root.goneWithAnimation()
                })
            checkupResultProblemIllustrationViewModel.submitStateToothWithProblemFrontTeethLower.observe(
                viewLifecycleOwner,
                { listOfTeeth ->
                    checkoutToothWithProblems(listOfTeeth)
                    binding.layoutLowerJaw.root.visibleWithAnimation()
                    binding.layoutUpperJaw.root.goneWithAnimation()
                })
            checkupResultProblemIllustrationViewModel.submitStateToothWithProblemUpperJaw.observe(
                viewLifecycleOwner,
                { listOfTeeth ->
                    checkoutToothWithProblems(listOfTeeth)
                    binding.layoutLowerJaw.root.goneWithAnimation()
                    binding.layoutUpperJaw.root.visibleWithAnimation()
                })
        }.root
    }

    private fun checkoutToothWithProblems(teethWithProblem: ArrayList<Int>) {
        if (teethWithProblem.isNotEmpty()) {

            objectAnimator?.end()

            // Apply animation to clicked problems
            teethWithProblem.forEach { index ->
                listOfIndicator[index].apply {
                    visibleWithAnimation()
                    objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                        this,
                        PropertyValuesHolder.ofFloat("scaleX", ANIMATION_SCALE_UP),
                        PropertyValuesHolder.ofFloat("scaleY", ANIMATION_SCALE_UP)
                    ).apply {
                        duration = ANIMATION_DURATION
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        repeatCount = ANIMATION_REPEAT_COUNT
                        setAutoCancel(false)
                        start()
                        doOnEnd {
                            listOfIndicator[index].animate().scaleX(ORIGINAL_SCALE)
                                .scaleY(ORIGINAL_SCALE).start()
                        }
                    }
                }
            }
        }

    }

    companion object {
        private const val ORIGINAL_SCALE = 1f
        private const val ANIMATION_DURATION = 500L
        private const val ANIMATION_REPEAT_COUNT = 4
        private const val ANIMATION_SCALE_UP = 1.10f
        private const val SCALE_UP_JAWS = 1.4f
    }
}