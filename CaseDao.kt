package com.rcsed.orthologbook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases")
    suspend fun getAllCases(): List<CaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cases: List<CaseEntity>)
}
