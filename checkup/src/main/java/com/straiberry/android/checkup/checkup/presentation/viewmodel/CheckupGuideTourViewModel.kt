package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.straiberry.android.checkup.checkup.domain.usecase.GetGuideTourStatusUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.SetGuideStatusUseCase
import com.straiberry.android.common.model.GuideTourStatusModel

class CheckupGuideTourViewModel(
    private val getGuideTourStatusUseCase: GetGuideTourStatusUseCase,
    private val setGuideStatusUseCase: SetGuideStatusUseCase
) : ViewModel() {

    fun getGuideTourStatus() = getGuideTourStatusUseCase.execute()

    fun setGuideTourStatus(guideTourStatusModel: GuideTourStatusModel) =
        setGuideStatusUseCase.execute(guideTourStatusModel)

    fun cameraGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.cameraGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun questionGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.questionGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun homeGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.homeGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun checkupGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.checkupGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun calenderGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.calenderGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun eventGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.createEventGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun profileGuideTourIsFinished() {
        val guideTour = getGuideTourStatus()
        guideTour.profileGuideTour = true
        setGuideTourStatus(guideTour)
    }

    fun reshowGuideTour() {
        setGuideTourStatus(GuideTourStatusModel())
    }
}