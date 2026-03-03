package com.uday.policytracker.data.db

data class FolderPolicyAttachmentRow(
    val attachmentId: Long,
    val policyId: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val addedAtEpochMillis: Long,
    val policyName: String,
    val policyNumber: String,
    val policyStartEpochDay: Long,
    val policyExpiryEpochDay: Long
)
