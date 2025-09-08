package com.rcsed.orthologbook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: String,
    val age: Int?,
    val gender: String?,
    val diagnosis: String,
    val procedure: String,
    val approach: String?,
    val surgeonRole: String,
    val date: Long,
    val hospital: String?,
    val notes: String?
)
