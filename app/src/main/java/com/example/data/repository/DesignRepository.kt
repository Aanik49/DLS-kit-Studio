package com.example.data.repository

import com.example.data.local.SavedKitDao
import com.example.data.local.SavedKitEntity
import com.example.data.local.SavedLogoDao
import com.example.data.local.SavedLogoEntity
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.LogoDesignSpec
import com.example.data.remote.RetrofitCatalogClient
import kotlinx.coroutines.flow.Flow

class DesignRepository(
    private val savedKitDao: SavedKitDao,
    private val savedLogoDao: SavedLogoDao
) {
    private val kitAdapter = RetrofitCatalogClient.moshi.adapter(JerseyDesignSpec::class.java)
    private val logoAdapter = RetrofitCatalogClient.moshi.adapter(LogoDesignSpec::class.java)

    val savedKits: Flow<List<SavedKitEntity>> = savedKitDao.observeAll()
    val savedLogos: Flow<List<SavedLogoEntity>> = savedLogoDao.observeAll()

    fun encodeKitSpec(spec: JerseyDesignSpec): String = kitAdapter.toJson(spec)

    fun decodeKitSpec(json: String): JerseyDesignSpec {
        return try {
            kitAdapter.fromJson(json) ?: JerseyDesignSpec()
        } catch (_: Exception) {
            JerseyDesignSpec()
        }
    }

    fun encodeLogoSpec(spec: LogoDesignSpec): String = logoAdapter.toJson(spec)

    fun decodeLogoSpec(json: String): LogoDesignSpec {
        return try {
            logoAdapter.fromJson(json) ?: LogoDesignSpec()
        } catch (_: Exception) {
            LogoDesignSpec()
        }
    }

    suspend fun saveKit(
        existingId: Long? = null,
        name: String,
        clubName: String,
        league: String,
        season: String,
        spec: JerseyDesignSpec
    ): Long {
        val now = System.currentTimeMillis()
        val entity = SavedKitEntity(
            id = existingId ?: 0L,
            name = name.ifBlank { "${clubName.ifBlank { "Custom" }} ${spec.slotType.shortLabel}" },
            clubName = clubName.ifBlank { spec.clubName },
            league = league.ifBlank { "Custom Club" },
            season = season.ifBlank { "2025/26" },
            slotType = spec.slotType.name,
            specJson = encodeKitSpec(spec),
            createdAt = now,
            updatedAt = now
        )
        return savedKitDao.upsert(entity)
    }

    suspend fun duplicateKit(kit: SavedKitEntity): Long {
        val now = System.currentTimeMillis()
        return savedKitDao.upsert(
            kit.copy(
                id = 0L,
                name = "${kit.name} Copy",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun deleteKit(id: Long) = savedKitDao.deleteById(id)

    suspend fun saveLogo(
        existingId: Long? = null,
        name: String,
        spec: LogoDesignSpec
    ): Long {
        val now = System.currentTimeMillis()
        val entity = SavedLogoEntity(
            id = existingId ?: 0L,
            name = name.ifBlank { "${spec.clubName.ifBlank { "Club" }} Crest" },
            clubName = spec.clubName.ifBlank { "Custom Club" },
            specJson = encodeLogoSpec(spec),
            createdAt = now,
            updatedAt = now
        )
        return savedLogoDao.upsert(entity)
    }

    suspend fun duplicateLogo(logo: SavedLogoEntity): Long {
        val now = System.currentTimeMillis()
        return savedLogoDao.upsert(
            logo.copy(
                id = 0L,
                name = "${logo.name} Copy",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun deleteLogo(id: Long) = savedLogoDao.deleteById(id)
}
