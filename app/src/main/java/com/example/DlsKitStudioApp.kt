package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesDataStore
import com.example.data.repository.DesignRepository
import com.example.data.repository.KitCatalogRepository

class DlsKitStudioApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferencesDataStore: UserPreferencesDataStore by lazy { UserPreferencesDataStore(this) }
    val catalogRepository: KitCatalogRepository by lazy {
        KitCatalogRepository(
            favoriteClubDao = database.favoriteClubDao(),
            recentlyViewedDao = database.recentlyViewedDao()
        )
    }
    val designRepository: DesignRepository by lazy {
        DesignRepository(
            savedKitDao = database.savedKitDao(),
            savedLogoDao = database.savedLogoDao()
        )
    }
}
