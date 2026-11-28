package com.example.data.repository

import com.example.data.local.FavoriteClubDao
import com.example.data.local.FavoriteClubEntity
import com.example.data.local.RecentlyViewedDao
import com.example.data.local.RecentlyViewedEntity
import com.example.data.model.ClubKitBundle
import com.example.data.model.CustomizationTemplate
import com.example.data.model.KitCategory
import com.example.data.model.LogoTemplatePreset
import com.example.data.remote.DlsCatalogApiService
import com.example.data.remote.GoalkeeperStudioCatalog
import com.example.data.remote.RetrofitCatalogClient
import com.example.data.remote.SampleKitCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KitCatalogRepository(
    private val favoriteClubDao: FavoriteClubDao,
    private val recentlyViewedDao: RecentlyViewedDao,
    private val apiService: DlsCatalogApiService = RetrofitCatalogClient.apiService
) {
    private val _clubs = MutableStateFlow(SampleKitCatalog.clubs)
    val clubs: StateFlow<List<ClubKitBundle>> = _clubs.asStateFlow()

    val templates: List<CustomizationTemplate> = SampleKitCatalog.featuredTemplates
    val gkTemplates: List<CustomizationTemplate> = GoalkeeperStudioCatalog.gkTemplates
    val logoTemplates: List<LogoTemplatePreset> = SampleKitCatalog.logoTemplates

    val favorites: Flow<List<FavoriteClubEntity>> = favoriteClubDao.observeAll()
    val recentlyViewed: Flow<List<RecentlyViewedEntity>> = recentlyViewedDao.observeAll()

    fun observeIsFavorite(clubId: String): Flow<Boolean> = favoriteClubDao.observeIsFavorite(clubId)

    fun getClubById(clubId: String): ClubKitBundle? = _clubs.value.firstOrNull { it.id == clubId }

    suspend fun toggleFavorite(club: ClubKitBundle, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            favoriteClubDao.deleteByClubId(club.id)
        } else {
            favoriteClubDao.insert(
                FavoriteClubEntity(
                    clubId = club.id,
                    clubName = club.clubName,
                    league = club.league
                )
            )
        }
    }

    suspend fun markViewed(club: ClubKitBundle) {
        recentlyViewedDao.upsert(
            RecentlyViewedEntity(
                clubId = club.id,
                clubName = club.clubName,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Attempts remote CMS sync via Retrofit; gracefully preserves offline bundled catalog
     * when offline or using default sample environment.
     */
    suspend fun refreshFromRemoteIfAvailable(): Boolean {
        return try {
            val response = apiService.fetchCatalog()
            response.isSuccessful && response.body()?.clubs?.isNotEmpty() == true
        } catch (_: Exception) {
            false
        }
    }

    fun getTrendingClubs(): List<ClubKitBundle> =
        _clubs.value.filter { it.isTrending }.sortedBy { it.popularityRank }

    fun getRecentlyAddedClubs(): List<ClubKitBundle> =
        _clubs.value.filter { it.isRecentlyAdded }

    fun getPopularClubCollections(): List<ClubKitBundle> =
        _clubs.value.filter { it.category != KitCategory.NATIONAL_TEAMS }
            .sortedByDescending { it.downloads }

    fun getNationalTeams(): List<ClubKitBundle> =
        _clubs.value.filter { it.category == KitCategory.NATIONAL_TEAMS }
}
