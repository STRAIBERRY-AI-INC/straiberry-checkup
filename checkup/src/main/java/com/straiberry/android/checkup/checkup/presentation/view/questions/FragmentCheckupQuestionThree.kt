package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupQuestionThreeBinding
import com.straiberry.android.common.extensions.dp
import com.straiberry.android.common.extensions.onClick

class FragmentCheckupQuestionThree : Fragment() {
    private lateinit var binding: FragmentCheckupQuestionThreeBinding
    private val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    private var isUserAnswersTheQuestion = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupQuestionThreeBinding.inflate(inflater, container, false).also {
            binding = it
            // If question is in edit mode then select answered option
            checkupQuestionViewModel.submitStateSelectedToothIndex.observe(viewLifecycleOwner,
                { selectedOption ->
                    if (checkupQuestionViewModel.submitStateAnswerThree.value?.get(selectedOption) != null)
                        setupSelectedChoice(
                            checkupQuestionViewModel.submitStateAnswerThree.value?.get(
                                selectedOption
                            )!!
                        )
                })
            // Setup options
            listOf(
                binding.cardViewAnswerOne,
                binding.cardViewAnswerTwo,
                binding.cardViewAnswerThree,
                binding.cardViewAnswerFour,
                binding.cardViewAnswerFive
            ).forEachIndexed { index, materialCardView ->
                materialCardView.onClick {
                    setupSelectedChoice(index)
                }
            }
        }.root
    }

    /**
     * User can only select one option. This will change the
     * selected option to enable.
     * @param selectedChoice to ge the index of selected option
     */
    private fun setupSelectedChoice(selectedChoice: Int) {
        // Save the user answer
        checkupQuestionViewModel.saveAnswerThree(
            selectedChoice,
            checkupQuestionViewModel.submitStateSelectedToothIndex.value!!
        )

        // User answered the question
        questionIsAnswered()

        // Change the selected choice card view to active
        listOf(
            binding.cardViewAnswerOne,
            binding.cardViewAnswerTwo,
            binding.cardViewAnswerThree,
            binding.cardViewAnswerFour,
            binding.cardViewAnswerFive
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

        // Change the selected choice text view to active
        listOf(
            binding.textViewAnswerOneTitle,
            binding.textViewAnswerTwoTitle,
            binding.textViewAnswerThreeTitle,
            binding.textViewAnswerFourTitle,
            binding.textViewAnswerFiveTitle
        ).forEachIndexed { index, textView ->
            textView.isEnabled = index == selectedChoice
        }

        // Change the selected choice checkbox to active
        listOf(
            binding.imageViewAnswerOneCheckbox,
            binding.imageViewAnswerTwoCheckbox,
            binding.imageViewAnswerThreeCheckbox,
            binding.textViewAnswerFourTitle,
            binding.textViewAnswerFiveTitle
        ).forEachIndexed { index, imageView ->
            imageView.isSelected = index == selectedChoice
        }
    }

    private fun questionIsAnswered() {
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
    }
}
