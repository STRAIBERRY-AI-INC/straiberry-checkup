package com.straiberry.android.checkup.checkup.presentation.view.help

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupHelpViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupInstructionDialogBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.dp
import com.straiberry.android.common.extensions.onClick
import com.straiberry.android.common.helper.AnimateViewPagerIndicator
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentCheckupInstructionDialog : DialogFragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupInstructionDialogBinding
    private val checkupHelpViewModel by viewModel<CheckupHelpViewModel>()
    private val jawDetectionViewModel by activityViewModels<DetectionJawViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupInstructionDialogBinding.inflate(inflater, container, false).also {
            binding = it

            // Check the times that tips are showed
            checkupHelpViewModel.checkupHelpHasBeenSeen()
            jawDetectionViewModel.checkupInstructionHasBeenShowing()

            dialog!!.setCancelable(false)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            binding.viewPager.adapter = AdapterCheckupInstruction(this)

            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    AnimateViewPagerIndicator(
                        LAST_PAGE_IN_VIEW_PAGER,
                        binding.tabLayoutDots,
                        binding.imageButtonClose,
                        CLOSE_BUTTON_SIZE.dp(requireContext())
                    ).animateView(position)
                }
            })

            TabLayoutMediator(binding.tabLayoutDots, binding.viewPager) { _, _ -> }.attach()

            binding.imageButtonClose.onClick {
                dialog!!.dismiss()
            }
        }.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        jawDetectionViewModel.checkupInstructionHasBeenDismiss()
    }

    companion object {
        const val CLOSE_BUTTON_SIZE = 40
        const val LAST_PAGE_IN_VIEW_PAGER = 3
    }
}