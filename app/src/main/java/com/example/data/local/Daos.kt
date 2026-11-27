package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedKitDao {
    @Query("SELECT * FROM saved_kits ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedKitEntity>>

    @Query("SELECT * FROM saved_kits WHERE id = :id")
    suspend fun getById(id: Long): SavedKitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kit: SavedKitEntity): Long

    @Query("DELETE FROM saved_kits WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SavedLogoDao {
    @Query("SELECT * FROM saved_logos ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedLogoEntity>>

    @Query("SELECT * FROM saved_logos WHERE id = :id")
    suspend fun getById(id: Long): SavedLogoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(logo: SavedLogoEntity): Long

    @Query("DELETE FROM saved_logos WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface FavoriteClubDao {
    @Query("SELECT * FROM favorite_clubs ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteClubEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_clubs WHERE clubId = :clubId)")
    fun observeIsFavorite(clubId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteClubEntity)

    @Query("DELETE FROM favorite_clubs WHERE clubId = :clubId")
    suspend fun deleteByClubId(clubId: String)
}

@Dao
interface RecentlyViewedDao {
    @Query("SELECT * FROM recently_viewed ORDER BY viewedAt DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentlyViewedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecentlyViewedEntity)

    @Query("DELETE FROM recently_viewed")
    suspend fun clearAll()
}
