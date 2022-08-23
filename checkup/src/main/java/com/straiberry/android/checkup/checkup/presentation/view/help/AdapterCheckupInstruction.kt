package com.straiberry.android.checkup.checkup.presentation.view.help

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterCheckupInstruction(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4
    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = FragmentCheckupInstructionPage()
        fragment.arguments = Bundle().apply {
            putInt(PAGE_NUMBER, position)
        }
        return fragment
    }

    companion object {
        const val PAGE_NUMBER = "page_number"
    }
}