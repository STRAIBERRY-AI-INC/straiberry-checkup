package com.straiberry.android.checkup.common.extentions


import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupType

import com.straiberry.android.common.model.JawPosition
import kotlin.math.roundToInt

val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Int.dp(context: Context): Int =
    (this * context.resources.displayMetrics.density).roundToInt()

fun Any?.toStringOrEmpty() = this?.toString() ?: ""
fun Any?.toStringOrEmptyChartData() = this?.toString() ?: "-1"


fun Any?.toStringOrEmptyHorizontalChartData(maximumValue: String): String {
    return when {
        this == null -> {
            "-1"
        }
        this.toString().toFloat() >= maximumValue.toFloat() -> maximumValue
        else -> this.toString()
    }
}

fun Any?.toStringOrZero() = this?.toString() ?: "0"


fun Double.toImageXPosition(width: Int): Int {
    return (this * width).toInt()
}

fun Double.toImageYPosition(height: Int): Int {
    return (this * height).toInt()
}


fun Int.convertToCheckupName(context: Context): String {
    return when (this) {
        -1 -> context.getString(R.string.regular_checkup)
        0 -> context.getString(R.string.teeth_whitening)
        1 -> context.getString(R.string.toothache_amp_tooth_sensitivity)
        2 -> context.getString(R.string.problems_with_previous_treatment)
        3 -> context.getString(R.string.others)
        else -> context.getString(R.string.regular_checkup)
    }
}

fun Int.convertCavityClassToDrawable(context: Context): Drawable {
    return when (this) {
        0 -> ContextCompat.getDrawable(context, R.drawable.ic_amalgam_filling)!!
        1 -> ContextCompat.getDrawable(context, R.drawable.ic_calcules)!!
        2 -> ContextCompat.getDrawable(context, R.drawable.ic_carries)!!
        3 -> ContextCompat.getDrawable(context, R.drawable.ic_white_spot)!!
        4 -> ContextCompat.getDrawable(context, R.drawable.ic_dental_implant)!!
        5 -> ContextCompat.getDrawable(context, R.drawable.ic_bone_loss)!!
        6 -> ContextCompat.getDrawable(context, R.drawable.ic_amalgam_restoration_overhang)!!
        7 -> ContextCompat.getDrawable(context, R.drawable.ic_infectious_apical_lesion)!!
        8 -> ContextCompat.getDrawable(context, R.drawable.ic_remained_dental_root)!!
        9 -> ContextCompat.getDrawable(context, R.drawable.ic_malposed_wisdom_tooth)!!
        10 -> ContextCompat.getDrawable(context, R.drawable.ic_tooth_with_missing_counterpart)!!
        else -> ContextCompat.getDrawable(
            context,
            com.straiberry.android.common.R.drawable.ic_amalgam_filling
        )!!
    }


}

fun Int.convertToCheckupType(): CheckupType {
    return when (this) {
        -1 -> CheckupType.Regular
        0 -> CheckupType.Whitening
        1 -> CheckupType.Sensitivity
        2 -> CheckupType.Treatments
        3 -> CheckupType.Others
        4 -> CheckupType.XRays
        else -> CheckupType.Regular
    }
}


fun Int.convertCavityClassToString(context: Context): String {
    return when (this) {
        0 -> context.getString(R.string.amalgam_filling)
        1 -> context.getString(R.string.calculus)
        2 -> context.getString(R.string.carries)
        3 -> context.getString(R.string.white_spot)
        4 -> context.getString(R.string.dental_implant)
        5 -> context.getString(R.string.bone_loss)
        6 -> context.getString(R.string.amalgam_restoration_overhang)
        7 -> context.getString(R.string.infectious_apical_lesion)
        8 -> context.getString(R.string.remained_dental_root)
        9 -> context.getString(R.string.malposed_wisdom_tooth)
        10 -> context.getString(R.string.tooth_with_missing_counterpart)
        else -> context.getString(com.straiberry.android.common.R.string.amalgam_filling)
    }
}

fun String.convertJawToInt(): Int {
    return when (this) {
        "front" -> 1
        "upper" -> -1
        "lower" -> 0
        else -> 1
    }
}

fun Int.convertIntToJaw(): String {
    return when (this) {
        1 -> "front"
        -1 -> "upper"
        0 -> "lower"
        else -> "Over"
    }
}

@Throws(NullPointerException::class)
fun Int.convertToothIdToDental(): String {
    return when (this) {
        // Upper right
        0 -> "ur8"
        1 -> "ur7"
        2 -> "ur6"
        3 -> "ur5"
        4 -> "ur4"
        5 -> "ur3"
        6 -> "ur2"
        7 -> "ur1"
        // Upper left
        8 -> "ul1"
        9 -> "ul2"
        10 -> "ul3"
        11 -> "ul4"
        12 -> "ul5"
        13 -> "ul6"
        14 -> "ul7"
        15 -> "ul8"
        // Lower right
        16 -> "lr8"
        17 -> "lr7"
        18 -> "lr6"
        19 -> "lr5"
        20 -> "lr4"
        21 -> "lr3"
        22 -> "lr2"
        23 -> "lr1"
        // Lower left
        24 -> "ll1"
        25 -> "ll2"
        26 -> "ll3"
        27 -> "ll4"
        28 -> "ll5"
        29 -> "ll6"
        30 -> "ll7"
        31 -> "ll8"
        else -> throw NullPointerException("None of inputs match the converter")
    }
}


@Throws(NullPointerException::class)
fun String.convertDentalToToothId(): Int {
    return when (this) {
        // Upper Right
        "ur8" -> 0
        "ur7" -> 1
        "ur6" -> 2
        "ur5" -> 3
        "ur4" -> 4
        "ur3" -> 5
        "ur2" -> 6
        "ur1" -> 7
        // Upper Left
        "ul1" -> 8
        "ul2" -> 9
        "ul3" -> 10
        "ul4" -> 11
        "ul5" -> 12
        "ul6" -> 13
        "ul7" -> 14
        "ul8" -> 15
        // Lower Right
        "lr8" -> 16
        "lr7" -> 17
        "lr6" -> 18
        "lr5" -> 19
        "lr4" -> 20
        "lr3" -> 21
        "lr2" -> 22
        "lr1" -> 23
        // Lower Left
        "ll1" -> 24
        "ll2" -> 25
        "ll3" -> 26
        "ll4" -> 27
        "ll5" -> 28
        "ll6" -> 29
        "ll7" -> 30
        "ll8" -> 31
        else -> throw NullPointerException("None of inputs match the converter")
    }
}


fun List<String>.convertToToothId(): ArrayList<Int> {
    val listOfToothId = arrayListOf<Int>()
    this.forEach {
        listOfToothId.add(it.convertDentalToToothId())
    }
    return listOfToothId
}

fun Int.convertToothClassToFrontJawPosition(): JawPosition {
    return if (this in 0..15)
        JawPosition.FrontTeethUpper
    else
        JawPosition.FrontTeethLower
}

fun Int.convertToJawPosition(): JawPosition {
    return when (this) {
        1 -> JawPosition.FrontTeeth
        -1 -> JawPosition.UpperJaw
        0 -> JawPosition.LowerJaw
        2 -> JawPosition.FrontTeeth
        else -> JawPosition.FrontTeeth
    }
}
