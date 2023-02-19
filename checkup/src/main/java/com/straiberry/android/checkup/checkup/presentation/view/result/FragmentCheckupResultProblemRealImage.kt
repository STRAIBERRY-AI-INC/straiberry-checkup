package com.straiberry.android.checkup.checkup.presentation.view.result

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import com.google.android.material.card.MaterialCardView
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupImageType
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemIllustrationViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemRealImageViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.XrayViewModel
import com.straiberry.android.checkup.common.extentions.createPaletteSync
import com.straiberry.android.checkup.databinding.FragmentCheckupProblemRealImageBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.gone
import com.straiberry.android.common.extensions.toImageXPosition
import com.straiberry.android.common.extensions.toImageYPosition
import com.straiberry.android.common.extensions.visibleWithAnimation
import com.straiberry.android.common.model.JawPosition
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCheckupResultProblemRealImage : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupProblemRealImageBinding
    private val xrayViewModel by viewModel<XrayViewModel>()
    private val checkupResultProblemRealImageViewModel by activityViewModels<CheckupResultProblemRealImageViewModel>()
    private val checkupResultProblemIllustrationViewModel by activityViewModels<CheckupResultProblemIllustrationViewModel>()
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private lateinit var selectedCheckup: CheckupType

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

            selectedCheckup = chooseCheckupViewModel.submitStateSelectedCheckupIndex.value!!

            checkupResultProblemIllustrationViewModel.submitStateToothCurrentPosition.observe(
                viewLifecycleOwner
            ) { jawPosition ->
                binding.layoutRealImage.removeAllViews()
                binding.layoutRealImage.addView(binding.imageViewRealImage)
                when (jawPosition) {
                    JawPosition.FrontTeeth, JawPosition.FrontTeethLower, JawPosition.FrontTeethUpper -> {
                        val jawImageUrl =
                            chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType == CheckupImageType.FrontJaw || it.imageType == CheckupImageType.XrayJaw }?.image

                        // If checkup is x-ray then get image as bitmap and create a square background
                        if (selectedCheckup == CheckupType.XRays) {
                            binding.imageViewRealImage.gone()
                            xrayViewModel.getUrlImageBitmap(requireContext(), jawImageUrl!!)
                            xrayViewModel.submitStateUrlImageBitmap.observe(viewLifecycleOwner) { jawBitmap ->
                                binding.imageViewRealImageXray.load(jawBitmap)
                                Palette.Builder(jawBitmap!!).generate {
                                    it?.let { palette ->
                                        val dominantColor = palette.getDominantColor(Color.GRAY)

                                        binding.cardViewBackground.setCardBackgroundColor(
                                            dominantColor
                                        )

                                    }
                                }
                            }
                        }

                        binding.imageViewRealImage.load(jawImageUrl) {
                            placeholder(circularProgressDrawable)
                        }

                        checkupResultProblemRealImageViewModel.submitStateToothWithProblemFrontTeeth.observe(
                            viewLifecycleOwner
                        ) {
                            checkoutToothWithProblems(it)
                        }
                    }
                    JawPosition.LowerJaw -> {
                        binding.imageViewRealImage.load(
                            chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType == CheckupImageType.LowerJaw }?.image
                        ) {
                            placeholder(circularProgressDrawable)
                        }
                        checkupResultProblemRealImageViewModel.submitStateToothWithProblemLowerJaw.observe(
                            viewLifecycleOwner
                        ) {
                            checkoutToothWithProblems(it)
                        }
                    }
                    JawPosition.UpperJaw -> {
                        binding.imageViewRealImage.load(
                            chooseCheckupViewModel.submitStateCheckupResult.value?.data?.images?.first { it.imageType == CheckupImageType.UpperJaw }?.image
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
            }
        }.root
    }

    private fun checkoutToothWithProblems(teethWithProblem: ArrayList<Pair<Double, Double>>) {
        // Remove all indicators
        val showedImageView = if (selectedCheckup == CheckupType.XRays)
            binding.imageViewRealImageXray
        else
            binding.imageViewRealImage

        binding.layoutRealImage.removeAllViews()
        binding.layoutRealImage.addView(showedImageView)
        teethWithProblem.forEach {
            binding.layoutRealImage.addView(
                layoutInflater.inflate(
                    R.layout.teeth_indicator,
                    binding.root,
                    false
                ).apply {
                    showedImageView.viewTreeObserver.addOnGlobalLayoutListener {
                        x = it.first.toImageXPosition(showedImageView.width)
                            .toFloat() - resources.getDimension(com.straiberry.android.common.R.dimen.problem_indicator_size)
                        y = it.second.toImageYPosition(showedImageView.height)
                            .toFloat() - resources.getDimension(com.straiberry.android.common.R.dimen.problem_indicator_size)
                        visibleWithAnimation()
                        this.findViewById<MaterialCardView>(R.id.indicatorLower).apply {
                            strokeColor = ContextCompat.getColor(
                                context,
                                com.straiberry.android.common.R.color.secondaryLight
                            )
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