package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

enum class KitCategory(val displayName: String, val group: String) {
    PREMIER_LEAGUE("Premier League", "Club Kits"),
    LA_LIGA("La Liga", "Club Kits"),
    SERIE_A("Serie A", "Club Kits"),
    BUNDESLIGA("Bundesliga", "Club Kits"),
    LIGUE_1("Ligue 1", "Club Kits"),
    OTHER_LEAGUES("Other Leagues", "Club Kits"),
    NATIONAL_TEAMS("National Teams", "National"),
    CLASSIC("Classic Kits", "Special"),
    RETRO("Retro Kits", "Special"),
    FANTASY("Fantasy Kits", "Special"),
    CUSTOM("Custom Kits", "Special")
}

enum class KitSlotType(val label: String, val shortLabel: String) {
    HOME("Home Jersey", "HOME"),
    AWAY("Away Jersey", "AWAY"),
    THIRD("Third Jersey", "THIRD"),
    GOALKEEPER("Goalkeeper Jersey", "GK")
}

enum class JerseyPattern(val label: String, val isGoalkeeper: Boolean = false) {
    SOLID("Solid"),
    STRIPES("Center Band"),
    VERTICAL_STRIPES("Vertical Stripes"),
    HORIZONTAL_STRIPES("Slim Hoops"),
    HOOPS("Classic Hoops"),
    CHECKS("Checkered"),
    GEOMETRIC("Geometric Grid"),
    ZIGZAG("Thunder Zigzag"),
    ABSTRACT_MODERN("Modern Sash"),
    PINSTRIPES("Twin Pinstripes"),
    HALF_AND_HALF("Half & Half"),
    QUARTERS("Quartered"),
    CHEST_SASH("Diagonal Sash"),
    REVERSE_SASH("Reverse Sash"),
    CHEST_BAND("Chest Band"),
    CHEVRON_V("Bordeaux V"),
    DOUBLE_CHEVRON("Double Chevron"),
    GRADIENT_HOOPS("Fade Hoops"),
    CROSS_ST_GEORGE("Templar Cross"),
    DIAMOND_ARGYLE("Argyle Weave"),
    LIGHTNING_BOLT("Electric Bolts"),
    BRUSH_SLASH("Painted Slash"),
    WAVE_RIPPLE("Stadium Wave"),
    HEX_HONEYCOMB("Aero Hex Mesh"),
    GK_SPLATTER("GK Paint Splatter", isGoalkeeper = true),
    GK_SHATTER("GK Prism Shatter", isGoalkeeper = true),
    GK_CAMO_GRID("GK Digi Camo", isGoalkeeper = true),
    GK_SONIC_BURST("GK Sonic Burst", isGoalkeeper = true)
}

enum class SponsorStyle(val label: String) {
    CLASSIC_BOLD("Classic Bold"),
    AERO_WINGS("Aero Wings"),
    TECH_PILL("Tech Capsule"),
    STAR_AIRLINES("Star Airlines"),
    CROWN_BANK("Crown Finance"),
    BOLT_ENERGY("Bolt Energy")
}

enum class SleevePatchStyle(val label: String) {
    NONE("None"),
    UCL_STARBALL("Champions Starball"),
    PREMIER_LION("League Crown"),
    WORLD_GLOBE("World Cup"),
    SUPER_CUP("Super Cup")
}

enum class ChestBadgeStyle(val label: String) {
    NONE("None"),
    WORLD_CHAMPION_GOLD("World Champ Gold"),
    CLUB_WORLD_SHIELD("Club World"),
    CONTINENTAL_TROPHY("Champions Cup")
}

enum class CollarStyle(val label: String) {
    CREW_NECK("Crew Neck"),
    V_NECK("V-Neck"),
    POLO_COLLAR("Polo Collar"),
    MANDARIN("Mandarin"),
    RIBBED_TRIM("Ribbed Trim")
}

enum class SleeveStyle(val label: String) {
    SET_IN("Set-In Short"),
    RAGLAN("Raglan Pro"),
    LONG_SLEEVE("Long Sleeve"),
    CONTRAST_CUFF("Contrast Cuff")
}

enum class ShoulderPattern(val label: String) {
    NONE("Clean"),
    THREE_STRIPES("3-Stripes"),
    CHEVRON_BAND("Chevron Band"),
    PIPING("Contrast Piping")
}

enum class SidePattern(val label: String) {
    NONE("Clean"),
    MESH_STRIPE("Mesh Stripe"),
    VENT_PANEL("Vent Panel"),
    DIAGONAL_TRIM("Diagonal Trim")
}

enum class CrestPlacement(val label: String) {
    LEFT_CHEST("Left Chest"),
    CENTER_CHEST("Center Chest"),
    RIGHT_CHEST("Right Chest")
}

enum class JerseyTemplateFit(val label: String) {
    PRO_AERO("Pro Aero Fit"),
    CLASSIC_STADIUM("Classic Stadium"),
    RETRO_LOOSE("Retro 90s Cut"),
    ELITE_RAGLAN("Elite Raglan")
}

enum class ManufacturerMark(val label: String) {
    STRIKER_SWOOSH("Apex Arc"),
    TRIPLE_PEAK("Triple Peak"),
    LEAP_CAT("Pounce Pro"),
    DIAMOND_CREST("Crown Diamond"),
    NONE("Minimal (None)")
}

enum class JerseyViewAngle(val label: String, val degrees: Float) {
    FRONT("Front", 0f),
    RIGHT_SIDE("Right", 90f),
    BACK("Back", 180f),
    LEFT_SIDE("Left", 270f)
}

@JsonClass(generateAdapter = true)
data class JerseyDesignSpec(
    val clubName: String = "FC Nova United",
    val slotType: KitSlotType = KitSlotType.HOME,
    val primaryColorHex: String = "#0A2463",
    val secondaryColorHex: String = "#00E5FF",
    val thirdColorHex: String = "#FFFFFF",
    val useGradient: Boolean = false,
    val gradientEndHex: String = "#05102E",
    val pattern: JerseyPattern = JerseyPattern.VERTICAL_STRIPES,
    val collarStyle: CollarStyle = CollarStyle.CREW_NECK,
    val collarColorHex: String = "#FFFFFF",
    val sleeveStyle: SleeveStyle = SleeveStyle.SET_IN,
    val sleeveColorHex: String = "#0A2463",
    val cuffColorHex: String = "#00E5FF",
    val shoulderPattern: ShoulderPattern = ShoulderPattern.THREE_STRIPES,
    val sidePattern: SidePattern = SidePattern.MESH_STRIPE,
    val crestPlacement: CrestPlacement = CrestPlacement.LEFT_CHEST,
    val manufacturer: ManufacturerMark = ManufacturerMark.STRIKER_SWOOSH,
    val sponsorText: String = "APEX ENERGY",
    val sponsorStyle: SponsorStyle = SponsorStyle.CLASSIC_BOLD,
    val customText: String = "",
    val playerName: String = "SILVA",
    val playerNumber: String = "10",
    val templateFit: JerseyTemplateFit = JerseyTemplateFit.PRO_AERO,
    val customClubLogoUri: String? = null,
    val customSponsorLogoUri: String? = null,
    val customSponsorOffsetX: Float = 0f,
    val customSponsorOffsetY: Float = 0f,
    val customSponsorScale: Float = 1f,
    val customStickerUri: String? = null,
    val customStickerOffsetX: Float = 0f,
    val customStickerOffsetY: Float = 0.22f,
    val customStickerScale: Float = 1f,
    val sleevePatch: SleevePatchStyle = SleevePatchStyle.NONE,
    val chestBadge: ChestBadgeStyle = ChestBadgeStyle.NONE,
    val captainArmbandEnabled: Boolean = false,
    val captainArmbandColorHex: String = "#FFDF00",
    val gkGloveColorHex: String? = null,
    val shortsColorHex: String = "#0A2463",
    val socksColorHex: String = "#0A2463",
    val shortsTrimColorHex: String? = null,
    val socksTrimColorHex: String? = null,
    val crestStyleId: String? = null,
    val crestPrimaryColorHex: String? = null,
    val crestSecondaryColorHex: String? = null,
    val crestAccentColorHex: String? = null,
    val manufacturerColorHex: String? = null,
    val sponsorColorHex: String? = null,
    val playerNumberColorHex: String? = null,
    val shortsNumberColorHex: String? = null,
    val cleanBackNumberBox: Boolean = false
)

data class KitVariant(
    val slotType: KitSlotType,
    val label: String,
    val kitUrl: String,
    val importCode: String,
    val designSpec: JerseyDesignSpec
)

data class ClubKitBundle(
    val id: String,
    val clubName: String,
    val shortCode: String,
    val country: String,
    val league: String,
    val category: KitCategory,
    val season: String,
    val info: String,
    val downloads: Int,
    val popularityRank: Int,
    val isTrending: Boolean,
    val isRecentlyAdded: Boolean,
    val crestPrimaryHex: String,
    val crestSecondaryHex: String,
    val crestEmblem: LogoElementType,
    val variants: List<KitVariant>
)

data class CustomizationTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val designSpec: JerseyDesignSpec
)

enum class LogoShape(val label: String) {
    SHIELD("Shield"),
    CIRCLE("Circle"),
    OVAL("Oval"),
    DIAMOND("Diamond"),
    MODERN_BADGE("Modern Badge"),
    CLASSIC_CREST("Classic Crest")
}

enum class LogoElementType(val label: String) {
    STAR("Star"),
    CROWN("Crown"),
    FOOTBALL("Football"),
    EAGLE("Eagle"),
    LION("Lion"),
    TIGER("Tiger"),
    WINGS("Wings"),
    STRIPES("Stripes"),
    SHIELD_INNER("Inner Shield"),
    LEAVES("Laurel Leaves"),
    GEOMETRIC("Geometric Hex")
}

enum class LogoBorderStyle(val label: String) {
    SOLID("Solid Ring"),
    DOUBLE("Double Rim"),
    GOLD_TRIM("Championship Trim"),
    NONE("Frameless")
}

enum class LogoFontStyle(val label: String) {
    ATHLETIC_CONDENSED("Athletic"),
    MODERN_SANS("Modern Sans"),
    CLASSIC_SERIF("Classic Heritage")
}

@JsonClass(generateAdapter = true)
data class LogoLayer(
    val id: String,
    val type: LogoElementType,
    val colorHex: String = "#FFC400",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1f
)

@JsonClass(generateAdapter = true)
data class LogoDesignSpec(
    val clubName: String = "NOVA UNITED",
    val initials: String = "NUFC",
    val establishedYear: String = "2025",
    val shape: LogoShape = LogoShape.SHIELD,
    val bgColorHex: String = "#0B1D48",
    val secondaryColorHex: String = "#00E5FF",
    val useGradient: Boolean = true,
    val borderColorHex: String = "#FFC400",
    val borderStyle: LogoBorderStyle = LogoBorderStyle.DOUBLE,
    val textColorHex: String = "#FFFFFF",
    val fontStyle: LogoFontStyle = LogoFontStyle.ATHLETIC_CONDENSED,
    val textOffsetY: Float = 0.24f,
    val layers: List<LogoLayer> = emptyList()
)

data class LogoTemplatePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val spec: LogoDesignSpec
)

enum class SortOption(val label: String) {
    MOST_POPULAR("Most Popular"),
    RECENTLY_ADDED("Recently Added"),
    ALPHABETICAL("Alphabetical"),
    MOST_DOWNLOADED("Most Downloaded")
}

enum class MainNavTab(val route: String, val titleKey: String) {
    HOME("home", "Home"),
    KITS("kits", "Kits"),
    CREATE("create", "Create"),
    LOGOS("logos", "Logos"),
    PROFILE("profile", "Profile")
}

fun parseColorHex(hex: String, fallback: Color = Color(0xFF0A2463)): Color {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        when (cleaned.length) {
            6 -> Color(android.graphics.Color.parseColor("#$cleaned"))
            8 -> Color(android.graphics.Color.parseColor("#$cleaned"))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

fun Color.toHexString(): String {
    val a = kotlin.math.round(alpha * 255f).toInt().coerceIn(0, 255)
    val r = kotlin.math.round(red * 255f).toInt().coerceIn(0, 255)
    val g = kotlin.math.round(green * 255f).toInt().coerceIn(0, 255)
    val b = kotlin.math.round(blue * 255f).toInt().coerceIn(0, 255)
    return if (a < 255) {
        String.format(java.util.Locale.US, "#%02X%02X%02X%02X", a, r, g, b)
    } else {
        String.format(java.util.Locale.US, "#%02X%02X%02X", r, g, b)
    }
}

fun Color.toAndroidColorInt(): Int {
    val a = kotlin.math.round(alpha * 255f).toInt().coerceIn(0, 255)
    val r = kotlin.math.round(red * 255f).toInt().coerceIn(0, 255)
    val g = kotlin.math.round(green * 255f).toInt().coerceIn(0, 255)
    val b = kotlin.math.round(blue * 255f).toInt().coerceIn(0, 255)
    return android.graphics.Color.argb(a, r, g, b)
}

/**
 * Exact sRGB -> HSL conversion matching DLS 26's COLOUR (Hue 0..360),
 * SATURATION (0..100%), and LIGHTNESS (0..100%) sliders.
 */
fun hexToHsl(hex: String): Triple<Float, Float, Float> {
    val colorInt = parseColorHex(hex).toAndroidColorInt()
    val r = android.graphics.Color.red(colorInt) / 255f
    val g = android.graphics.Color.green(colorInt) / 255f
    val b = android.graphics.Color.blue(colorInt) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val l = (max + min) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * l - 1f))
    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    return Triple(h.coerceIn(0f, 360f), s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

/**
 * Exact HSL -> sRGB Hex conversion matching DLS 26's color picker.
 */
fun hslToHex(hueDeg: Float, saturation: Float, lightness: Float): String {
    val h = ((hueDeg % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val r = kotlin.math.round((r1 + m) * 255f).toInt().coerceIn(0, 255)
    val g = kotlin.math.round((g1 + m) * 255f).toInt().coerceIn(0, 255)
    val b = kotlin.math.round((b1 + m) * 255f).toInt().coerceIn(0, 255)
    return String.format(java.util.Locale.US, "#%02X%02X%02X", r, g, b)
}
