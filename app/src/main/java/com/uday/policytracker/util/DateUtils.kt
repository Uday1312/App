package com.uday.policytracker.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.Instant

private val uiFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
private val legacyUiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun formatEpochDay(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(uiFormatter)
fun formatEpochDayIso(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(isoFormatter)
fun formatTodayUi(): String = LocalDate.now().format(uiFormatter)
fun formatFutureUi(days: Long): String = LocalDate.now().plusDays(days).format(uiFormatter)

fun parseFlexibleDate(value: String): Long? {
    val input = value.trim()
    return runCatching { LocalDate.parse(input, uiFormatter).toEpochDay() }.getOrNull()
        ?: runCatching { LocalDate.parse(input, legacyUiFormatter).toEpochDay() }.getOrNull()
        ?: runCatching { LocalDate.parse(input, isoFormatter).toEpochDay() }.getOrNull()
}

fun epochDayToMillis(epochDay: Long): Long {
    return LocalDate.ofEpochDay(epochDay)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun millisToUiDate(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(uiFormatter)
}

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

fun daysToExpiry(expiryEpochDay: Long): Long = expiryEpochDay - todayEpochDay()

fun formatTimeLeft(expiryEpochDay: Long): String {
    val today = LocalDate.now()
    val expiry = LocalDate.ofEpochDay(expiryEpochDay)
    if (expiry.isBefore(today)) return "Expired"
    val period = Period.between(today, expiry)
    val years = period.years
    val months = period.months
    val days = period.days

    return when {
        years > 0 -> {
            val y = if (years == 1) "1 year" else "$years years"
            val m = when {
                months <= 0 -> ""
                months == 1 -> " 1 month"
                else -> " $months months"
            }
            "$y$m left"
        }
        months > 0 -> {
            val m = if (months == 1) "1 month" else "$months months"
            val d = when {
                days <= 0 -> ""
                days == 1 -> " 1 day"
                else -> " $days days"
            }
            "$m$d left"
        }
        days == 0 -> "Today"
        days == 1 -> "1 day left"
        else -> "$days days left"
    }
}
