package com.example.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.SponsorStyle
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt
import java.util.Locale
import kotlin.math.abs

data class ExtractedDlsKitPalette(
    val jerseyPrimaryHex: String,
    val jerseySecondaryHex: String,
    val sleeveHex: String,
    val collarHex: String,
    val shortsHex: String,
    val socksHex: String,
    val jerseyNumberHex: String,
    val shortsNumberHex: String
)

data class DlsUvReverseEditConfig(
    val retintJerseyHex: String? = null,
    val retintJerseyStrength: Float = 0.55f,
    val retintShortsHex: String? = null,
    val retintShortsStrength: Float = 0.65f,
    val retintSocksHex: String? = null,
    val retintSocksStrength: Float = 0.65f,
    val overlayCrestEnabled: Boolean = false,
    val overlayCrestStyleId: String = ClubCrestStyle.ROYAL_CROWN_RING.name,
    val overlayCrestPrimaryHex: String = "#0A2463",
    val overlayCrestSecondaryHex: String = "#FFC400",
    val overlayCrestAccentHex: String = "#FFFFFF",
    val overlayCustomCrestUri: String? = null,
    val overlaySponsorEnabled: Boolean = false,
    val overlaySponsorText: String = "APEX FLY",
    val overlaySponsorStyle: SponsorStyle = SponsorStyle.CLASSIC_BOLD,
    val overlaySponsorColorHex: String = "#FFFFFF",
    val overlayCustomSponsorUri: String? = null,
    val jerseyNumberColorHex: String? = null,
    val shortsNumberColorHex: String? = null
) {
    val hasAnyActiveEdit: Boolean
        get() = retintJerseyHex != null ||
            retintShortsHex != null ||
            retintSocksHex != null ||
            overlayCrestEnabled ||
            overlaySponsorEnabled ||
            jerseyNumberColorHex != null ||
            shortsNumberColorHex != null
}

/**
 * Direct DLS 26 Kit Importer & Reverse Editor:
 *  1) Samples official 512x512 DLS 26 UV islands to extract the real jersey, secondary,
 *     sleeve, collar, shorts, socks, and jersey/shorts number colors from any uploaded PNG.
 *  2) Converts extracted palettes into an editable [JerseyDesignSpec] for Kit Studio.
 *  3) Non-destructively re-tints jersey/shorts/socks islands and stamps custom club crests
 *     and sponsors directly onto the uploaded 512x512 DLS 26 UV texture while keeping
 *     the Primary Color Indicator Circle & Dual Number Squares synced.
 */
object DlsUvReverseEditor {

    private const val REGION_COLLAR = 1
    private const val REGION_OUTER_SLEEVES = 2
    private const val REGION_SOCKS = 3
    private const val REGION_INNER_SLEEVES = 4
    private const val REGION_TORSO = 5
    private const val REGION_MAIN_SLEEVES = 6
    private const val REGION_SHORTS = 7
    private const val REGION_PRIMARY_CIRCLE = 8
    private const val REGION_JERSEY_NUM_SQ = 9
    private const val REGION_SHORTS_NUM_SQ = 10

    fun extractPaletteFromDlsUv(context: Context, bitmap512: Bitmap): ExtractedDlsKitPalette {
        val bmp = if (bitmap512.width == 512 && bitmap512.height == 512) {
            bitmap512
        } else {
            Bitmap.createScaledBitmap(bitmap512, 512, 512, true)
        }
        val pixels = IntArray(512 * 512)
        bmp.getPixels(pixels, 0, 512, 0, 0, 512, 512)
        val regionBytes = DlsUvTextureExporter.getOfficialDls26RegionBytes(context)

        val torsoColors = sampleRegionTopColors(pixels, regionBytes, REGION_TORSO, RectF(150f, 130f, 495f, 258f), topN = 2)
        val indicatorColor = sampleRegionTopColors(pixels, regionBytes, REGION_PRIMARY_CIRCLE, RectF(445f, 476f, 472f, 504f), topN = 1).firstOrNull()
        val sleeveColor = sampleRegionTopColors(pixels, regionBytes, REGION_MAIN_SLEEVES, RectF(135f, 285f, 240f, 375f), topN = 1).firstOrNull()
        val collarColor = sampleRegionTopColors(pixels, regionBytes, REGION_COLLAR, RectF(18f, 12f, 68f, 118f), topN = 1).firstOrNull()
        val shortsColor = sampleRegionTopColors(pixels, regionBytes, REGION_SHORTS, RectF(65f, 410f, 230f, 495f), topN = 1).firstOrNull()
        val socksColor = sampleRegionTopColors(pixels, regionBytes, REGION_SOCKS, RectF(260f, 18f, 355f, 102f), topN = 1).firstOrNull()
        val jerseyNumColor = sampleRegionTopColors(pixels, regionBytes, REGION_JERSEY_NUM_SQ, RectF(471f, 348f, 486f, 363f), topN = 1).firstOrNull()
        val shortsNumColor = sampleRegionTopColors(pixels, regionBytes, REGION_SHORTS_NUM_SQ, RectF(471f, 427f, 486f, 443f), topN = 1).firstOrNull()

        val primary = indicatorColor ?: torsoColors.firstOrNull() ?: Color.parseColor("#0A2463")
        val secondary = torsoColors.firstOrNull { colorDist(it, primary) > 48 }
            ?: torsoColors.getOrNull(1)
            ?: contrastColor(primary)

        if (bmp !== bitmap512) bmp.recycle()

        return ExtractedDlsKitPalette(
            jerseyPrimaryHex = colorIntToHex(primary),
            jerseySecondaryHex = colorIntToHex(secondary),
            sleeveHex = colorIntToHex(sleeveColor ?: primary),
            collarHex = colorIntToHex(collarColor ?: secondary),
            shortsHex = colorIntToHex(shortsColor ?: primary),
            socksHex = colorIntToHex(socksColor ?: shortsColor ?: primary),
            jerseyNumberHex = colorIntToHex(jerseyNumColor ?: contrastColor(primary)),
            shortsNumberHex = colorIntToHex(shortsNumColor ?: jerseyNumColor ?: contrastColor(shortsColor ?: primary))
        )
    }

    fun toEditableJerseySpec(
        palette: ExtractedDlsKitPalette,
        clubName: String = "Imported DLS Kit"
    ): JerseyDesignSpec {
        val hasDistinctSecondary = colorDist(
            parseColorHex(palette.jerseyPrimaryHex).toAndroidColorInt(),
            parseColorHex(palette.jerseySecondaryHex).toAndroidColorInt()
        ) > 55
        return JerseyDesignSpec(
            clubName = clubName.ifBlank { "Imported DLS Kit" },
            primaryColorHex = palette.jerseyPrimaryHex,
            secondaryColorHex = palette.jerseySecondaryHex,
            thirdColorHex = palette.collarHex,
            pattern = if (hasDistinctSecondary) JerseyPattern.VERTICAL_STRIPES else JerseyPattern.SOLID,
            collarColorHex = palette.collarHex,
            sleeveColorHex = palette.sleeveHex,
            cuffColorHex = palette.jerseySecondaryHex,
            shortsColorHex = palette.shortsHex,
            shortsTrimColorHex = palette.jerseySecondaryHex,
            socksColorHex = palette.socksHex,
            socksTrimColorHex = palette.jerseySecondaryHex,
            playerNumberColorHex = palette.jerseyNumberHex,
            shortsNumberColorHex = palette.shortsNumberHex
        )
    }

    fun applyReverseEdits(
        context: Context,
        source512: Bitmap,
        edits: DlsUvReverseEditConfig,
        clubName: String = "Custom Club"
    ): Bitmap {
        val base = if (source512.width == 512 && source512.height == 512) {
            source512.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            Bitmap.createScaledBitmap(source512, 512, 512, true)
        }
        base.setHasAlpha(true)
        if (!edits.hasAnyActiveEdit) return base

        val regionBytes = DlsUvTextureExporter.getOfficialDls26RegionBytes(context)
        val pixels = IntArray(512 * 512)
        base.getPixels(pixels, 0, 512, 0, 0, 512, 512)

        val jerseyTint = edits.retintJerseyHex?.let {
            DlsColorProfileSync.mapColorToDlsTexture(parseColorHex(it).toAndroidColorInt(), DlsColorChannel.FABRIC_SURFACE)
        }
        val shortsTint = edits.retintShortsHex?.let {
            DlsColorProfileSync.mapColorToDlsTexture(parseColorHex(it).toAndroidColorInt(), DlsColorChannel.FABRIC_SURFACE)
        }
        val socksTint = edits.retintSocksHex?.let {
            DlsColorProfileSync.mapColorToDlsTexture(parseColorHex(it).toAndroidColorInt(), DlsColorChannel.FABRIC_SURFACE)
        }
        val jNumCol = edits.jerseyNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
        val sNumCol = edits.shortsNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }

        for (i in pixels.indices) {
            val px = pixels[i]
            val alpha = (px ushr 24) and 0xFF
            if (alpha < 10) continue

            val rid = if (regionBytes != null && regionBytes.size == 512 * 512 * 2) {
                regionBytes[i * 2].toInt() and 0xFF
            } else {
                fallbackRegionForPixel(i % 512, i / 512)
            }

            when (rid) {
                REGION_TORSO, REGION_MAIN_SLEEVES, REGION_OUTER_SLEEVES, REGION_INNER_SLEEVES, REGION_COLLAR -> {
                    if (jerseyTint != null) {
                        pixels[i] = tintPixelPreservingLuma(px, jerseyTint, edits.retintJerseyStrength)
                    }
                }
                REGION_SHORTS -> {
                    if (shortsTint != null) {
                        pixels[i] = tintPixelPreservingLuma(px, shortsTint, edits.retintShortsStrength)
                    }
                }
                REGION_SOCKS -> {
                    if (socksTint != null) {
                        pixels[i] = tintPixelPreservingLuma(px, socksTint, edits.retintSocksStrength)
                    }
                }
                REGION_PRIMARY_CIRCLE -> {
                    if (jerseyTint != null) {
                        pixels[i] = (alpha shl 24) or (jerseyTint and 0x00FFFFFF)
                    }
                }
                REGION_JERSEY_NUM_SQ -> {
                    if (jNumCol != null) {
                        pixels[i] = (alpha shl 24) or (jNumCol and 0x00FFFFFF)
                    }
                }
                REGION_SHORTS_NUM_SQ -> {
                    if (sNumCol != null) {
                        pixels[i] = (alpha shl 24) or (sNumCol and 0x00FFFFFF)
                    }
                }
            }
        }
        base.setPixels(pixels, 0, 512, 0, 0, 512, 512)

        // Stamp custom club crest & sponsor directly at official DLS 26 UV landmarks
        val canvas = Canvas(base)
        val athleticFont = try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        if (edits.overlayCrestEnabled || edits.overlaySponsorEnabled) {
            // Front Torso UV sits in x: 325..510, y: 110..278 rotated -90°
            val frontRect = RectF(325f, 110f, 510f, 278f)
            canvas.save()
            canvas.clipRect(frontRect)
            canvas.translate(frontRect.centerX(), frontRect.centerY())
            canvas.rotate(-90f)
            val rotFront = RectF(-frontRect.height() / 2f, -frontRect.width() / 2f, frontRect.height() / 2f, frontRect.width() / 2f)

            if (edits.overlayCrestEnabled) {
                val chestY = rotFront.top + rotFront.height() * 0.22f
                val crestX = rotFront.centerX() + rotFront.width() * 0.16f
                val customBmp = loadBitmapFromUri(context, edits.overlayCustomCrestUri)
                if (customBmp != null) {
                    canvas.drawBitmap(customBmp, null, RectF(crestX - 11f, chestY - 11f, crestX + 11f, chestY + 11f), null)
                } else {
                    ProCrestRenderer.drawProCrestAndroid(
                        canvas = canvas,
                        cx = crestX,
                        cy = chestY,
                        radius = 9.8f,
                        primary = parseColorHex(edits.overlayCrestPrimaryHex).toAndroidColorInt(),
                        secondary = parseColorHex(edits.overlayCrestSecondaryHex).toAndroidColorInt(),
                        accent = parseColorHex(edits.overlayCrestAccentHex).toAndroidColorInt(),
                        clubName = clubName,
                        crestStyleId = edits.overlayCrestStyleId
                    )
                }
            }

            if (edits.overlaySponsorEnabled) {
                val sponsorY = rotFront.top + rotFront.height() * 0.41f
                val customSp = loadBitmapFromUri(context, edits.overlayCustomSponsorUri)
                if (customSp != null) {
                    canvas.drawBitmap(customSp, null, RectF(rotFront.centerX() - 42f, sponsorY - 14f, rotFront.centerX() + 42f, sponsorY + 14f), null)
                } else if (edits.overlaySponsorText.isNotBlank()) {
                    val sponsorCol = parseColorHex(edits.overlaySponsorColorHex).toAndroidColorInt()
                    KitBadgeRenderer.drawStyledSponsorAndroid(
                        canvas = canvas,
                        text = edits.overlaySponsorText,
                        style = edits.overlaySponsorStyle,
                        cx = rotFront.centerX(),
                        cy = sponsorY,
                        textSizePx = 16.5f,
                        fillColor = sponsorCol,
                        accentColor = sponsorCol,
                        typeface = athleticFont
                    )
                }
            }
            canvas.restore()

            // Also stamp matching crest onto Right Shorts thigh (382, 469) cleanly inside hem!
            if (edits.overlayCrestEnabled) {
                val customBmp = loadBitmapFromUri(context, edits.overlayCustomCrestUri)
                if (customBmp != null) {
                    canvas.drawBitmap(customBmp, null, RectF(373f, 460f, 391f, 478f), null)
                } else {
                    ProCrestRenderer.drawProCrestAndroid(
                        canvas = canvas,
                        cx = 382f,
                        cy = 469f,
                        radius = 9.2f,
                        primary = parseColorHex(edits.overlayCrestPrimaryHex).toAndroidColorInt(),
                        secondary = parseColorHex(edits.overlayCrestSecondaryHex).toAndroidColorInt(),
                        accent = parseColorHex(edits.overlayCrestAccentHex).toAndroidColorInt(),
                        clubName = clubName,
                        crestStyleId = edits.overlayCrestStyleId
                    )
                }
            }
        }

        // Ensure official UV alpha stays clean on number squares if user explicitly updated them
        if (jNumCol != null) {
            canvas.drawRect(470f, 347f, 487f, 364f, Paint().apply { color = jNumCol })
        }
        if (sNumCol != null) {
            canvas.drawRect(470f, 426f, 487f, 444f, Paint().apply { color = sNumCol })
        }

        return base
    }

    private fun tintPixelPreservingLuma(srcPixel: Int, targetColor: Int, strength: Float): Int {
        val alpha = (srcPixel ushr 24) and 0xFF
        val sr = Color.red(srcPixel) / 255f
        val sg = Color.green(srcPixel) / 255f
        val sb = Color.blue(srcPixel) / 255f
        val srcLuma = (0.299f * sr + 0.587f * sg + 0.114f * sb).coerceIn(0f, 1f)

        val tr = Color.red(targetColor) / 255f
        val tg = Color.green(targetColor) / 255f
        val tb = Color.blue(targetColor) / 255f

        // Modulate target color gently by original fabric shading (keeps fold/stripe detail alive!)
        val shade = (0.58f + srcLuma * 0.65f).coerceIn(0.45f, 1.18f)
        val tintedR = (tr * shade).coerceIn(0f, 1f)
        val tintedG = (tg * shade).coerceIn(0f, 1f)
        val tintedB = (tb * shade).coerceIn(0f, 1f)

        val s = strength.coerceIn(0f, 1f)
        val outR = ((sr * (1f - s) + tintedR * s) * 255f).toInt().coerceIn(0, 255)
        val outG = ((sg * (1f - s) + tintedG * s) * 255f).toInt().coerceIn(0, 255)
        val outB = ((sb * (1f - s) + tintedB * s) * 255f).toInt().coerceIn(0, 255)
        return Color.argb(alpha, outR, outG, outB)
    }

    private fun sampleRegionTopColors(
        pixels: IntArray,
        regionBytes: ByteArray?,
        regionId: Int,
        fallbackRect: RectF,
        topN: Int
    ): List<Int> {
        val buckets = HashMap<Int, LongArray>() // quantized key -> [count, sumR, sumG, sumB]
        val useRegionMap = regionBytes != null && regionBytes.size == 512 * 512 * 2

        if (useRegionMap) {
            var idx = 0
            while (idx < pixels.size) {
                val rid = regionBytes!![idx * 2].toInt() and 0xFF
                if (rid == regionId) {
                    val px = pixels[idx]
                    if (((px ushr 24) and 0xFF) > 180) {
                        accumulateBucket(buckets, px)
                    }
                }
                idx += 2 // fast 2x stride sampling
            }
        } else {
            val x0 = fallbackRect.left.toInt().coerceIn(0, 511)
            val x1 = fallbackRect.right.toInt().coerceIn(0, 511)
            val y0 = fallbackRect.top.toInt().coerceIn(0, 511)
            val y1 = fallbackRect.bottom.toInt().coerceIn(0, 511)
            for (y in y0..y1 step 2) {
                for (x in x0..x1 step 2) {
                    val px = pixels[y * 512 + x]
                    if (((px ushr 24) and 0xFF) > 180) {
                        accumulateBucket(buckets, px)
                    }
                }
            }
        }

        if (buckets.isEmpty()) return emptyList()
        val sorted = buckets.values.sortedByDescending { it[0] }
        val picked = ArrayList<Int>(topN)
        for (entry in sorted) {
            val cnt = entry[0].coerceAtLeast(1L)
            val avg = Color.rgb((entry[1] / cnt).toInt(), (entry[2] / cnt).toInt(), (entry[3] / cnt).toInt())
            if (picked.none { colorDist(it, avg) < 42 }) {
                picked.add(avg)
                if (picked.size >= topN) break
            }
        }
        return picked
    }

    private fun accumulateBucket(buckets: HashMap<Int, LongArray>, px: Int) {
        val r = Color.red(px)
        val g = Color.green(px)
        val b = Color.blue(px)
        val key = ((r shr 4) shl 8) or ((g shr 4) shl 4) or (b shr 4)
        val arr = buckets.getOrPut(key) { LongArray(4) }
        arr[0] += 1L
        arr[1] += r.toLong()
        arr[2] += g.toLong()
        arr[3] += b.toLong()
    }

    private fun fallbackRegionForPixel(x: Int, y: Int): Int = when {
        x in 12..74 && y in 0..128 -> REGION_COLLAR
        x in 78..242 && y in 0..130 -> REGION_OUTER_SLEEVES
        x in 238..506 && y in 0..122 -> REGION_SOCKS
        x in 0..126 && y in 136..358 -> REGION_INNER_SLEEVES
        x in 126..510 && y in 110..278 -> REGION_TORSO
        x in 110..456 && y in 264..395 -> REGION_MAIN_SLEEVES
        x in 46..449 && y in 384..511 -> REGION_SHORTS
        x in 440..480 && y in 472..511 -> REGION_PRIMARY_CIRCLE
        x in 468..489 && y in 345..366 -> REGION_JERSEY_NUM_SQ
        x in 468..489 && y in 424..446 -> REGION_SHORTS_NUM_SQ
        else -> 0
    }

    private fun colorDist(a: Int, b: Int): Int =
        abs(Color.red(a) - Color.red(b)) + abs(Color.green(a) - Color.green(b)) + abs(Color.blue(a) - Color.blue(b))

    private fun contrastColor(bg: Int): Int {
        val lum = (0.299 * Color.red(bg) + 0.587 * Color.green(bg) + 0.114 * Color.blue(bg)) / 255.0
        return if (lum > 0.62) Color.parseColor("#0B0D12") else Color.WHITE
    }

    private fun colorIntToHex(color: Int): String =
        String.format(Locale.US, "#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color))

    private fun loadBitmapFromUri(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (_: Exception) {
            null
        }
    }
}
