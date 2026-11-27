# DLS Kit Studio – Architecture & Content Extension Guide

## 1. Architecture Overview (MVVM + Repository Pattern)

DLS Kit Studio is structured into clean, modular layers:

- **`com.example.data.model`**:
  - Immutable domain models (`ClubKitBundle`, `KitVariant`, `JerseyDesignSpec`, `LogoDesignSpec`, `LogoLayer`, `CustomizationTemplate`, `LogoTemplatePreset`) annotated with `@JsonClass(generateAdapter = true)` for fast Moshi serialization.
- **`com.example.data.local`**:
  - **Room Database (`AppDatabase`)**: Stores user-authored kits (`SavedKitEntity`), custom club logos (`SavedLogoEntity`), bookmarked favorite clubs (`FavoriteClubEntity`), and browsing history (`RecentlyViewedEntity`) offline via reactive `Flow<List<...>>` DAOs.
  - **DataStore (`UserPreferencesDataStore`)**: Persists appearance mode (`DARK`, `LIGHT`, `SYSTEM`), active UI language (`en`, `es`, `pt`, `fr`), guest studio profile metadata, and export counters.
- **`com.example.data.remote`**:
  - **Retrofit + Moshi (`DlsCatalogApiService`, `RetrofitCatalogClient`)**: Defines a headless-CMS-ready REST contract (`api/v1/catalog.json`, `api/v1/clubs/{clubId}.json`) so new seasons, leagues, and kits can be distributed remotely without an APK release.
  - **Bundled Catalog (`SampleKitCatalog`)**: Provides an immediate offline-ready catalog across Premier League, La Liga, Serie A, Bundesliga, Ligue 1, Other Leagues, National Teams, Classic, Retro, and Fantasy collections.
- **`com.example.rendering`**:
  - **`JerseyVectorRenderer`**: High-precision Jetpack Compose `Canvas` renderer supporting Front/Back views, shorts/socks preview, 4 template fits, 9 jersey patterns, 5 collar styles, 4 sleeve styles, shoulder/side panels, manufacturer marks, Photo Picker uploaded crests/sponsors, and outlined back player name & number.
  - **`DlsUvTextureExporter`**: Generates authentic **512×512 PNG** Dream League Soccer UV-mapped sprite sheets (distinct front torso, back torso, left/right sleeves, collar strip, captain armband, shorts, and socks UV coordinates) and writes to `MediaStore.Images.Media` (`Pictures/DLSKitStudio`) + `FileProvider` for zero-permission sharing.
  - **`StudioPreviewExporter`**: Generates **1024×1024** Front & Back Jersey Showcase PNG cards.
  - **`LogoCanvasRenderer`**: Interactive drag-and-drop vector crest renderer & exporter (`512×512` transparent DLS PNG & `1024×1024` HD PNG).
- **`com.example.ui`**:
  - **`MainStudioViewModel`**, **`KitCreatorViewModel`**, **`LogoCreatorViewModel`** driving reactive `StateFlow` UIs across 5 main navigation sections (`Home`, `Kits`, `Create`, `Logos`, `Profile`) plus `KitDetailScreen` and `MyDesignsScreen`.

---

## 2. How to Add New Clubs, Seasons & Templates

### Option A: Local / Bundled Catalog (`SampleKitCatalog.kt`)
1. Add a new `ClubKitBundle` entry to `SampleKitCatalog.clubs`.
2. Specify `category` (`KitCategory.PREMIER_LEAGUE`, `KitCategory.NATIONAL_TEAMS`, `KitCategory.RETRO`, etc.), `season`, `crestPrimaryHex`, `crestSecondaryHex`, and 4 `JerseyDesignSpec` objects (`home`, `away`, `third`, `gk`) via `buildFourSlotVariants(...)`.
3. Add starter jersey presets to `SampleKitCatalog.featuredTemplates` or crest presets to `SampleKitCatalog.logoTemplates`.

### Option B: Remote CMS / JSON Feed (`DlsCatalogApiService.kt`)
1. Host a JSON document matching `RemoteCatalogPayload` at your CDN/CMS endpoint (`api/v1/catalog.json`).
2. Update `RetrofitCatalogClient.DEFAULT_BASE_URL` to your production HTTPS endpoint.
3. Users can tap the Cloud Sync button in the **Kits** screen to fetch new clubs and seasons dynamically.
