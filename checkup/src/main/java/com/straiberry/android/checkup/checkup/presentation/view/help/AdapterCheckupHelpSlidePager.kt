package com.straiberry.android.checkup.checkup.presentation.view.help

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterCheckupHelpSlidePager(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 5

    private val listOfFragments = listOf(
        FragmentCheckupHelpFirstTip(),
        FragmentCheckupHelpSecondTip(),
        FragmentCheckupHelpThirdTip(),
        FragmentCheckupHelpFourthTip(),
        FragmentCheckupHelpFiveTip()
    )

    override fun createFragment(position: Int): Fragment {
        return listOfFragments[position]
    }
}