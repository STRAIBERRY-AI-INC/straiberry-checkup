package com.straiberry.android.checkup.checkup.data.repository

import android.content.Context
import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore
import com.straiberry.android.common.helper.Prefs
import com.straiberry.android.common.model.GuideTourStatusModel
import io.paperdb.Paper

class FlowCheckupDataStore(val context: Context) : CheckupDataStore {

    override fun getGuideTourStatus(): GuideTourStatusModel =
        if (Paper.book().contains(GUIDE_TOUR))
            Paper.book().read(GUIDE_TOUR)
        else
            GuideTourStatusModel()

    override fun setGuideTourStatus(guideTourStatusModel: GuideTourStatusModel) {
        Paper.book().write(GUIDE_TOUR, guideTourStatusModel)
    }

    override suspend fun saveDentalIssue(dentalIssueQuestionsModel: DentalIssueQuestionsModel) {
        var answers = ArrayList<DentalIssueQuestionsModel>()
        var remoteToothId = -1
        if (Paper.book().contains(DentalIssueQuestions)) {
            answers = Paper.book().read(DentalIssueQuestions)
            // Update the answer if it exist
            var isUpdated = false
            answers.forEachIndexed { index, dentalIssueQuestion ->
                if (dentalIssueQuestion.teethId == dentalIssueQuestionsModel.teethId) {
                    answers[index] = dentalIssueQuestionsModel
                    remoteToothId = dentalIssueQuestion.remoteTeethId
                    isUpdated = true
                }
            }
            if (!isUpdated)
                answers.add(dentalIssueQuestionsModel)
        } else
            answers.add(dentalIssueQuestionsModel)

        Paper.book().write(DentalIssueQuestions, answers)
    }

    override fun deleteDentalIssue(toothId: Int): Int {
        val answers: ArrayList<DentalIssueQuestionsModel> = Paper.book().read(DentalIssueQuestions)
        val remoteToothId: Int = answers.first { it.teethId == toothId }.remoteTeethId
        answers.removeAll { it.teethId == toothId }
        Paper.book().write(DentalIssueQuestions, answers)
        return remoteToothId
    }

    override fun observeDentalQuestions(): ArrayList<DentalIssueQuestionsModel> =
        Paper.book().read(DentalIssueQuestions) as ArrayList<DentalIssueQuestionsModel>

    override fun shouldShowCheckupHelp(): Boolean {
        return Prefs.getInteger(context, TIPS_COUNTER) <= 2
    }

    override fun checkupHelpHasBeenSeen() {
        val counterForShowTips = Prefs.getInteger(context, TIPS_COUNTER)
        Prefs.putInteger(context, TIPS_COUNTER, counterForShowTips + 1)

    }

    companion object {
        const val GUIDE_TOUR = "GuideTour"
        const val DentalIssueQuestions = "DentalIssueQuestions"
        const val TIPS_COUNTER = "CounterToShowTips"
    }
}