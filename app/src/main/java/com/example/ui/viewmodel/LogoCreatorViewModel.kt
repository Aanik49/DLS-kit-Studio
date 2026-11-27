package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesDataStore
import com.example.data.model.LogoBorderStyle
import com.example.data.model.LogoDesignSpec
import com.example.data.model.LogoElementType
import com.example.data.model.LogoFontStyle
import com.example.data.model.LogoLayer
import com.example.data.model.LogoShape
import com.example.data.model.LogoTemplatePreset
import com.example.data.remote.SampleKitCatalog
import com.example.data.repository.DesignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class LogoEditorTab(val label: String) {
    TEMPLATES("Templates"),
    SHAPE("Shape & Colors"),
    ELEMENTS("Elements"),
    LAYERS("Layers"),
    TEXT("Text"),
    EXPORT("Export")
}

data class LogoCreatorUiState(
    val editingId: Long? = null,
    val designName: String = "Nova United Crest",
    val spec: LogoDesignSpec = SampleKitCatalog.logoTemplates.first().spec,
    val selectedLayerId: String? = SampleKitCatalog.logoTemplates.first().spec.layers.firstOrNull()?.id,
    val activeTab: LogoEditorTab = LogoEditorTab.ELEMENTS,
    val showGuideGrid: Boolean = true,
    val undoStack: List<LogoDesignSpec> = emptyList(),
    val redoStack: List<LogoDesignSpec> = emptyList()
)

class LogoCreatorViewModel(
    private val designRepo: DesignRepository,
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoCreatorUiState())
    val uiState: StateFlow<LogoCreatorUiState> = _uiState.asStateFlow()

    private fun pushSpecUpdate(transform: (LogoDesignSpec) -> LogoDesignSpec) {
        _uiState.update { state ->
            val next = transform(state.spec)
            state.copy(
                spec = next,
                undoStack = (state.undoStack + state.spec).takeLast(30),
                redoStack = emptyList()
            )
        }
    }

    fun undo() {
        _uiState.update { state ->
            val prev = state.undoStack.lastOrNull() ?: return@update state
            state.copy(
                spec = prev,
                selectedLayerId = prev.layers.lastOrNull()?.id,
                undoStack = state.undoStack.dropLast(1),
                redoStack = (state.redoStack + state.spec).takeLast(30)
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            val next = state.redoStack.lastOrNull() ?: return@update state
            state.copy(
                spec = next,
                selectedLayerId = next.layers.lastOrNull()?.id,
                undoStack = (state.undoStack + state.spec).takeLast(30),
                redoStack = state.redoStack.dropLast(1)
            )
        }
    }

    fun applyTemplate(preset: LogoTemplatePreset) {
        pushSpecUpdate { preset.spec }
        _uiState.update {
            it.copy(
                designName = preset.title,
                selectedLayerId = preset.spec.layers.lastOrNull()?.id
            )
        }
    }

    fun loadSavedLogo(id: Long, name: String, spec: LogoDesignSpec) {
        _uiState.update {
            LogoCreatorUiState(
                editingId = id,
                designName = name,
                spec = spec,
                selectedLayerId = spec.layers.lastOrNull()?.id
            )
        }
    }

    fun setActiveTab(tab: LogoEditorTab) = _uiState.update { it.copy(activeTab = tab) }
    fun toggleGuideGrid(show: Boolean) = _uiState.update { it.copy(showGuideGrid = show) }
    fun selectLayer(id: String?) = _uiState.update { it.copy(selectedLayerId = id) }
    fun setDesignName(name: String) = _uiState.update { it.copy(designName = name) }

    fun setShape(shape: LogoShape) = pushSpecUpdate { it.copy(shape = shape) }
    fun setBgColor(hex: String) = pushSpecUpdate { it.copy(bgColorHex = hex) }
    fun setSecondaryColor(hex: String) = pushSpecUpdate { it.copy(secondaryColorHex = hex) }
    fun setUseGradient(use: Boolean) = pushSpecUpdate { it.copy(useGradient = use) }
    fun setBorderColor(hex: String) = pushSpecUpdate { it.copy(borderColorHex = hex) }
    fun setBorderStyle(style: LogoBorderStyle) = pushSpecUpdate { it.copy(borderStyle = style) }

    fun setClubName(name: String) = pushSpecUpdate { it.copy(clubName = name) }
    fun setInitials(initials: String) = pushSpecUpdate { it.copy(initials = initials) }
    fun setEstYear(year: String) = pushSpecUpdate { it.copy(establishedYear = year.take(4)) }
    fun setTextColor(hex: String) = pushSpecUpdate { it.copy(textColorHex = hex) }
    fun setFontStyle(style: LogoFontStyle) = pushSpecUpdate { it.copy(fontStyle = style) }
    fun setTextOffsetY(offsetY: Float) = pushSpecUpdate { it.copy(textOffsetY = offsetY.coerceIn(-0.35f, 0.36f)) }

    fun addElement(type: LogoElementType) {
        val newLayer = LogoLayer(
            id = UUID.randomUUID().toString().take(8),
            type = type,
            colorHex = "#FFC400",
            offsetX = 0f,
            offsetY = -0.05f,
            scale = 0.9f,
            rotationDeg = 0f,
            opacity = 1f
        )
        pushSpecUpdate { it.copy(layers = it.layers + newLayer) }
        _uiState.update { it.copy(selectedLayerId = newLayer.id) }
    }

    fun removeLayer(id: String) {
        pushSpecUpdate { spec ->
            val updated = spec.layers.filterNot { it.id == id }
            spec.copy(layers = updated)
        }
        _uiState.update { state ->
            state.copy(selectedLayerId = state.spec.layers.lastOrNull()?.id)
        }
    }

    fun updateLayerDrag(layerId: String, dx: Float, dy: Float) {
        _uiState.update { state ->
            val updatedLayers = state.spec.layers.map { l ->
                if (l.id == layerId) {
                    l.copy(
                        offsetX = (l.offsetX + dx).coerceIn(-0.42f, 0.42f),
                        offsetY = (l.offsetY + dy).coerceIn(-0.42f, 0.42f)
                    )
                } else l
            }
            state.copy(spec = state.spec.copy(layers = updatedLayers))
        }
    }

    fun updateSelectedLayer(transform: (LogoLayer) -> LogoLayer) {
        val targetId = _uiState.value.selectedLayerId ?: return
        _uiState.update { state ->
            val updated = state.spec.layers.map { l -> if (l.id == targetId) transform(l) else l }
            state.copy(spec = state.spec.copy(layers = updated))
        }
    }

    fun moveLayerOrder(id: String, forward: Boolean) {
        pushSpecUpdate { spec ->
            val list = spec.layers.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx == -1) return@pushSpecUpdate spec
            val targetIdx = if (forward) (idx + 1).coerceAtMost(list.lastIndex) else (idx - 1).coerceAtLeast(0)
            if (idx != targetIdx) {
                val item = list.removeAt(idx)
                list.add(targetIdx, item)
            }
            spec.copy(layers = list)
        }
    }

    fun saveCurrentLogo(asCopy: Boolean = false, onSaved: (String) -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            val idToUse = if (asCopy) null else state.editingId
            val finalName = if (asCopy) "${state.designName} Copy" else state.designName
            val savedId = designRepo.saveLogo(
                existingId = idToUse,
                name = finalName,
                spec = state.spec
            )
            _uiState.update { it.copy(editingId = savedId, designName = finalName) }
            onSaved("Saved \"$finalName\" to My Logos")
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
            return LogoCreatorViewModel(designRepo, prefsStore) as T
        }
    }
}
