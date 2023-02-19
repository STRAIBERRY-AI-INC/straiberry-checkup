package com.straiberry.android.checkup.checkup.presentation.view.result

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.straiberry.android.checkup.BuildConfig
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.checkup.checkup.presentation.viewmodel.UserInfoViewModel
import com.straiberry.android.checkup.common.helper.StraiberryCheckupSdkInfo
import com.straiberry.android.checkup.databinding.FragmentCheckupResultWhiteningBinding
import com.straiberry.android.checkup.di.IsolatedKoinComponent
import com.straiberry.android.checkup.di.StraiberrySdk
import com.straiberry.android.common.extensions.onClick
import com.straiberry.android.common.extensions.toStringOrZero
import com.straiberry.android.common.helper.FirebaseAppEvents
import com.straiberry.android.common.helper.ShareScreenshotHelper

class FragmentCheckupResultWhitening : Fragment(), IsolatedKoinComponent {
    private lateinit var binding: FragmentCheckupResultWhiteningBinding
    private var checkupResult = CheckupResultSuccessModel(CheckupHistorySuccessResponse.Data())

    private val chooseCheckupViewModel by activityViewModels<ChooseCheckupTypeViewModel>()
    private val checkupQuestionViewModel by activityViewModels<CheckupQuestionViewModel>()
    private val jawDetectionViewModel by activityViewModels<DetectionJawViewModel>()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StraiberrySdk.start(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCheckupResultWhiteningBinding.inflate(inflater, container, false).also {
            binding = it

            if (chooseCheckupViewModel.submitStateCheckupResult.value == null)
                findNavController().popBackStack()

            checkupResult = chooseCheckupViewModel.submitStateCheckupResult.value!!

            binding.apply {
                // Set front teeth image
                imageViewWhitening.load(
                    checkupResult.data.images.first().image
                )

                // Setting scores
                textViewWhiteningScore.text =
                    checkupResult.data.images.first().result.first().whiteningScore.toStringOrZero()

                // Setup checkup description
                val currentLanguage =
                    if (StraiberryCheckupSdkInfo.getSelectedLanguage() == "" && BuildConfig.IS_FARSI)
                        "fa"
                    else if (StraiberryCheckupSdkInfo.getSelectedLanguage() != "")
                        StraiberryCheckupSdkInfo.getSelectedLanguage()
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        Resources.getSystem().configuration.locales.get(0).language
                    else
                        Resources.getSystem().configuration.locale.language

                textViewTips.text = if (currentLanguage == "fa")
                    checkupResult.data.description.persian
                else
                    checkupResult.data.description.english


                // Setup share the result
                shareCheckupResult.setCheckupResult(
                    userAvatar = BitmapDrawable(
                        resources,
                        userInfoViewModel.userAvatarAsBitmap.value
                    ),
                    userName = userInfoViewModel.userName.value!!,
                    checkupResultTitle = textViewCheckupResultTitle.text.toString(),
                    oralHygieneScore = "",
                    whiteningScore = textViewWhiteningScore.text.toString(),
                    checkupProblems = "",
                    checkupTip = textViewTips.text.toString(),
                    removeOralHygieneScore = true
                )
                imageButtonShare.onClick {
                    // Log event when user shares his/her checkup result
                    FirebaseAppEvents.onShareCheckupResult()
                    ShareScreenshotHelper().shareResult(shareCheckupResult, requireActivity())
                }

                buttonDone.onClick {
                    jawDetectionViewModel.resetNumberOfUploadedJaw()
                    checkupQuestionViewModel.resetSelectedJaw()
                    findNavController().popBackStack()
                }
            }

        }.root
    }
}