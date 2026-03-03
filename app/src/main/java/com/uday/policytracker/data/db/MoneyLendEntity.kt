package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "money_lend_entries")
data class MoneyLendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val borrowerName: String,
    val amount: Double,
    val interestRate: Double,
    val startDateEpochDay: Long,
    val dueDateEpochDay: Long?,
    val notes: String,
    val paidInstallmentsJson: String,
    val isRepaid: Boolean,
    val createdAtEpochMillis: Long
)

