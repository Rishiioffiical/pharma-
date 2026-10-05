package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PharmaceuticalDrug

/**
 * Dedicated Room database for Pharmaceutical Drugs library.
 */
@Database(
    entities = [PharmaceuticalDrug::class],
    version = 1,
    exportSchema = false
)
abstract class PharmaceuticalDrugDatabase : RoomDatabase() {
    abstract fun pharmaceuticalDrugDao(): PharmaceuticalDrugDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaceuticalDrugDatabase? = null

        fun getInstance(context: Context): PharmaceuticalDrugDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaceuticalDrugDatabase::class.java,
                    "pharmaceutical_drugs_library_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
