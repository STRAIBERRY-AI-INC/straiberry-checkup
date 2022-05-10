package com.straiberry.android.checkup.checkup.presentation.view.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.straiberry.android.checkup.databinding.FragmentCheckupHelpFiveTipBinding

class FragmentCheckupHelpFiveTip : Fragment() {
    private lateinit var binding: FragmentCheckupHelpFiveTipBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentCheckupHelpFiveTipBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }
}