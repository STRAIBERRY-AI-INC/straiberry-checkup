package com.straiberry.android.common.features.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.straiberry.android.common.databinding.BottomSheetOnlineSupportBinding
import com.straiberry.android.common.extensions.launchUrl
import com.straiberry.android.common.extensions.onClick

class BottomSheetOnlineSupport : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetOnlineSupportBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return BottomSheetOnlineSupportBinding.inflate(inflater, container, false).also {
            binding = it
            binding.buttonYes.onClick {
                RAY_CHAT_URL.launchUrl(requireActivity())
            }
            binding.buttonNo.onClick { dismiss() }
        }.root
    }

    companion object {
        private const val RAY_CHAT_URL = "https://widget.raychat.io/626cf5c291829a8307f80541"
    }
}