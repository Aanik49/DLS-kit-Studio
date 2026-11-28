package com.example.data.model

import androidx.compose.runtime.Immutable

enum class PlayerCardTier(
    val label: String,
    val primaryHex: String,
    val secondaryHex: String,
    val borderHex: String,
    val textColorHex: String
) {
    LEGENDARY_GOLD(
        label = "Legendary",
        primaryHex = "#FFD83B",
        secondaryHex = "#F5A623",
        borderHex = "#FFF1A8",
        textColorHex = "#10141A"
    ),
    MAXED_BLACK(
        label = "Maxed Black",
        primaryHex = "#1B1F27",
        secondaryHex = "#0B0E14",
        borderHex = "#F7C948",
        textColorHex = "#FFFFFF"
    ),
    RARE_BLUE(
        label = "Rare Blue",
        primaryHex = "#2484F5",
        secondaryHex = "#0E4FA8",
        borderHex = "#9AD0FF",
        textColorHex = "#FFFFFF"
    ),
    COMMON_SILVER(
        label = "Common",
        primaryHex = "#DCE3EC",
        secondaryHex = "#9AA8B8",
        borderHex = "#FFFFFF",
        textColorHex = "#12171F"
    ),
    SPECIAL_ICON(
        label = "Special Icon",
        primaryHex = "#8E2DE2",
        secondaryHex = "#4A00E0",
        borderHex = "#00F5D4",
        textColorHex = "#FFFFFF"
    )
}

enum class PlayerPositionGroup(val label: String) {
    ALL("All"),
    FWD("FWD"),
    MID("MID"),
    DEF("DEF"),
    GK("GK")
}

enum class PlayerPosition(
    val code: String,
    val displayName: String,
    val group: PlayerPositionGroup,
    val badgeColorHex: String
) {
    CF("CF", "Center Forward", PlayerPositionGroup.FWD, "#D94A64"),
    LW("LW", "Left Winger", PlayerPositionGroup.FWD, "#D94A64"),
    RW("RW", "Right Winger", PlayerPositionGroup.FWD, "#D94A64"),
    SS("SS", "Second Striker", PlayerPositionGroup.FWD, "#D94A64"),
    AM("AM", "Attacking Mid", PlayerPositionGroup.MID, "#22A954"),
    CM("CM", "Central Mid", PlayerPositionGroup.MID, "#22A954"),
    LM("LM", "Left Mid", PlayerPositionGroup.MID, "#22A954"),
    RM("RM", "Right Mid", PlayerPositionGroup.MID, "#22A954"),
    DM("DM", "Defensive Mid", PlayerPositionGroup.MID, "#22A954"),
    CB("CB", "Center Back", PlayerPositionGroup.DEF, "#2B74E4"),
    LB("LB", "Left Back", PlayerPositionGroup.DEF, "#2B74E4"),
    RB("RB", "Right Back", PlayerPositionGroup.DEF, "#2B74E4"),
    GK("GK", "Goalkeeper", PlayerPositionGroup.GK, "#E08B1A");

    val isGoalkeeper: Boolean get() = this == GK
}

enum class PreferredFoot(val label: String) {
    RIGHT("Right"),
    LEFT("Left"),
    BOTH("Both")
}

enum class FlagLayout {
    VERTICAL_TRICOLOR,
    HORIZONTAL_TRICOLOR,
    HORIZONTAL_BICOLOR,
    NORDIC_CROSS,
    UNION_JACK,
    BRAZIL,
    ARGENTINA,
    SPAIN,
    PORTUGAL,
    JAPAN,
    SOUTH_KOREA,
    USA,
    MOROCCO,
    CROATIA,
    SWITZERLAND,
    TURKEY,
    CANADA,
    SCOTLAND,
    SENEGAL,
    GHANA,
    CAMEROON,
    GEORGIA
}

enum class PlayerCountry(
    val code: String,
    val displayName: String,
    val layout: FlagLayout,
    val c1: String,
    val c2: String,
    val c3: String = "#FFFFFF"
) {
    FRANCE("FRA", "France", FlagLayout.VERTICAL_TRICOLOR, "#002395", "#FFFFFF", "#ED2939"),
    ARGENTINA("ARG", "Argentina", FlagLayout.ARGENTINA, "#74ACDF", "#FFFFFF", "#F6B40E"),
    BRAZIL("BRA", "Brazil", FlagLayout.BRAZIL, "#009C3B", "#FFDF00", "#002776"),
    ENGLAND("ENG", "England", FlagLayout.UNION_JACK, "#FFFFFF", "#CE1124", "#012169"),
    SPAIN("ESP", "Spain", FlagLayout.SPAIN, "#AA151B", "#F1BF00", "#AA151B"),
    PORTUGAL("POR", "Portugal", FlagLayout.PORTUGAL, "#006600", "#FF0000", "#FFCC00"),
    NORWAY("NOR", "Norway", FlagLayout.NORDIC_CROSS, "#BA0C2F", "#FFFFFF", "#00205B"),
    GERMANY("GER", "Germany", FlagLayout.HORIZONTAL_TRICOLOR, "#000000", "#DD0000", "#FFCE00"),
    NETHERLANDS("NED", "Netherlands", FlagLayout.HORIZONTAL_TRICOLOR, "#AE1C28", "#FFFFFF", "#21468B"),
    BELGIUM("BEL", "Belgium", FlagLayout.VERTICAL_TRICOLOR, "#000000", "#FAE042", "#ED2939"),
    URUGUAY("URU", "Uruguay", FlagLayout.ARGENTINA, "#0038A8", "#FFFFFF", "#FCD116"),
    COLOMBIA("COL", "Colombia", FlagLayout.HORIZONTAL_TRICOLOR, "#FCD116", "#003893", "#CE1126"),
    CROATIA("CRO", "Croatia", FlagLayout.CROATIA, "#FF0000", "#FFFFFF", "#171796"),
    ITALY("ITA", "Italy", FlagLayout.VERTICAL_TRICOLOR, "#009246", "#FFFFFF", "#CE2B37"),
    EGYPT("EGY", "Egypt", FlagLayout.HORIZONTAL_TRICOLOR, "#CE1126", "#FFFFFF", "#000000"),
    SOUTH_KOREA("KOR", "South Korea", FlagLayout.SOUTH_KOREA, "#FFFFFF", "#CD2E3A", "#0047A0"),
    JAPAN("JPN", "Japan", FlagLayout.JAPAN, "#FFFFFF", "#BC002D", "#FFFFFF"),
    MOROCCO("MAR", "Morocco", FlagLayout.MOROCCO, "#C1272D", "#006233", "#C1272D"),
    SENEGAL("SEN", "Senegal", FlagLayout.SENEGAL, "#00853F", "#FDEF42", "#E31B23"),
    NIGERIA("NGA", "Nigeria", FlagLayout.VERTICAL_TRICOLOR, "#008751", "#FFFFFF", "#008751"),
    USA("USA", "United States", FlagLayout.USA, "#B22234", "#FFFFFF", "#3C3B6E"),
    CANADA("CAN", "Canada", FlagLayout.CANADA, "#FF0000", "#FFFFFF", "#FF0000"),
    POLAND("POL", "Poland", FlagLayout.HORIZONTAL_BICOLOR, "#FFFFFF", "#DC143C", "#DC143C"),
    SWEDEN("SWE", "Sweden", FlagLayout.NORDIC_CROSS, "#006AA7", "#FECC00", "#FECC00"),
    SWITZERLAND("SUI", "Switzerland", FlagLayout.SWITZERLAND, "#D52B1E", "#FFFFFF", "#FFFFFF"),
    TURKEY("TUR", "Turkey", FlagLayout.TURKEY, "#E30A17", "#FFFFFF", "#FFFFFF"),
    SCOTLAND("SCO", "Scotland", FlagLayout.SCOTLAND, "#005EB8", "#FFFFFF", "#FFFFFF"),
    ECUADOR("ECU", "Ecuador", FlagLayout.HORIZONTAL_TRICOLOR, "#FFDD00", "#034EA2", "#ED1C24"),
    GEORGIA("GEO", "Georgia", FlagLayout.GEORGIA, "#FFFFFF", "#FF0000", "#FF0000"),
    SLOVENIA("SVN", "Slovenia", FlagLayout.HORIZONTAL_TRICOLOR, "#FFFFFF", "#005DA4", "#ED1C24"),
    GHANA("GHA", "Ghana", FlagLayout.GHANA, "#CF0921", "#FCD20F", "#006B3F"),
    CAMEROON("CMR", "Cameroon", FlagLayout.CAMEROON, "#007A5E", "#CE1126", "#FCD116"),
    AUSTRIA("AUT", "Austria", FlagLayout.HORIZONTAL_TRICOLOR, "#ED2939", "#FFFFFF", "#ED2939"),
    DENMARK("DEN", "Denmark", FlagLayout.NORDIC_CROSS, "#C8102E", "#FFFFFF", "#FFFFFF"),
    SERBIA("SRB", "Serbia", FlagLayout.HORIZONTAL_TRICOLOR, "#C6363C", "#0C4076", "#FFFFFF"),
    IVORY_COAST("CIV", "Ivory Coast", FlagLayout.VERTICAL_TRICOLOR, "#F77F00", "#FFFFFF", "#009E60"),
    UKRAINE("UKR", "Ukraine", FlagLayout.HORIZONTAL_BICOLOR, "#0057B7", "#FFD700", "#FFD700")
}

@Immutable
data class DlsPlayerCardSpec(
    val id: String,
    val fullName: String = "KYLIAN MBAPPÉ",
    val cardShortName: String = "MBAPPÉ",
    val rating: Int = 94,
    val ratingBoosted: Boolean = true,
    val position: PlayerPosition = PlayerPosition.CF,
    val country: PlayerCountry = PlayerCountry.FRANCE,
    val tier: PlayerCardTier = PlayerCardTier.LEGENDARY_GOLD,
    val heightCm: Int = 178,
    val preferredFoot: PreferredFoot = PreferredFoot.RIGHT,
    val spe: Int = 95,
    val acc: Int = 96,
    val sta: Int = 82,
    val str: Int = 78,
    val con: Int = 100,
    val pas: Int = 100,
    val sho: Int = 100,
    val tac: Int = 59,
    val gkh: Int = 88,
    val gkr: Int = 90,
    val staminaPercent: Float = 1f,
    val clubAccentHex: String = "#1D3C85",
    val customPhotoUri: String? = null,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val photoScale: Float = 1f
)

/**
 * Exact DLS 26 stat color grading matching in-game circular stat badges:
 *  90–100: Teal/Cyan (#4AD8C8)
 *  80–89:  Bright Green (#4CE24C)
 *  70–79:  Yellow (#F2DF50)
 *  60–69:  Orange (#F59E0B)
 *  < 60:   Coral Red (#EB5E5E)
 */
fun dlsStatColorHex(value: Int): String = when {
    value >= 90 -> "#4AD8C8"
    value >= 80 -> "#4CE24C"
    value >= 70 -> "#F2DF50"
    value >= 60 -> "#F59E0B"
    else -> "#EB5E5E"
}
