package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "policy_history",
    foreignKeys = [
        ForeignKey(
            entity = PolicyEntity::class,
            parentColumns = ["id"],
            childColumns = ["policyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("policyId")]
)
data class PolicyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val policyId: Long,
    val policyHolderName: String = "",
    val insurerName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val premiumAmount: Double,
    val attachmentRefs: String = ""
)
