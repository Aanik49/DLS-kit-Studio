package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedKitEntity::class,
        SavedLogoEntity::class,
        FavoriteClubEntity::class,
        RecentlyViewedEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedKitDao(): SavedKitDao
    abstract fun savedLogoDao(): SavedLogoDao
    abstract fun favoriteClubDao(): FavoriteClubDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dls_kit_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
