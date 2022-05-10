package com.straiberry.android.checkup.checkup.presentation.view.help

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupHelpViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupHelpBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.onClick
import com.straiberry.android.common.helper.AnimateViewPagerIndicator
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentCheckupHelp : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupHelpBinding
    private val checkupHelpViewModel by viewModel<CheckupHelpViewModel>()

    // Permissions
    private val cameraPermissions = listOf(Manifest.permission.CAMERA)
    private val readStoragePermissions = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupHelpBinding.inflate(inflater, container, false).also {
            binding = it

            // If counter is more then 2 time then skip help
            if (checkupHelpViewModel.shouldShowCheckupHelp())
                findNavController().navigate(R.id.action_fragmentCheckupHelp_to_fragmentCamera)

            // Check the times that tips are showed
            checkupHelpViewModel.checkupHelpHasBeenSeen()

            // Setup view pager
            binding.viewPager.apply {
                adapter =
                    AdapterCheckupHelpSlidePager(childFragmentManager, lifecycle)
                binding.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        AnimateViewPagerIndicator(
                            LastPageInViewPager,
                            binding.tabLayoutDots,
                            binding.imageButtonClose
                        ).animateView(position)
                    }
                })

            }

            // Setup tab indicator
            TabLayoutMediator(binding.tabLayoutDots, binding.viewPager) { _, _ -> }.attach()

            binding.textViewBack.onClick { findNavController().popBackStack() }

            // Setup button go
            binding.imageButtonClose.onClick {
                // If permissions wasn't granted the go to permission page
//                if (!hasReadStoragePermissions(requireContext()) ||
//                    !hasCameraPermissions(requireContext())
//                )
//                    findNavController().navigate(R.id.action_fragmentCheckupHelp_to_fragmentGetPermissions)
//                else
                findNavController().navigate(R.id.action_fragmentCheckupHelp_to_fragmentCamera)
            }
        }.root
    }

    /** Convenience method used to check if all permissions required for camera are granted */
    private fun hasCameraPermissions(context: Context) = cameraPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Convenience method used to check if all permissions required for read storage are granted */
    private fun hasReadStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            true
        else
            readStoragePermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
    }

    companion object {
        const val LastPageInViewPager = 4
        const val CounterToShowTips = "CounterToShowTips"
    }
}