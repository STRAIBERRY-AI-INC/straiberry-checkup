package com.straiberry.android.checkup.checkup.presentation.view.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.straiberry.android.checkup.databinding.FragmentCheckupHelpSecondTipBinding

class FragmentCheckupHelpSecondTip : Fragment() {
    private lateinit var binding: FragmentCheckupHelpSecondTipBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupHelpSecondTipBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onResume() {
        super.onResume()
        if (binding.lottieAnimation.animation != null)
            binding.lottieAnimation.animation.reset()
    }
}