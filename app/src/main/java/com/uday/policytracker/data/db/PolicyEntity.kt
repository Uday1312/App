package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "policies")
data class PolicyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val policyHolderName: String = "",
    val policyName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val expiryDateEpochDay: Long,
    val insurerName: String,
    val previousInsurerName: String,
    val premiumAmount: Double,
    val notes: String = ""
)
