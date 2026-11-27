package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppLanguage
import com.example.data.local.FavoriteClubEntity
import com.example.data.local.RecentlyViewedEntity
import com.example.data.local.SavedKitEntity
import com.example.data.local.SavedLogoEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesDataStore
import com.example.data.model.ClubKitBundle
import com.example.data.model.CustomizationTemplate
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.KitCategory
import com.example.data.model.LogoDesignSpec
import com.example.data.model.LogoTemplatePreset
import com.example.data.model.SortOption
import com.example.data.remote.PublishedKitUrlResult
import com.example.data.repository.DesignRepository
import com.example.data.repository.KitCatalogRepository
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CatalogSpecialFilter(val label: String) {
    ALL("All Kits"),
    TRENDING("Trending"),
    RECENTLY_ADDED("Recently Added"),
    PREMIER_LEAGUE("Premier League"),
    LA_LIGA("La Liga"),
    SERIE_A("Serie A"),
    BUNDESLIGA("Bundesliga"),
    LIGUE_1("Ligue 1"),
    OTHER_LEAGUES("Other Leagues"),
    NATIONAL_TEAMS("National Teams"),
    CLASSIC("Classic"),
    RETRO("Retro"),
    FANTASY("Fantasy")
}

class MainStudioViewModel(
    private val catalogRepo: KitCatalogRepository,
    private val designRepo: DesignRepository,
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    val allClubs: StateFlow<List<ClubKitBundle>> = catalogRepo.clubs
    val featuredTemplates: List<CustomizationTemplate> = catalogRepo.templates
    val logoTemplates: List<LogoTemplatePreset> = catalogRepo.logoTemplates

    val trendingClubs: List<ClubKitBundle> = catalogRepo.getTrendingClubs()
    val recentlyAddedClubs: List<ClubKitBundle> = catalogRepo.getRecentlyAddedClubs()
    val popularClubCollections: List<ClubKitBundle> = catalogRepo.getPopularClubCollections()
    val nationalTeams: List<ClubKitBundle> = catalogRepo.getNationalTeams()

    val savedKits: StateFlow<List<SavedKitEntity>> = designRepo.savedKits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedLogos: StateFlow<List<SavedLogoEntity>> = designRepo.savedLogos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteClubEntity>> = catalogRepo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyViewed: StateFlow<List<RecentlyViewedEntity>> = catalogRepo.recentlyViewed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = prefsStore.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(CatalogSpecialFilter.ALL)
    val selectedFilter: StateFlow<CatalogSpecialFilter> = _selectedFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.MOST_POPULAR)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _isRemoteSyncing = MutableStateFlow(false)
    val isRemoteSyncing: StateFlow<Boolean> = _isRemoteSyncing.asStateFlow()

    val filteredClubs: StateFlow<List<ClubKitBundle>> = combine(
        allClubs,
        _searchQuery,
        _selectedFilter,
        _sortOption
    ) { clubs, query, filter, sort ->
        val q = query.trim().lowercase()
        val byFilter = when (filter) {
            CatalogSpecialFilter.ALL -> clubs
            CatalogSpecialFilter.TRENDING -> clubs.filter { it.isTrending }
            CatalogSpecialFilter.RECENTLY_ADDED -> clubs.filter { it.isRecentlyAdded }
            CatalogSpecialFilter.PREMIER_LEAGUE -> clubs.filter { it.category == KitCategory.PREMIER_LEAGUE }
            CatalogSpecialFilter.LA_LIGA -> clubs.filter { it.category == KitCategory.LA_LIGA }
            CatalogSpecialFilter.SERIE_A -> clubs.filter { it.category == KitCategory.SERIE_A }
            CatalogSpecialFilter.BUNDESLIGA -> clubs.filter { it.category == KitCategory.BUNDESLIGA }
            CatalogSpecialFilter.LIGUE_1 -> clubs.filter { it.category == KitCategory.LIGUE_1 }
            CatalogSpecialFilter.OTHER_LEAGUES -> clubs.filter { it.category == KitCategory.OTHER_LEAGUES }
            CatalogSpecialFilter.NATIONAL_TEAMS -> clubs.filter { it.category == KitCategory.NATIONAL_TEAMS }
            CatalogSpecialFilter.CLASSIC -> clubs.filter { it.category == KitCategory.CLASSIC }
            CatalogSpecialFilter.RETRO -> clubs.filter { it.category == KitCategory.RETRO }
            CatalogSpecialFilter.FANTASY -> clubs.filter { it.category == KitCategory.FANTASY }
        }
        val byQuery = if (q.isEmpty()) byFilter else {
            byFilter.filter { club ->
                club.clubName.lowercase().contains(q) ||
                    club.country.lowercase().contains(q) ||
                    club.league.lowercase().contains(q) ||
                    club.season.lowercase().contains(q) ||
                    club.category.displayName.lowercase().contains(q) ||
                    club.shortCode.lowercase().contains(q)
            }
        }
        when (sort) {
            SortOption.MOST_POPULAR -> byQuery.sortedBy { it.popularityRank }
            SortOption.RECENTLY_ADDED -> byQuery.sortedByDescending { it.isRecentlyAdded }
            SortOption.ALPHABETICAL -> byQuery.sortedBy { it.clubName }
            SortOption.MOST_DOWNLOADED -> byQuery.sortedByDescending { it.downloads }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFilter(filter: CatalogSpecialFilter) {
        _selectedFilter.value = filter
    }

    fun selectSort(sort: SortOption) {
        _sortOption.value = sort
    }

    fun getClubById(clubId: String): ClubKitBundle? = catalogRepo.getClubById(clubId)

    fun observeIsFavorite(clubId: String) = catalogRepo.observeIsFavorite(clubId)

    fun toggleFavorite(club: ClubKitBundle, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            catalogRepo.toggleFavorite(club, currentlyFavorite)
        }
    }

    fun markClubViewed(club: ClubKitBundle) {
        viewModelScope.launch {
            catalogRepo.markViewed(club)
        }
    }

    fun decodeKitSpec(json: String): JerseyDesignSpec = designRepo.decodeKitSpec(json)

    fun decodeLogoSpec(json: String): LogoDesignSpec = designRepo.decodeLogoSpec(json)

    fun duplicateSavedKit(kit: SavedKitEntity) {
        viewModelScope.launch { designRepo.duplicateKit(kit) }
    }

    fun deleteSavedKit(id: Long) {
        viewModelScope.launch { designRepo.deleteKit(id) }
    }

    fun duplicateSavedLogo(logo: SavedLogoEntity) {
        viewModelScope.launch { designRepo.duplicateLogo(logo) }
    }

    fun deleteSavedLogo(id: Long) {
        viewModelScope.launch { designRepo.deleteLogo(id) }
    }

    fun updateProfile(name: String, tagline: String) {
        viewModelScope.launch { prefsStore.updateProfile(name, tagline) }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch { prefsStore.setThemePreference(theme) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsStore.setLanguage(language) }
    }

    private val _publishedUrls = MutableStateFlow<Map<String, PublishedKitUrlResult>>(emptyMap())
    val publishedUrls: StateFlow<Map<String, PublishedKitUrlResult>> = _publishedUrls.asStateFlow()

    fun cachePublishedUrl(clubId: String, slotIndex: Int, result: PublishedKitUrlResult) {
        if (!result.success || result.url.isBlank()) return
        _publishedUrls.value = _publishedUrls.value + ("${clubId}_$slotIndex" to result)
    }

    fun recordExport() {
        viewModelScope.launch { prefsStore.incrementExportCount() }
    }

    fun attemptCatalogSync(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isRemoteSyncing.value = true
            val synced = catalogRepo.refreshFromRemoteIfAvailable()
            _isRemoteSyncing.value = false
            onResult(synced)
        }
    }

    class Factory(
        private val catalogRepo: KitCatalogRepository,
        private val designRepo: DesignRepository,
        private val prefsStore: UserPreferencesDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainStudioViewModel(catalogRepo, designRepo, prefsStore) as T
        }
    }
}
