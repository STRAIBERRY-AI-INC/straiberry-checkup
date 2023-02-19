package com.straiberry.android.common.helper

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object FirebaseAppEvents {
    private const val ShareCheckupResultFirebaseEventLog = "share_checkup_result"
    private const val CreateDentalIssueFirebaseEventLog = "create_dental_issue"
    private const val BrushingChartMoreSectionFirebaseEventLog = "brushing_chart_more"
    private const val OralHygieneChartMoreSectionFirebaseEventLog = "oral_hygiene_chart_more"
    private const val WhiteningChartMoreSectionFirebaseEventLog = "whitening_chart_more"
    private const val SuggestionClickFirebaseEventLog = "suggestion_clicked"
    private const val AnswersPersonalDataFirebaseEventLog = "answers_personal_data"
    private const val AnswerYesNoQuestionFirebaseLogEvent = "answers_yes_no_questions"
    private const val AnswerWelcomeQuestionFirebaseLogEvent = "answers_welcome_questions"
    private const val DoneEventFirebaseEventName = "done_event"
    private const val SaveEventFirebaseLogKey = "create_event"

    fun onSelectedCheckup(checkupType:String){
        Firebase.analytics.logEvent(checkupType){}
    }

    fun onShareCheckupResult(){
        Firebase.analytics.logEvent(ShareCheckupResultFirebaseEventLog){}
    }

    fun onCreatedDentalIssue(){
        Firebase.analytics.logEvent(CreateDentalIssueFirebaseEventLog){}
    }

    fun onSelectedMoreInBrushingChart(){
        Firebase.analytics.logEvent(BrushingChartMoreSectionFirebaseEventLog){}
    }

    fun onSelectedMoreInOralHygieneChart(){
        Firebase.analytics.logEvent(OralHygieneChartMoreSectionFirebaseEventLog){}
    }

    fun onSelectedMoreInWhiteningChart(){
        Firebase.analytics.logEvent(WhiteningChartMoreSectionFirebaseEventLog){}
    }

    fun onSuggestionClicked(){
        Firebase.analytics.logEvent(SuggestionClickFirebaseEventLog){}
    }

    fun onPersonalDataFilled(){
        Firebase.analytics.logEvent(AnswersPersonalDataFirebaseEventLog){}
    }

    fun onYesNoQuestionAnswered(){
        Firebase.analytics.logEvent(AnswerYesNoQuestionFirebaseLogEvent){}
    }

    fun onWelcomeQuestionAnswered(){
        Firebase.analytics.logEvent(AnswerWelcomeQuestionFirebaseLogEvent){}
    }

    fun onDoneCalendarEvent(){
        Firebase.analytics.logEvent(DoneEventFirebaseEventName){}
    }

    fun onCreateCalendarEvent(){
        Firebase.analytics.logEvent(SaveEventFirebaseLogKey){}
    }
}