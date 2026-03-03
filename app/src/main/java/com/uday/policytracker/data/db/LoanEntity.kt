package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanName: String,
    val lenderName: String,
    val principalAmount: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val emiAmount: Double,
    val paymentFrequency: String = "Monthly",
    val startDateEpochDay: Long,
    val createdAtEpochMillis: Long
)
