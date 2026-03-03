package com.uday.policytracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_folders")
data class CategoryFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val createdAtEpochMillis: Long,
    val colorHex: String = "#F6E49A"
)
