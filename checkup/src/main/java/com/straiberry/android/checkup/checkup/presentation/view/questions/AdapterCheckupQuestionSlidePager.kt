package com.straiberry.android.checkup.checkup.presentation.view.questions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterCheckupQuestionSlidePager(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 3

    private val listOfFragments = listOf(
        FragmentCheckupQuestionOne(),
        FragmentCheckupQuestionTwo(),
        FragmentCheckupQuestionThree()
    )

    override fun createFragment(position: Int): Fragment {
        return listOfFragments[position]
    }
}