package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folder_attachments",
    foreignKeys = [
        ForeignKey(
            entity = CategoryFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId")]
)
data class FolderAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val addedAtEpochMillis: Long
)
