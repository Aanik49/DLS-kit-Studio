package com.example.data.remote

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class RemoteCatalogPayload(
    val schemaVersion: Int = 1,
    val seasonLabel: String = "2025/26",
    val updatedAtIso: String = "",
    val featuredBannerTitle: String = "",
    val clubs: List<RemoteClubDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteClubDto(
    val id: String,
    val clubName: String,
    val shortCode: String,
    val country: String,
    val league: String,
    val categoryKey: String,
    val season: String,
    val info: String,
    val downloads: Int = 0,
    val isTrending: Boolean = false,
    val isRecentlyAdded: Boolean = false,
    val crestPrimaryHex: String,
    val crestSecondaryHex: String,
    val variants: List<RemoteKitVariantDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteKitVariantDto(
    val slotType: String,
    val label: String,
    val kitUrl: String,
    val importCode: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val thirdColorHex: String,
    val patternKey: String,
    val collarKey: String,
    val sponsorText: String
)

interface DlsCatalogApiService {
    @GET("api/v1/catalog.json")
    suspend fun fetchCatalog(
        @Query("season") season: String? = null,
        @Query("category") category: String? = null
    ): Response<RemoteCatalogPayload>

    @GET("api/v1/clubs/{clubId}.json")
    suspend fun fetchClubDetails(
        @Path("clubId") clubId: String
    ): Response<RemoteClubDto>
}

object RetrofitCatalogClient {
    // Configurable base URL for remote headless CMS / JSON feed updates
    private const val DEFAULT_BASE_URL = "https://cdn.dlskitstudio.example/"

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    val apiService: DlsCatalogApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DlsCatalogApiService::class.java)
    }
}
