package com.straiberry.android.checkup.common.custom.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import coil.transform.CircleCropTransformation
import com.straiberry.android.checkup.BuildConfig
import com.straiberry.android.checkup.R
import com.straiberry.android.common.extensions.gone
import com.straiberry.android.common.extensions.visible

/**
 * This class will share a screen shot of a layout that
 * includes some information of current checkup. User can share
 * this screen shot.
 *
 * See (res/layouts/layout_share_checkup_result.xml)
 */
class ShareCheckupResultView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attributes, defStyleAttr) {
    private var imageViewUserAvatar: ImageView
    private var textViewNameShare: TextView
    private var textViewCheckupResultTitleShare: TextView
    private var textViewOralHygieneScoreShare: TextView
    private var textViewOralHygieneScoreShareCenter: TextView
    private var textViewWhiteningScoreShare: TextView
    private var textViewWhiteningScoreShareCenter: TextView
    private var textViewCheckupCountProblemShare: TextView
    private var textViewTipsShare: TextView
    private var layoutWhiteningScoreShare: ConstraintLayout
    private var layoutWhiteningScoreShareCenter: ConstraintLayout
    private var frameLayoutLine72Share: FrameLayout
    private var layoutOralHygieneScoreShare: ConstraintLayout
    private var layoutOralHygieneScoreShareCenter: ConstraintLayout
    private var layoutAppInfoGlobal: ConstraintLayout
    private var layoutAppInfoFarsi: ConstraintLayout

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_share_checkup_result, this)

        imageViewUserAvatar = findViewById(R.id.imageViewUserAvatar)
        textViewNameShare = findViewById(R.id.textViewNameShare)
        textViewCheckupResultTitleShare = findViewById(R.id.textViewCheckupResultTitleShare)
        textViewOralHygieneScoreShare = findViewById(R.id.textViewOralHygieneScoreShare)
        textViewOralHygieneScoreShareCenter = findViewById(R.id.textViewOralHygieneScoreShareCenter)
        textViewWhiteningScoreShare = findViewById(R.id.textViewWhiteningScoreShare)
        textViewWhiteningScoreShareCenter = findViewById(R.id.textViewWhiteningScoreShareCenter)
        textViewCheckupCountProblemShare = findViewById(R.id.textViewCheckupCountProblemShare)
        textViewTipsShare = findViewById(R.id.textViewTipsShare)
        layoutWhiteningScoreShareCenter = findViewById(R.id.layoutWhiteningScoreShareCenter)
        layoutWhiteningScoreShare = findViewById(R.id.layoutWhiteningScoreShare)
        frameLayoutLine72Share = findViewById(R.id.frameLayoutLine72Share)
        layoutOralHygieneScoreShareCenter = findViewById(R.id.layoutOralHygieneScoreShareCenter)
        layoutOralHygieneScoreShare = findViewById(R.id.layoutOralHygieneScoreShare)
        layoutAppInfoFarsi = findViewById(R.id.layoutAppInfoFarsi)
        layoutAppInfoGlobal = findViewById(R.id.layoutAppInfoGlobal)
    }

    fun setCheckupResult(
        userAvatar: Drawable?,
        userName: String,
        checkupResultTitle: String,
        oralHygieneScore: String,
        whiteningScore: String,
        checkupProblems: String,
        checkupTip: String,
        removeWhiteningScore: Boolean = false,
        removeOralHygieneScore: Boolean = false
    ) {

        imageViewUserAvatar.load(userAvatar) {
            error(com.straiberry.android.common.R.drawable.ic_empty_avatar)
            transformations(CircleCropTransformation())
        }

        textViewNameShare.text =
            context.getString(R.string.share_checkup_result_user_name, userName)

        textViewCheckupResultTitleShare.text = checkupResultTitle

        textViewOralHygieneScoreShare.text = oralHygieneScore

        textViewOralHygieneScoreShareCenter.text = oralHygieneScore

        textViewCheckupCountProblemShare.text = checkupProblems

        textViewWhiteningScoreShareCenter.text = whiteningScore

        textViewWhiteningScoreShare.text = whiteningScore

        textViewTipsShare.text = checkupTip

        // If checkup type is not "regular checkup" and "other" then remove whitening score layout
        if (removeWhiteningScore) {
            layoutWhiteningScoreShare.gone()
            frameLayoutLine72Share.gone()
            layoutOralHygieneScoreShare.gone()
            layoutOralHygieneScoreShareCenter.visible()
        }

        // If checkup type is whitening then remove oral hygiene score
        if (removeOralHygieneScore) {
            layoutWhiteningScoreShare.gone()
            frameLayoutLine72Share.gone()
            layoutOralHygieneScoreShare.gone()
            layoutWhiteningScoreShareCenter.visible()
        }

        // Show app info layout based on flavor version
        if (BuildConfig.IS_FARSI)
            layoutAppInfoFarsi.visible()
        else
            layoutAppInfoGlobal.visible()
    }
}