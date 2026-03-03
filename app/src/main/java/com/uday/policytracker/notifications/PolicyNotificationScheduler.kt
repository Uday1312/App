package com.uday.policytracker.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "policy_expiry_notifications"

fun schedulePolicyExpiryNotifications(context: Context) {
    val request = PeriodicWorkRequestBuilder<PolicyExpiryNotificationWorker>(24, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

