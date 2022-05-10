package com.straiberry.android.checkup.common.helper

import kotlin.math.sqrt

class Variance(var data: IntArray) {
    var size: Int = data.size
    val mean: Double
        get() {
            var sum = 0.0
            for (a in data) sum += a
            return sum / size
        }
    val variance: Double
        get() {
            val mean = mean
            var temp = 0.0
            for (a in data) temp += (a - mean) * (a - mean)
            return temp / (size - 1)
        }
    val stdDev: Double
        get() = sqrt(variance)

}