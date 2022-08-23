package com.straiberry.android.checkup.checkup.presentation.view.help

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.view.help.AdapterCheckupInstruction.Companion.PAGE_NUMBER
import com.straiberry.android.checkup.databinding.FragmentCheckupInstructionPagesBinding
import com.straiberry.android.common.extensions.hide
import com.straiberry.android.common.extensions.visible

class FragmentCheckupInstructionPage : Fragment() {
    private lateinit var binding: FragmentCheckupInstructionPagesBinding
    private var pageNumber = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupInstructionPagesBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments.takeIf { it!!.containsKey(PAGE_NUMBER) }?.apply {
            pageNumber = getInt(PAGE_NUMBER)
            val listOfVideoDescription = listOf(
                getString(R.string.checkup_instruction_description_one),
                getString(R.string.checkup_instruction_desciption_two),
                getString(R.string.checkup_instruction_description_three),
                getString(R.string.checkup_instruction_description_four)
            )
            val listOfVideoPath = listOf(
                "android.resource://" + requireActivity().packageName + "/" + R.raw.checkup_instruction_one,
                "android.resource://" + requireActivity().packageName + "/" + R.raw.checkup_instruction_two,
                "android.resource://" + requireActivity().packageName + "/" + R.raw.checkup_instruction_three,
                "android.resource://" + requireActivity().packageName + "/" + R.raw.checkup_instruction_four,
            )
            binding.textViewDescription.text = listOfVideoDescription[pageNumber]
            binding.cardViewVideo.hide()
            binding.videoView.apply {
                setVideoURI(Uri.parse(listOfVideoPath[pageNumber]))
                setOnPreparedListener {
                    it.isLooping = true
                    it.start()
                }
                setOnInfoListener { mediaPlayer, i, _ ->
                    mediaPlayer.setVolume(0F, 0F)
                    if (i == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        binding.cardViewVideo.visible()
                        return@setOnInfoListener true
                    }
                    return@setOnInfoListener false
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.videoView.start()
    }
}