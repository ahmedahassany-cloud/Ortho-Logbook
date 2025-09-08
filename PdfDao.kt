package com.rcsed.orthologbook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdfs WHERE caseId = :caseId")
    suspend fun getPdfsForCase(caseId: Long): List<PdfEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pdfs: List<PdfEntity>)
}
