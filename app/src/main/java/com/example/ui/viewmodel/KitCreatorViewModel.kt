package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesDataStore
import com.example.data.model.ClubKitBundle
import com.example.data.model.CollarStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.JerseyTemplateFit
import com.example.data.model.JerseyViewAngle
import com.example.data.model.KitSlotType
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.SleeveStyle
import com.example.data.repository.DesignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class KitEditorTab(val label: String) {
    BASE("Base & Fit"),
    COLORS("Colors"),
    PATTERN("Pattern"),
    DETAILS("Trim & Panels"),
    BRANDING("Brand & Back"),
    EXPORT("Save & Export")
}

data class KitCreatorUiState(
    val editingId: Long? = null,
    val designName: String = "Nova Home 25/26",
    val selectedClubLeague: String = "Custom Club",
    val season: String = "2025/26",
    val isCustomClubMode: Boolean = true,
    val spec: JerseyDesignSpec = JerseyDesignSpec(),
    val isBackView: Boolean = false,
    val rotationAngleDeg: Float = 0f,
    val showUvPreview: Boolean = false,
    val showShortsPreview: Boolean = true,
    val zoomLevel: Float = 1f,
    val activeTab: KitEditorTab = KitEditorTab.BASE,
    val undoStack: List<JerseyDesignSpec> = emptyList(),
    val redoStack: List<JerseyDesignSpec> = emptyList(),
    val statusMessage: String? = null
)

class KitCreatorViewModel(
    private val designRepo: DesignRepository,
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitCreatorUiState())
    val uiState: StateFlow<KitCreatorUiState> = _uiState.asStateFlow()

    private fun updateSpec(transform: (JerseyDesignSpec) -> JerseyDesignSpec) {
        _uiState.update { state ->
            val updated = transform(state.spec)
            state.copy(
                spec = updated,
                undoStack = (state.undoStack + state.spec).takeLast(25),
                redoStack = emptyList()
            )
        }
    }

    fun undo() {
        _uiState.update { state ->
            val prev = state.undoStack.lastOrNull() ?: return@update state
            state.copy(
                spec = prev,
                undoStack = state.undoStack.dropLast(1),
                redoStack = (state.redoStack + state.spec).takeLast(25)
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            val next = state.redoStack.lastOrNull() ?: return@update state
            state.copy(
                spec = next,
                undoStack = (state.undoStack + state.spec).takeLast(25),
                redoStack = state.redoStack.dropLast(1)
            )
        }
    }

    fun loadFromClub(club: ClubKitBundle, slotIndex: Int = 0) {
        val variant = club.variants.getOrElse(slotIndex) { club.variants.first() }
        _uiState.update {
            KitCreatorUiState(
                editingId = null,
                designName = "${club.clubName} ${variant.slotType.shortLabel}",
                selectedClubLeague = club.league,
                season = club.season,
                isCustomClubMode = false,
                spec = variant.designSpec
            )
        }
    }

    fun loadFromSpec(spec: JerseyDesignSpec, name: String, existingId: Long? = null, league: String = "Custom Club", season: String = "2025/26") {
        _uiState.update {
            KitCreatorUiState(
                editingId = existingId,
                designName = name,
                selectedClubLeague = league,
                season = season,
                isCustomClubMode = league == "Custom Club",
                spec = spec
            )
        }
    }

    fun startFreshCustomClub(clubName: String) {
        _uiState.update {
            KitCreatorUiState(
                editingId = null,
                designName = "${clubName.ifBlank { "Custom FC" }} Home",
                selectedClubLeague = "Custom Club",
                season = "2025/26",
                isCustomClubMode = true,
                spec = JerseyDesignSpec(clubName = clubName.ifBlank { "Custom FC" })
            )
        }
    }

    fun setActiveTab(tab: KitEditorTab) = _uiState.update { it.copy(activeTab = tab) }
    fun toggleBackView(isBack: Boolean) = _uiState.update {
        it.copy(
            isBackView = isBack,
            rotationAngleDeg = if (isBack) 180f else 0f,
            showUvPreview = false
        )
    }

    fun setRotationAngle(deg: Float) = _uiState.update {
        val norm = ((deg % 360f) + 360f) % 360f
        it.copy(
            rotationAngleDeg = norm,
            isBackView = norm in 135f..225f,
            showUvPreview = false
        )
    }

    fun rotateBy(deltaDeg: Float) = _uiState.update {
        val norm = (((it.rotationAngleDeg + deltaDeg) % 360f) + 360f) % 360f
        it.copy(
            rotationAngleDeg = norm,
            isBackView = norm in 135f..225f,
            showUvPreview = false
        )
    }

    fun snapToViewAngle(angle: JerseyViewAngle) = _uiState.update {
        it.copy(
            rotationAngleDeg = angle.degrees,
            isBackView = angle == JerseyViewAngle.BACK,
            showUvPreview = false
        )
    }

    fun toggleUvPreview(showUv: Boolean) = _uiState.update { it.copy(showUvPreview = showUv) }
    fun toggleShortsPreview(show: Boolean) = _uiState.update { it.copy(showShortsPreview = show) }
    fun setZoom(zoom: Float) = _uiState.update { it.copy(zoomLevel = zoom.coerceIn(0.8f, 1.8f)) }
    fun setDesignName(name: String) = _uiState.update { it.copy(designName = name) }

    fun setClubName(name: String) = updateSpec { it.copy(clubName = name) }
    fun setSlotType(slot: KitSlotType) = updateSpec { it.copy(slotType = slot) }
    fun setTemplateFit(fit: JerseyTemplateFit) = updateSpec { it.copy(templateFit = fit) }
    fun setPrimaryColor(hex: String) = updateSpec { it.copy(primaryColorHex = hex) }
    fun setSecondaryColor(hex: String) = updateSpec { it.copy(secondaryColorHex = hex) }
    fun setThirdColor(hex: String) = updateSpec { it.copy(thirdColorHex = hex) }
    fun setUseGradient(use: Boolean) = updateSpec { it.copy(useGradient = use) }
    fun setGradientEnd(hex: String) = updateSpec { it.copy(gradientEndHex = hex) }
    fun setPattern(pattern: JerseyPattern) = updateSpec { it.copy(pattern = pattern) }
    fun setCollarStyle(collar: CollarStyle) = updateSpec { it.copy(collarStyle = collar) }
    fun setCollarColor(hex: String) = updateSpec { it.copy(collarColorHex = hex) }
    fun setSleeveStyle(style: SleeveStyle) = updateSpec { it.copy(sleeveStyle = style) }
    fun setSleeveColor(hex: String) = updateSpec { it.copy(sleeveColorHex = hex) }
    fun setCuffColor(hex: String) = updateSpec { it.copy(cuffColorHex = hex) }
    fun setShoulderPattern(p: ShoulderPattern) = updateSpec { it.copy(shoulderPattern = p) }
    fun setSidePattern(p: SidePattern) = updateSpec { it.copy(sidePattern = p) }
    fun setShortsColor(hex: String) = _uiState.update { state ->
        val updated = state.spec.copy(shortsColorHex = hex)
        state.copy(
            spec = updated,
            showShortsPreview = if (!state.showUvPreview) true else state.showShortsPreview,
            undoStack = (state.undoStack + state.spec).takeLast(25),
            redoStack = emptyList()
        )
    }
    fun setSocksColor(hex: String) = _uiState.update { state ->
        val updated = state.spec.copy(socksColorHex = hex)
        state.copy(
            spec = updated,
            showShortsPreview = if (!state.showUvPreview) true else state.showShortsPreview,
            undoStack = (state.undoStack + state.spec).takeLast(25),
            redoStack = emptyList()
        )
    }
    fun setShortsTrimColor(hex: String?) = updateSpec { it.copy(shortsTrimColorHex = hex) }
    fun setSocksTrimColor(hex: String?) = updateSpec { it.copy(socksTrimColorHex = hex) }
    fun applyShortsAndSocksPreset(shortsHex: String, socksHex: String) = _uiState.update { state ->
        val updated = state.spec.copy(shortsColorHex = shortsHex, socksColorHex = socksHex)
        state.copy(
            spec = updated,
            showShortsPreview = if (!state.showUvPreview) true else state.showShortsPreview,
            undoStack = (state.undoStack + state.spec).takeLast(25),
            redoStack = emptyList()
        )
    }
    fun setCrestPlacement(p: CrestPlacement) = updateSpec { it.copy(crestPlacement = p) }
    fun setCrestStyleId(id: String?) = updateSpec { it.copy(crestStyleId = id) }
    fun setCrestPrimaryColor(hex: String?) = updateSpec { it.copy(crestPrimaryColorHex = hex) }
    fun setCrestSecondaryColor(hex: String?) = updateSpec { it.copy(crestSecondaryColorHex = hex) }
    fun setCrestAccentColor(hex: String?) = updateSpec { it.copy(crestAccentColorHex = hex) }
    fun resetCrestColorsToKit() = updateSpec {
        it.copy(
            crestPrimaryColorHex = null,
            crestSecondaryColorHex = null,
            crestAccentColorHex = null
        )
    }
    fun setManufacturer(m: ManufacturerMark) = updateSpec { it.copy(manufacturer = m) }
    fun setManufacturerColor(hex: String?) = updateSpec { it.copy(manufacturerColorHex = hex) }
    fun setSponsorText(text: String) = updateSpec { it.copy(sponsorText = text) }
    fun setSponsorColor(hex: String?) = updateSpec { it.copy(sponsorColorHex = hex) }
    fun setCustomText(text: String) = updateSpec { it.copy(customText = text) }
    fun setPlayerName(name: String) = updateSpec { it.copy(playerName = name) }
    fun setPlayerNumber(num: String) = updateSpec { it.copy(playerNumber = num.filter { ch -> ch.isDigit() }.take(2)) }
    fun setPlayerNumberColor(hex: String?) = updateSpec { it.copy(playerNumberColorHex = hex) }
    fun setShortsNumberColor(hex: String?) = updateSpec { it.copy(shortsNumberColorHex = hex) }
    fun setCleanBackNumberBox(enabled: Boolean) = updateSpec { it.copy(cleanBackNumberBox = enabled) }
    fun setCustomClubLogoUri(uri: String?) = updateSpec { it.copy(customClubLogoUri = uri) }
    fun setCustomSponsorLogoUri(uri: String?) = updateSpec { it.copy(customSponsorLogoUri = uri) }

    fun saveCurrentDesign(asCopy: Boolean = false, onSaved: (String) -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            val idToUse = if (asCopy) null else state.editingId
            val finalName = if (asCopy) "${state.designName} Copy" else state.designName
            val savedId = designRepo.saveKit(
                existingId = idToUse,
                name = finalName,
                clubName = state.spec.clubName,
                league = state.selectedClubLeague,
                season = state.season,
                spec = state.spec
            )
            _uiState.update { it.copy(editingId = savedId, designName = finalName) }
            onSaved("Saved \"$finalName\" to My Kits")
        }
    }

    fun recordExport() {
        viewModelScope.launch { prefsStore.incrementExportCount() }
    }

    class Factory(
        private val designRepo: DesignRepository,
        private val prefsStore: UserPreferencesDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KitCreatorViewModel(designRepo, prefsStore) as T
        }
    }
}
