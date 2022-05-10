package com.straiberry.android.checkup.checkup.presentation.view.result

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.straiberry.android.common.model.JawPosition

class AdapterCheckupProblemRealImageSlidePager(
    private val selectedJaw: HashMap<Int, JawPosition>,
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = selectedJaw.toList().size

    private val listOfFragments = arrayListOf<Fragment>()

    override fun createFragment(position: Int): Fragment {
        if (selectedJaw[0] != null)
            listOfFragments.add(FragmentCheckupResultProblemRealImage())
        if (selectedJaw[1] != null)
            listOfFragments.add(FragmentCheckupResultProblemRealImage())
        if (selectedJaw[2] != null)
            listOfFragments.add(FragmentCheckupResultProblemRealImage())

        return listOfFragments[position]
    }
}