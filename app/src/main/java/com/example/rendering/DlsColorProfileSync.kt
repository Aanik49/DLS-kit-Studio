package com.example.rendering

import android.graphics.Color
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.hexToHsl
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Target DLS 26 texture channel determining how the game's Unity renderer samples each color slot.
 */
enum class DlsColorChannel {
    /** 3D fabric mesh (shirt, sleeves, collar, shorts, socks, logos) lit by DLS 26's Linear shader. */
    FABRIC_SURFACE,
    /** Bottom-right circular primary kit color indicator in the 512x512 DLS 26 UV atlas. */
    PRIMARY_INDICATOR,
    /** Right-edge top/bottom squad number swatch squares rendered as crisp flat decals in DLS 26. */
    NUMBER_DECAL
}

/**
 * A single kit color slot mapped from user-selected sRGB into the official DLS 26 color profile:
 * both the calibrated 512x512 UV texture color and the official DLS 26 Edit Kit H/S/L sliders.
 */
data class DlsSyncedColorSlot(
    val slotName: String,
    val sourceHex: String,
    val sourceColorInt: Int,
    val dlsTextureColorInt: Int,
    val dlsTextureHex: String,
    val dlsHueDegrees: Int,
    val dlsSaturationPercent: Int,
    val dlsLightnessPercent: Int,
    val channel: DlsColorChannel
) {
    val dlsSliderLabel: String
        get() = "H ${dlsHueDegrees}° · S ${dlsSaturationPercent}% · L ${dlsLightnessPercent}%"
}

/**
 * Complete synced color profile for a full DLS 26 kit (jersey, shorts, socks, branding, and
 * official UV metadata swatches). Serves as the single source of truth for both the 512x512 UV
 * texture exporter and the Kit Creator's live DLS 26 Color Profile Sync UI.
 */
data class DlsOfficialKitColorProfile(
    val jerseyPrimary: DlsSyncedColorSlot,
    val jerseySecondary: DlsSyncedColorSlot,
    val jerseyThird: DlsSyncedColorSlot,
    val jerseyGradientEnd: DlsSyncedColorSlot,
    val collar: DlsSyncedColorSlot,
    val sleeve: DlsSyncedColorSlot,
    val cuff: DlsSyncedColorSlot,
    val shortsPrimary: DlsSyncedColorSlot,
    val shortsTrim: DlsSyncedColorSlot,
    val socksPrimary: DlsSyncedColorSlot,
    val socksTrim: DlsSyncedColorSlot,
    val jerseyNumber: DlsSyncedColorSlot,
    val shortsNumber: DlsSyncedColorSlot,
    val manufacturer: DlsSyncedColorSlot,
    val sponsor: DlsSyncedColorSlot,
    val sockBrand: DlsSyncedColorSlot,
    val shortsBrand: DlsSyncedColorSlot,
    val crestPrimary: DlsSyncedColorSlot,
    val crestSecondary: DlsSyncedColorSlot,
    val crestAccent: DlsSyncedColorSlot,
    val primaryIndicatorCircle: DlsSyncedColorSlot
) {
    /** Primary Jersey, Shorts, Socks, and Squad Number slots displayed in the Color Sync panel. */
    fun summarySlots(): List<DlsSyncedColorSlot> = listOf(
        jerseyPrimary,
        jerseySecondary,
        shortsPrimary,
        shortsTrim,
        socksPrimary,
        jerseyNumber,
        shortsNumber
    )

    fun formatShareableProfileText(): String = buildString {
        appendLine("DLS 26 Official Color Profile")
        summarySlots().forEach { slot ->
            appendLine("${slot.slotName}: ${slot.sourceHex} -> UV ${slot.dlsTextureHex} (${slot.dlsSliderLabel})")
        }
    }.trim()
}

/**
 * Official Dream League Soccer 26 Color Syncing Utility.
 *
 * Maps user-selected jersey, shorts, socks, and squad-number colors into the official DLS 26
 * color profile format so exported 512x512 UV textures render accurately under DLS 26's Unity
 * Linear-space lighting pipeline without grey-blue dark washout or coral/pastel desaturation.
 */
object DlsColorProfileSync {

    /**
     * Inverse gamma applied to fabric textures to cancel DLS 26's Unity Linear-space lift (~c^0.36).
     * Exact boundary invariants: #FFFFFF -> #FFFFFF and #000000 -> #000000.
     */
    const val DLS_FABRIC_INVERSE_GAMMA = 2.0

    fun mapColorToDlsTexture(
        colorInt: Int,
        channel: DlsColorChannel = DlsColorChannel.FABRIC_SURFACE
    ): Int {
        if (channel == DlsColorChannel.NUMBER_DECAL) {
            return colorInt
        }
        val a = Color.alpha(colorInt)
        val r = calibrateFabricChannel(Color.red(colorInt))
        val g = calibrateFabricChannel(Color.green(colorInt))
        val b = calibrateFabricChannel(Color.blue(colorInt))
        return Color.argb(a, r, g, b)
    }

    fun mapHexToDlsTextureHex(
        hex: String,
        channel: DlsColorChannel = DlsColorChannel.FABRIC_SURFACE
    ): String {
        val raw = parseColorHex(hex).toAndroidColorInt()
        val mapped = mapColorToDlsTexture(raw, channel)
        return colorIntToHex(mapped)
    }

    /**
     * Forward-simulates DLS 26's in-game Unity lighting response (`c^0.36 * 193 + 2`) on a 512x512
     * UV pixel so tests and diagnostics can verify how calibrated colors settle in-game.
     */
    fun predictInGameRenderedColor(dlsTextureColorInt: Int): Int {
        fun forward(ch: Int): Int {
            val norm = ch / 255.0
            return (norm.pow(0.36) * 193.0 + 2.0).roundToInt().coerceIn(0, 255)
        }
        return Color.argb(
            Color.alpha(dlsTextureColorInt),
            forward(Color.red(dlsTextureColorInt)),
            forward(Color.green(dlsTextureColorInt)),
            forward(Color.blue(dlsTextureColorInt))
        )
    }

    fun syncSlot(
        slotName: String,
        sourceColorInt: Int,
        channel: DlsColorChannel = DlsColorChannel.FABRIC_SURFACE,
        calibrateForDls26: Boolean = true
    ): DlsSyncedColorSlot {
        val normalizedHex = colorIntToHex(sourceColorInt)
        val textureColorInt = if (calibrateForDls26) {
            mapColorToDlsTexture(sourceColorInt, channel)
        } else {
            sourceColorInt
        }
        val (h, s, l) = hexToHsl(normalizedHex)
        return DlsSyncedColorSlot(
            slotName = slotName,
            sourceHex = normalizedHex,
            sourceColorInt = sourceColorInt,
            dlsTextureColorInt = textureColorInt,
            dlsTextureHex = colorIntToHex(textureColorInt),
            dlsHueDegrees = h.roundToInt().coerceIn(0, 360),
            dlsSaturationPercent = (s * 100f).roundToInt().coerceIn(0, 100),
            dlsLightnessPercent = (l * 100f).roundToInt().coerceIn(0, 100),
            channel = channel
        )
    }

    fun buildOfficialProfile(
        spec: JerseyDesignSpec,
        calibrateForDls26: Boolean = true
    ): DlsOfficialKitColorProfile {
        val rawPrimary = parseColorHex(spec.primaryColorHex).toAndroidColorInt()
        val rawSecondary = parseColorHex(spec.secondaryColorHex).toAndroidColorInt()
        val rawThird = parseColorHex(spec.thirdColorHex).toAndroidColorInt()
        val rawGradEnd = parseColorHex(spec.gradientEndHex).toAndroidColorInt()
        val rawCollar = parseColorHex(spec.collarColorHex).toAndroidColorInt()
        val rawSleeve = parseColorHex(spec.sleeveColorHex).toAndroidColorInt()
        val rawCuff = parseColorHex(spec.cuffColorHex).toAndroidColorInt()
        val rawShorts = parseColorHex(spec.shortsColorHex).toAndroidColorInt()
        val rawSocks = parseColorHex(spec.socksColorHex).toAndroidColorInt()
        val rawShortsTrim = spec.shortsTrimColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawSecondary
        val rawSocksTrim = spec.socksTrimColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawSecondary

        val autoContrast = computeReadableTextColor(
            parseColorHex(spec.primaryColorHex),
            parseColorHex(spec.secondaryColorHex),
            parseColorHex(spec.thirdColorHex)
        ).toAndroidColorInt()

        val rawManufacturer = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoContrast
        val rawSponsor = spec.sponsorColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoContrast
        val rawJerseyNum = spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoContrast
        val rawShortsNum = spec.shortsNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
            ?: spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
            ?: contrastInkFor(rawShorts)
        val rawSockBrand = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: contrastInkFor(rawSocks)
        val rawShortsBrand = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: contrastInkFor(rawShorts)
        val rawCrestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawPrimary
        val rawCrestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawSecondary
        val rawCrestAccent = spec.crestAccentColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawThird

        return DlsOfficialKitColorProfile(
            jerseyPrimary = syncSlot("Jersey Primary", rawPrimary, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            jerseySecondary = syncSlot("Jersey Secondary", rawSecondary, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            jerseyThird = syncSlot("Jersey Accent", rawThird, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            jerseyGradientEnd = syncSlot("Gradient End", rawGradEnd, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            collar = syncSlot("Collar", rawCollar, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            sleeve = syncSlot("Sleeves", rawSleeve, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            cuff = syncSlot("Sleeve Cuffs", rawCuff, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            shortsPrimary = syncSlot("Shorts Primary", rawShorts, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            shortsTrim = syncSlot("Shorts Trim", rawShortsTrim, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            socksPrimary = syncSlot("Socks Primary", rawSocks, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            socksTrim = syncSlot("Socks Trim", rawSocksTrim, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            jerseyNumber = syncSlot("Jersey #", rawJerseyNum, DlsColorChannel.NUMBER_DECAL, calibrateForDls26),
            shortsNumber = syncSlot("Shorts #", rawShortsNum, DlsColorChannel.NUMBER_DECAL, calibrateForDls26),
            manufacturer = syncSlot("Brand Mark", rawManufacturer, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            sponsor = syncSlot("Sponsor", rawSponsor, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            sockBrand = syncSlot("Sock Brand", rawSockBrand, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            shortsBrand = syncSlot("Shorts Brand", rawShortsBrand, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            crestPrimary = syncSlot("Crest Primary", rawCrestPrimary, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            crestSecondary = syncSlot("Crest Secondary", rawCrestSecondary, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            crestAccent = syncSlot("Crest Accent", rawCrestAccent, DlsColorChannel.FABRIC_SURFACE, calibrateForDls26),
            primaryIndicatorCircle = syncSlot("UV Primary Indicator", rawPrimary, DlsColorChannel.PRIMARY_INDICATOR, calibrateForDls26)
        )
    }

    private fun calibrateFabricChannel(channel: Int): Int {
        val norm = channel / 255.0
        return (norm.pow(DLS_FABRIC_INVERSE_GAMMA) * 255.0 + 0.5).toInt().coerceIn(0, 255)
    }

    private fun contrastInkFor(bgColor: Int): Int {
        val lum = (0.299 * Color.red(bgColor) + 0.587 * Color.green(bgColor) + 0.114 * Color.blue(bgColor)) / 255.0
        return if (lum > 0.65) Color.parseColor("#111827") else Color.WHITE
    }

    private fun colorIntToHex(colorInt: Int): String =
        String.format("#%02X%02X%02X", Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
}
