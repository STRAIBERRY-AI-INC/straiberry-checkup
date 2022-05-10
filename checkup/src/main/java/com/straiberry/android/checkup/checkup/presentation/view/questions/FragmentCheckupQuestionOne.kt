package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupQuestionOneBinding
import com.straiberry.android.common.extensions.*

class FragmentCheckupQuestionOne : Fragment() {
    private lateinit var binding: FragmentCheckupQuestionOneBinding
    private val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    private var isUserAnswersTheQuestion = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupQuestionOneBinding.inflate(inflater, container, false).also {
            binding = it

            setupAnimation()

            // If question is in edit mode then select answered option
            checkupQuestionViewModel.submitStateSelectedToothIndex.observe(viewLifecycleOwner,
                { selectedOption ->
                    if (checkupQuestionViewModel.submitStateAnswerOne.value?.get(selectedOption) != null)
                        setupSelectedChoice(
                            checkupQuestionViewModel.submitStateAnswerOne.value?.get(
                                selectedOption
                            )!!
                        )
                })
            // Setup options
            listOf(
                binding.cardViewAnswerOneSubAnswerOne,
                binding.cardViewAnswerOneSubAnswerTwo,
                binding.cardViewAnswerOne,
                binding.cardViewAnswerTwo,
                binding.cardViewAnswerThree,
                binding.cardViewAnswerFour,
            ).forEachIndexed { index, materialCardView ->
                // Disable all answers to avoid click while animation is on
                materialCardView.disable()

                materialCardView.onClick {
                    setupSelectedChoice(index)
                }
            }
        }.root
    }

    private fun setupAnimation() {
        binding.cardViewAnswerOne.animate()
            .alpha(1f)
            .setStartDelay(AnimationDelay)
            .setDuration(AnimationDuration)
            .withEndAction {
                binding.cardViewAnswerTwo.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(AnimationDuration)
                    .start()
                binding.cardViewAnswerThree.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(AnimationDuration)
                    .start()
                binding.cardViewAnswerFour.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(AnimationDuration)
                    .withEndAction {
                        // Enable all answer after animation
                        listOf(
                            binding.cardViewAnswerOneSubAnswerOne,
                            binding.cardViewAnswerOneSubAnswerTwo,
                            binding.cardViewAnswerOne,
                            binding.cardViewAnswerTwo,
                            binding.cardViewAnswerThree,
                            binding.cardViewAnswerFour,
                        ).forEach { it.enable() }
                    }
                    .start()
            }.start()
    }

    /**
     * User can only select one option. This will change the
     * selected option to enable.
     * @param selectedChoice to ge the index of selected option
     */
    private fun setupSelectedChoice(selectedChoice: Int) {
        // Save user answer
        checkupQuestionViewModel.saveAnswerOne(
            selectedChoice,
            checkupQuestionViewModel.submitStateSelectedToothIndex.value!!
        )

        // User answered the question
        questionIsAnswered(selectedChoice)

        // Change the selected choice card view to active
        listOf(
            binding.cardViewAnswerOneSubAnswerOne,
            binding.cardViewAnswerOneSubAnswerTwo,
            binding.cardViewAnswerOne,
            binding.cardViewAnswerTwo,
            binding.cardViewAnswerThree,
            binding.cardViewAnswerFour,
        ).forEachIndexed { index, materialCardView ->
            if (index == selectedChoice)
                materialCardView.apply {
                    isSelected = true
                    strokeWidth = EnabledCardViewStrokeWidth.dp
                    elevation = EnabledCardViewElevation
                }
            else
                materialCardView.apply {
                    isSelected = false
                    strokeWidth = DisabledCardViewStrokeWidth.dp
                    elevation = DisabledCardViewElevation
                }
        }

        // Hide/show the sub answer
        if (selectedChoice == SelectedChoiceOne ||
            selectedChoice == SelectedChoiceThree ||
            selectedChoice == SelectedChoiceTwo
        )
            binding.apply {
                cardViewAnswerOne.gone()
                layoutDayQuestion.visible()
            }
        else if (selectedChoice != SelectedChoiceOne || selectedChoice != SelectedChoiceTwo)
            binding.apply {
                cardViewAnswerOne.visible()
                layoutDayQuestion.gone()
            }

        // Change the selected choice text view to active
        listOf(
            binding.textViewAnswerOneSubAnswerOneTitle,
            binding.textViewAnswerOneSubAnswerTwoTitle,
            binding.textViewAnswerOneTitle,
            binding.textViewAnswerTwoTitle,
            binding.textViewAnswerThreeTitle,
            binding.textViewAnswerFourTitle,
        ).forEachIndexed { index, textView ->
            textView.isEnabled = index == selectedChoice
        }

        // Change the selected choice checkbox to active
        listOf(
            binding.imageViewAnswerOneSubAnswerOneCheckbox,
            binding.imageViewAnswerOneSubAnswerTwoCheckbox,
            binding.imageViewAnswerOneCheckbox,
            binding.imageViewAnswerTwoCheckbox,
            binding.imageViewAnswerThreeCheckbox,
            binding.imageViewAnswerFourCheckbox,
        ).forEachIndexed { index, imageView ->
            imageView.isSelected = index == selectedChoice
        }
    }

    private fun questionIsAnswered(selectedChoice: Int) {
        // The selected answer must not be a title answer
        if (selectedChoice != SelectedChoiceThree)
            if (isUserAnswersTheQuestion) {
                checkupQuestionViewModel.userAnswersQuestion()
                isUserAnswersTheQuestion = false
            }
    }

    companion object {
        private const val EnabledCardViewStrokeWidth = 2
        private const val EnabledCardViewElevation = 10f
        private const val DisabledCardViewStrokeWidth = 1
        private const val DisabledCardViewElevation = 0f
        private const val SelectedChoiceOne = 0
        private const val SelectedChoiceTwo = 1
        private const val SelectedChoiceThree = 2
        private const val AnimationDuration = 800L
        private const val AnimationDelay = 300L
    }
}