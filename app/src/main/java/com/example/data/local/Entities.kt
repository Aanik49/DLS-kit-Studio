package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_kits")
data class SavedKitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val clubName: String,
    val league: String,
    val season: String,
    val slotType: String,
    val specJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_logos")
data class SavedLogoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val clubName: String,
    val specJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_clubs")
data class FavoriteClubEntity(
    @PrimaryKey val clubId: String,
    val clubName: String,
    val league: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val clubId: String,
    val clubName: String,
    val viewedAt: Long = System.currentTimeMillis()
)
