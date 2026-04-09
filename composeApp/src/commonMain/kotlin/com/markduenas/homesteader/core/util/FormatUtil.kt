package com.markduenas.homesteader.core.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * KMP-compatible decimal formatting. Replaces String.format("%.Nf") which is JVM-only.
 */
fun Double.formatDecimal(decimals: Int = 2): String {
    if (isNaN() || isInfinite()) return "0.${"0".repeat(decimals)}"
    val negative = this < 0
    val absVal = abs(this)
    val factor = 10.0.pow(decimals.toDouble()).toLong()
    val rounded = round(absVal * factor).toLong()
    val intPart = rounded / factor
    val fracPart = rounded % factor
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return "${if (negative) "-" else ""}$intPart.$fracStr"
}
