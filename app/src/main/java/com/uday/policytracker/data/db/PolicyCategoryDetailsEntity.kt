package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "policy_category_details",
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
data class PolicyCategoryDetailsEntity(
    @PrimaryKey val policyId: Long,
    val premiumFrequency: String = "",
    val premiumDueDayOfMonth: Int? = null,
    val coverageAmount: Double? = null,
    val coverageAmountUnit: String = "",
    val premiumPaymentStartEpochDay: Long? = null,
    val premiumPaymentEndEpochDay: Long? = null,
    val premiumPaymentTermYears: Int? = null,
    val policyValidityEndEpochDay: Long? = null,
    val policyTermYears: Int? = null,
    val entryAge: Int? = null,
    val coverageTillAge: Int? = null,
    val nomineeName: String = "",
    val nomineeRelationship: String = "",
    val riderAddons: String = "",
    val paymentMode: String = "",
    val gracePeriodDays: Int? = null,
    val termPolicyStatus: String = "",
    val totalPayments: Int? = null,
    val paidPayments: Int? = null,
    val vehicleNumber: String = "",
    val vehicleType: String = "",
    val makeModelVariant: String = "",
    val fuelType: String = "",
    val vehiclePolicyType: String = "",
    val vehicleAddons: String = "",
    val claimHistory: String = "",
    val documentType: String = "",
    val issuingRto: String = "",
    val stateName: String = "",
    val vehicleClass: String = "",
    val ownerName: String = "",
    val linkedVehicleNumber: String = "",
    val dateOfBirth: String = "",
    val customFieldValuesJson: String = ""
)
