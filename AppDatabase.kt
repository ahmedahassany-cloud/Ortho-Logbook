package com.rcsed.orthologbook

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [CaseEntity::class, PhotoEntity::class, PdfEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
    abstract fun photoDao(): PhotoDao
    abstract fun pdfDao(): PdfDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rcsed_ortho_logbook.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
