package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "policy_folder_cross_ref",
    indices = [Index("folderId"), Index("policyId")],
    primaryKeys = ["policyId", "folderId"]
)
data class PolicyFolderCrossRef(
    val policyId: Long,
    val folderId: Long
)
