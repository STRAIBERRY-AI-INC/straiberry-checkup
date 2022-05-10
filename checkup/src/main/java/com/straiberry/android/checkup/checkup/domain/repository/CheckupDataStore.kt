package com.straiberry.android.checkup.checkup.domain.repository

import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.common.model.GuideTourStatusModel


interface CheckupDataStore {
    suspend fun saveDentalIssue(dentalIssueQuestionsModel: DentalIssueQuestionsModel)
    fun deleteDentalIssue(toothId: Int): Int
    fun observeDentalQuestions(): ArrayList<DentalIssueQuestionsModel>
    fun shouldShowCheckupHelp(): Boolean
    fun checkupHelpHasBeenSeen()
    fun getGuideTourStatus(): GuideTourStatusModel
    fun setGuideTourStatus(guideTourStatusModel: GuideTourStatusModel)
}