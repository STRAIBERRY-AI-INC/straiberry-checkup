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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import com.google.android.material.card.MaterialCardView
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.X_RAY_JAW
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemIllustrationViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemRealImageViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupProblemRealImageBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.toImageXPosition
import com.straiberry.android.common.extensions.toImageYPosition
import com.straiberry.android.common.extensions.visibleWithAnimation
import com.straiberry.android.common.model.JawPosition

class FragmentCheckupResultProblemRealImage : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupProblemRealImageBinding
    private val checkupResultProblemRealImageViewModel by activityViewModels<CheckupResultProblemRealImageViewModel>()
    private val checkupResultProblemIllustrationViewModel by activityViewModels<CheckupResultProblemIllustrationViewModel>()
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupProblemRealImageBinding.inflate(inflater, container, false).also {
            binding = it

            // Progressbar to load image
            val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

            checkupResultProblemIllustrationViewModel.submitStateToothCurrentPosition.observe(
                viewLifecycleOwner,
                { jawPosition ->
                    // Remove all indicators
                    binding.layoutRealImage.removeAllViews()
                    when (jawPosition) {
                        JawPosition.FrontTeeth -> {
                            binding.imageViewRealImage.load(
                                chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType.toInt() == FRONT_JAW || it.imageType.toInt() == X_RAY_JAW }?.image
                            ) {
                                placeholder(circularProgressDrawable)
                            }
                            checkupResultProblemRealImageViewModel.submitStateToothWithProblemFrontTeeth.observe(
                                viewLifecycleOwner,
                                {
                                    checkoutToothWithProblems(it)
                                })
                        }
                        JawPosition.LowerJaw -> {
                            binding.imageViewRealImage.load(
                                chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType.toInt() == LOWER_JAW }?.image
                            ) {
                                placeholder(circularProgressDrawable)
                            }
                            checkupResultProblemRealImageViewModel.submitStateToothWithProblemLowerJaw.observe(
                                viewLifecycleOwner,
                                {
                                    checkoutToothWithProblems(it)
                                })
                        }
                        JawPosition.UpperJaw -> {
                            binding.imageViewRealImage.load(
                                chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType.toInt() == UPPER_JAW }?.image
                            ) {
                                placeholder(circularProgressDrawable)
                            }
                            checkupResultProblemRealImageViewModel.submitStateToothWithProblemUpperJaw.observe(
                                viewLifecycleOwner,
                                {
                                    checkoutToothWithProblems(it)
                                })
                        }
                        else -> {
                        }
                    }
                })
        }.root
    }

    private fun checkoutToothWithProblems(teethWithProblem: ArrayList<Pair<Double, Double>>) {
        // Remove all indicators
        binding.layoutRealImage.removeAllViews()
        teethWithProblem.forEach {
            binding.layoutRealImage.addView(
                layoutInflater.inflate(
                    R.layout.teeth_indicator,
                    binding.root,
                    false
                ).apply {
                    x = it.first.toImageXPosition(binding.imageViewRealImage.width)
                        .toFloat() - resources.getDimension(R.dimen.problem_indicator_size)
                    y = it.second.toImageYPosition(binding.imageViewRealImage.height)
                        .toFloat() - resources.getDimension(R.dimen.problem_indicator_size)
                    visibleWithAnimation()
                    this.findViewById<MaterialCardView>(R.id.indicatorLower).apply {
                        strokeColor = ContextCompat.getColor(context, R.color.secondaryLight)
                        ObjectAnimator.ofPropertyValuesHolder(
                            this,
                            PropertyValuesHolder.ofFloat(
                                "scaleX",
                                AnimationScaleUp
                            ),
                            PropertyValuesHolder.ofFloat(
                                "scaleY",
                                AnimationScaleUp
                            )
                        ).apply {
                            duration = AnimationDuration
                            repeatCount = ObjectAnimator.INFINITE
                            repeatMode = ObjectAnimator.REVERSE
                            repeatCount = AnimationRepeatCount
                            start()
                        }.doOnEnd {
                            this.apply {
                                this.animate().scaleX(AnimationScaleDown).scaleY(
                                    AnimationScaleDown
                                ).start()
                            }
                        }
                    }
                })
        }
    }

    companion object {
        private const val AnimationDuration = 800L
        private const val AnimationRepeatCount = 4
        private const val AnimationScaleUp = 1.1f
        private const val AnimationScaleDown = 0.9f
    }
}