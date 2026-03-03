package com.uday.policytracker.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class FolderWithDetails(
    @Embedded val folder: CategoryFolderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PolicyFolderCrossRef::class,
            parentColumn = "folderId",
            entityColumn = "policyId"
        )
    )
    val policies: List<PolicyEntity>,
    @Relation(parentColumn = "id", entityColumn = "folderId")
    val attachments: List<FolderAttachmentEntity>
)
