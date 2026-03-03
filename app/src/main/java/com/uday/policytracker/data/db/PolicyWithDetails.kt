package com.uday.policytracker.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class PolicyWithDetails(
    @Embedded val policy: PolicyEntity,
    @Relation(parentColumn = "id", entityColumn = "policyId")
    val categoryDetails: PolicyCategoryDetailsEntity?,
    @Relation(parentColumn = "id", entityColumn = "policyId")
    val history: List<PolicyHistoryEntity>,
    @Relation(parentColumn = "id", entityColumn = "policyId")
    val attachments: List<AttachmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "policyId")
    val futurePolicies: List<FuturePolicyEntity>
)
