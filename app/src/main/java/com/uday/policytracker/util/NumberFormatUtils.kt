package com.uday.policytracker.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun formatIndianAmount(value: Double, fractionDigits: Int = 0): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")) as DecimalFormat
    formatter.maximumFractionDigits = fractionDigits
    formatter.minimumFractionDigits = fractionDigits
    return formatter.format(value)
}
