package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.widget.ImageButton
import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel

interface CheckupQuestionContract {

    fun animateImageButtonNext(isShow: Boolean)
    fun moveQuestionTitleBox(PagePosition: Int)
    fun movePrimaryIndicator()
    fun moveIndicatorAlongsideQuestionTitleBox()
    fun animateIndicatorAlongsideQuestionTitleBox()
    fun animateToothScaleUp(imageButton: ImageButton)
    fun animateToothScaleDown(imageButton: ImageButton)
    fun setupButtonCancel(buttonCancelFunctional: ButtonCancelFunctional)
    fun activeIndicator(index: Int)
    fun showEditAndDeleteLayout()
    fun hideAllEditDeleteLayout()
    fun showHideDone(isDone: Boolean)
    fun selectedJawForCheckup()

    // Question box
    fun hideQuestionBox()
    fun showQuestionBox()

    // Tooth
    fun inactiveTooth(teethList: List<ImageButton>)
    fun enableAllTooth(teethList: List<ImageButton>)
    fun disableAllTooth(teethList: List<ImageButton>)
    fun showAllSelectedTooth()
    fun hideAllSelectedTooth()
    fun editTooth(element: Int)
    fun deleteTooth(element: Int)

    // Active/dative jaws
    fun inactiveUpperJaw()
    fun inactiveLowerJaw()
    fun activeUpperJaw()
    fun activeLowerJaw()

    // Dental issue local
    fun saveDentalIssue(toothId: Int)
    fun deleteDentalIssue(toothId: Int)
    fun getAllDentalIssues()

    // Dental issue remote
    fun remoteSaveDentalIssue()
    fun remoteDeleteDentalIssue(toothId: Int)
    fun remoteUpdateDentalIssue(dentalIssue: DentalIssueQuestionsModel)
}