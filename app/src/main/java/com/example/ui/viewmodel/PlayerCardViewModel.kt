package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.DlsPlayerCardSpec
import com.example.data.model.PlayerCardTier
import com.example.data.model.PlayerCountry
import com.example.data.model.PlayerPosition
import com.example.data.model.PlayerPositionGroup
import com.example.data.model.PreferredFoot
import com.example.data.remote.DlsPlayerDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class PlayerCardTab(val label: String) {
    DATABASE("100 Players"),
    IDENTITY("Card & Photo"),
    STATS("8 Stats")
}

data class PlayerCardUiState(
    val allPlayers: List<DlsPlayerCardSpec> = DlsPlayerDatabase.players,
    val searchQuery: String = "",
    val positionGroup: PlayerPositionGroup = PlayerPositionGroup.ALL,
    val activeCard: DlsPlayerCardSpec = DlsPlayerDatabase.players.first(),
    val activeTab: PlayerCardTab = PlayerCardTab.DATABASE,
    val shieldOnlyMode: Boolean = false
) {
    val filteredPlayers: List<DlsPlayerCardSpec>
        get() {
            val q = searchQuery.trim()
            return allPlayers.filter { p ->
                val matchesPos = positionGroup == PlayerPositionGroup.ALL || p.position.group == positionGroup
                val matchesQuery = q.isBlank() ||
                    p.fullName.contains(q, ignoreCase = true) ||
                    p.cardShortName.contains(q, ignoreCase = true) ||
                    p.country.displayName.contains(q, ignoreCase = true) ||
                    p.country.code.contains(q, ignoreCase = true) ||
                    p.position.code.equals(q, ignoreCase = true)
                matchesPos && matchesQuery
            }
        }
}

class PlayerCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerCardUiState())
    val uiState: StateFlow<PlayerCardUiState> = _uiState.asStateFlow()

    fun setActiveTab(tab: PlayerCardTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setPositionGroup(group: PlayerPositionGroup) {
        _uiState.update { it.copy(positionGroup = group) }
    }

    fun setShieldOnlyMode(shieldOnly: Boolean) {
        _uiState.update { it.copy(shieldOnlyMode = shieldOnly) }
    }

    fun selectPlayer(player: DlsPlayerCardSpec, openEditor: Boolean = false) {
        _uiState.update {
            it.copy(
                activeCard = player,
                activeTab = if (openEditor) PlayerCardTab.IDENTITY else it.activeTab
            )
        }
    }

    fun createNewCustomPlayer() {
        val custom = DlsPlayerCardSpec(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            fullName = "CUSTOM STAR",
            cardShortName = "STAR",
            rating = 88,
            ratingBoosted = true,
            position = PlayerPosition.CF,
            country = PlayerCountry.BRAZIL,
            tier = PlayerCardTier.LEGENDARY_GOLD,
            heightCm = 181,
            preferredFoot = PreferredFoot.RIGHT,
            spe = 92,
            acc = 91,
            sta = 86,
            str = 80,
            con = 90,
            pas = 85,
            sho = 92,
            tac = 55,
            clubAccentHex = "#00E5FF"
        )
        _uiState.update {
            it.copy(
                activeCard = custom,
                activeTab = PlayerCardTab.IDENTITY
            )
        }
    }

    fun updateActiveCard(transform: (DlsPlayerCardSpec) -> DlsPlayerCardSpec) {
        _uiState.update { state ->
            state.copy(activeCard = transform(state.activeCard))
        }
    }

    fun setFullName(name: String) = updateActiveCard { card ->
        val cleaned = name.take(28)
        val autoShort = cleaned.trim().substringAfterLast(' ').ifBlank { cleaned.trim() }.take(16)
        card.copy(
            fullName = cleaned,
            cardShortName = if (card.cardShortName.equals(card.fullName.trim().substringAfterLast(' '), true)) {
                autoShort
            } else {
                card.cardShortName
            }
        )
    }

    fun setCardShortName(shortName: String) = updateActiveCard {
        it.copy(cardShortName = shortName.take(18))
    }

    fun setRating(rating: Int) = updateActiveCard {
        it.copy(rating = rating.coerceIn(40, 100))
    }

    fun setRatingBoosted(boosted: Boolean) = updateActiveCard {
        it.copy(ratingBoosted = boosted)
    }

    fun setPosition(pos: PlayerPosition) = updateActiveCard {
        it.copy(position = pos)
    }

    fun setCountry(country: PlayerCountry) = updateActiveCard {
        it.copy(country = country)
    }

    fun setTier(tier: PlayerCardTier) = updateActiveCard {
        it.copy(tier = tier)
    }

    fun setHeightCm(height: Int) = updateActiveCard {
        it.copy(heightCm = height.coerceIn(155, 210))
    }

    fun setPreferredFoot(foot: PreferredFoot) = updateActiveCard {
        it.copy(preferredFoot = foot)
    }

    fun setStaminaPercent(percent: Float) = updateActiveCard {
        it.copy(staminaPercent = percent.coerceIn(0.2f, 1f))
    }

    fun setClubAccentHex(hex: String) = updateActiveCard {
        it.copy(clubAccentHex = hex)
    }

    fun setCustomPhotoUri(uri: String?) = updateActiveCard {
        it.copy(
            customPhotoUri = uri,
            photoOffsetX = 0f,
            photoOffsetY = 0f,
            photoScale = 1f
        )
    }

    fun setPhotoOffsetX(x: Float) = updateActiveCard { it.copy(photoOffsetX = x.coerceIn(-1f, 1f)) }
    fun setPhotoOffsetY(y: Float) = updateActiveCard { it.copy(photoOffsetY = y.coerceIn(-1f, 1f)) }
    fun setPhotoScale(scale: Float) = updateActiveCard { it.copy(photoScale = scale.coerceIn(0.6f, 2.2f)) }

    fun setStat(code: String, value: Int) = updateActiveCard { card ->
        val v = value.coerceIn(1, 100)
        when (code) {
            "SPE" -> card.copy(spe = v)
            "ACC" -> card.copy(acc = v)
            "STA" -> card.copy(sta = v)
            "STR" -> card.copy(str = v)
            "CON" -> card.copy(con = v)
            "PAS" -> card.copy(pas = v)
            "SHO" -> card.copy(sho = v)
            "TAC" -> card.copy(tac = v)
            "GKH" -> card.copy(gkh = v)
            "GKR" -> card.copy(gkr = v)
            else -> card
        }
    }

    /**
     * One-tap DLS Maxed Card boost (+10 to rating & stats, caps at 100, enables boosted ring & Maxed Black tier).
     */
    fun applyMaxedBoost() = updateActiveCard { card ->
        card.copy(
            rating = (card.rating + 10).coerceAtMost(100),
            ratingBoosted = true,
            tier = PlayerCardTier.MAXED_BLACK,
            spe = (card.spe + 10).coerceAtMost(100),
            acc = (card.acc + 10).coerceAtMost(100),
            sta = (card.sta + 10).coerceAtMost(100),
            str = (card.str + 10).coerceAtMost(100),
            con = (card.con + 10).coerceAtMost(100),
            pas = (card.pas + 10).coerceAtMost(100),
            sho = (card.sho + 10).coerceAtMost(100),
            tac = (card.tac + 10).coerceAtMost(100),
            gkh = (card.gkh + 10).coerceAtMost(100),
            gkr = (card.gkr + 10).coerceAtMost(100)
        )
    }

    fun resetActiveToOriginalDatabase() {
        val id = _uiState.value.activeCard.id
        val original = DlsPlayerDatabase.players.firstOrNull { it.id == id } ?: return
        _uiState.update { it.copy(activeCard = original) }
    }

    /**
     * Persists edits of the currently open player back into the in-app searchable database list
     * (or prepends if it is a newly created custom player).
     */
    fun saveActiveCardToDatabase(): String {
        val current = _uiState.value.activeCard
        val existingIdx = _uiState.value.allPlayers.indexOfFirst { it.id == current.id }
        val updatedList = if (existingIdx >= 0) {
            _uiState.value.allPlayers.toMutableList().apply { set(existingIdx, current) }
        } else {
            listOf(current) + _uiState.value.allPlayers
        }
        _uiState.update { it.copy(allPlayers = updatedList) }
        return if (existingIdx >= 0) {
            "Saved changes to ${current.fullName}"
        } else {
            "Added ${current.fullName} to Player Database"
        }
    }
}
