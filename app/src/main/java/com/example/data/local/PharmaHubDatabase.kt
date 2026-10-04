package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        ResourceEntity::class,
        DrugEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        CommunityPostEntity::class,
        ChatMessageEntity::class,
        StudyAnalyticsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PharmaHubDatabase : RoomDatabase() {
    abstract fun dao(): PharmaHubDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaHubDatabase? = null

        fun getInstance(context: Context): PharmaHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaHubDatabase::class.java,
                    "pharmahub_academic_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
