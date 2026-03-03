package com.uday.policytracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uday.policytracker.data.db.PolicyDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private const val CHANNEL_ID = "policy_expiry_alerts"
private const val CHANNEL_NAME = "Policy Expiry Alerts"
private const val PREFS = "policy_notification_prefs"

class PolicyExpiryNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        ensureChannel()
        if (!canNotify()) return Result.success()

        val dao = PolicyDatabase.getInstance(applicationContext).policyDao()
        val details = dao.observePoliciesWithDetails().first()
        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now().toEpochDay()

        details.forEach { row ->
            val policy = row.policy
            if (row.futurePolicies.isNotEmpty()) return@forEach
            val days = policy.expiryDateEpochDay - today

            val key60 = "once60_${policy.id}_${policy.expiryDateEpochDay}"
            val keyDaily = "daily_${policy.id}_${policy.expiryDateEpochDay}"

            when {
                days in 31..60 -> {
                    if (!prefs.getBoolean(key60, false)) {
                        notifyPolicy(
                            id = policy.id.toInt(),
                            title = "Policy expiring in $days days",
                            body = "${policy.policyName} (${policy.policyNumber}) expires on ${com.uday.policytracker.util.formatEpochDay(policy.expiryDateEpochDay)}"
                        )
                        prefs.edit().putBoolean(key60, true).apply()
                    }
                    prefs.edit().remove(keyDaily).apply()
                }

                days in 0..30 -> {
                    val lastNotified = prefs.getLong(keyDaily, Long.MIN_VALUE)
                    if (lastNotified != today) {
                        notifyPolicy(
                            id = (policy.id + 100000).toInt(),
                            title = "Policy expiring soon",
                            body = "${policy.policyName} (${policy.policyNumber}) expires in ${com.uday.policytracker.util.formatTimeLeft(policy.expiryDateEpochDay)}"
                        )
                        prefs.edit().putLong(keyDaily, today).apply()
                    }
                }

                else -> {
                    prefs.edit().remove(keyDaily).apply()
                }
            }
        }

        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    private fun canNotify(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun notifyPolicy(id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }
}

