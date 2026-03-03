package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "future_policies",
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
data class FuturePolicyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val policyId: Long,
    val policyHolderName: String = "",
    val policyName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val expiryDateEpochDay: Long,
    val insurerName: String,
    val premiumAmount: Double,
    val createdAtEpochMillis: Long,
    val attachmentRefs: String = ""
)
