package com.straiberry.android.checkup.checkup.presentation.view.history

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.domain.model.CheckupHistorySuccessModel
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.X_RAY_JAW
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupHistoryViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupType
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.checkup.common.extentions.convertToCheckupType
import com.straiberry.android.checkup.databinding.FragmentCheckupHistoryBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.base.*
import com.straiberry.android.common.extensions.*
import com.straiberry.android.common.helper.PaginationScrollListener
import com.straiberry.android.common.model.JawPosition
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class FragmentCheckupHistory : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupHistoryBinding
    private lateinit var layoutManagerCheckupHistory: LinearLayoutManager
    private lateinit var checkupHistoryAdapter: CheckupHistoryAdapter

    //    private val retryConnectionViewModel by activityViewModels<RetryConnectionViewModel>()
    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val checkupHistoryViewModel by viewModel<CheckupHistoryViewModel>()
    val jawDetectionViewModel by activityViewModels<DetectionJawViewModel>()

    // Pagination
    private var isLoading = true
    private var currentPage = 1
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupHistoryBinding.inflate(inflater, container, false).also {
            binding = it
            binding.textViewBack.onClick { findNavController().popBackStack() }
            binding.progressBar.gone()
            getHistoryCheckup()
            // Refresh ui on request failed and retry
//            retryConnectionViewModel.submitState.observe(viewLifecycleOwner,{
//                getHistoryCheckup()
//            })
        }.root
    }

    private fun getHistoryCheckup() {
        setupRecyclerView()
        checkupHistoryViewModel.submitStateCheckupHistory.subscribe(
            viewLifecycleOwner,
            ::handleViewStateCheckupHistory
        )
        checkupHistoryViewModel.checkupHistory(currentPage)
    }

    /** Handel view state for getting all the checkup */
    private fun handleViewStateCheckupHistory(loadable: Loadable<CheckupHistorySuccessModel>?) {
        if (loadable != Loading) binding.apply {
            progressBar.goneWithAnimation()
//            if (currentPage != 1)
            checkupHistoryAdapter.removeLoadingFooter()
            isLoading = false
        }
        when (loadable) {
            is Success -> {
                if (loadable.data.data?.isNullOrEmpty()!!)
                    isLastPage = true
                else
                    setupLoadData(loadable.data.data)
            }
            is Failure -> {
                if (loadable.throwable is HttpException) {
                    // Last page
                    if ((loadable.throwable as HttpException).code() == 404)
                        isLastPage = true
                }
            }
            Loading -> {
                isLoading = true
                checkupHistoryAdapter.addLoadingFooter()
                if (currentPage == 1) binding.progressBar.visibleWithAnimation()
            }
            NotLoading -> {
            }
            null -> {}
        }
    }

    private fun setupRecyclerView() {
        checkupHistoryAdapter = CheckupHistoryAdapter(requireContext()) { clickedCheckup ->
            chooseCheckupViewModel.setCheckupResult(clickedCheckup)
            chooseCheckupViewModel.setSelectedCheckup(
                clickedCheckup.data.checkupType.toInt().convertToCheckupName(requireContext())
            )
            chooseCheckupViewModel.setSelectedCheckupIndex(
                clickedCheckup.data.checkupType.toInt().convertToCheckupType()
            )
            // Setup selected jaw
            clickedCheckup.data.images.forEach {
                when {
                    it.imageType.toInt() == FRONT_JAW -> jawDetectionViewModel.setSelectedJaw(
                        0,
                        JawPosition.FrontTeeth
                    )
                    it.imageType.toInt() == X_RAY_JAW -> jawDetectionViewModel.setSelectedJaw(
                        0,
                        JawPosition.FrontTeeth
                    )
                    it.imageType.toInt() == UPPER_JAW -> jawDetectionViewModel.setSelectedJaw(
                        1,
                        JawPosition.UpperJaw
                    )
                    it.imageType.toInt() == LOWER_JAW -> jawDetectionViewModel.setSelectedJaw(
                        2,
                        JawPosition.LowerJaw
                    )
                }
            }
            // findNavController().popBackStack(R.id.fragmentCheckupHistory,true)
            // Setup navigation
            if (clickedCheckup.data.checkupType.toInt()
                    .convertToCheckupType() == CheckupType.Whitening
            )
                findNavController().navigate(Uri.parse(GoToCheckupResultWhitening))
            else
                findNavController().navigate(Uri.parse(GoToCheckupResultBasic))
        }
        layoutManagerCheckupHistory = LinearLayoutManager(requireContext())
        binding.recyclerViewCheckupHistory.apply {
            adapter = checkupHistoryAdapter
            layoutManager = layoutManagerCheckupHistory
        }
    }


    private fun setupLoadData(dataList: List<CheckupHistorySuccessResponse.Data?>?) {
        if (dataList?.size!! < TotalItemCount) isLastPage = true
        listOfItems.clear()
        listOfItems.addAll(dataList)
        checkupHistoryAdapter.add(listOfItems)
        binding.recyclerViewCheckupHistory.addOnScrollListener(object :
            PaginationScrollListener(layoutManagerCheckupHistory) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                checkupHistoryViewModel.checkupHistory(currentPage)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })
    }


    companion object {
        private const val GoToCheckupResultBasic =
            "android-app://com.straiberry.app.features.checkup.presentation.view.result.FragmentCheckupResultBasic"
        private const val GoToCheckupResultWhitening =
            "android-app://com.straiberry.app.features.checkup.presentation.view.result.FragmentCheckupResultWhitening"
        private const val TotalItemCount = 10
        private val listOfItems: ArrayList<CheckupHistorySuccessResponse.Data?> = arrayListOf()
    }
}