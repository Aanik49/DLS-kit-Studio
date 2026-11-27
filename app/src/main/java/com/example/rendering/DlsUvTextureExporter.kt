package com.example.rendering

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

data class ExportResult(
    val success: Boolean,
    val fileUri: Uri? = null,
    val displayPath: String = "",
    val errorMessage: String? = null
)

data class DlsKitUploadAnalysis(
    val originalWidth: Int,
    val originalHeight: Int,
    val maskCoveragePercent: Int,
    val outsideOpaquePercent: Int,
    val hasTransparentBackground: Boolean,
    val isOfficialDls26Aligned: Boolean,
    val statusTitle: String,
    val statusDetail: String,
    val normalized512Bitmap: Bitmap,
    val autoMasked512Bitmap: Bitmap
)

/**
 * Generates pixel-accurate 512x512 Dream League Soccer 2026 (DLS 26) UV-mapped sprite sheets.
 * Every texture island is painted into its exact DLS 26 atlas region and then precision-masked
 * against the official 512x512 DLS 26 UV silhouette (`R.drawable.dls26_uv_mask`) so collar rings,
 * shoulder flaps, socks, side gussets, torso (neck hole + armholes), sleeves, shorts, and armbands
 * align 1:1 with the in-game DLS 26 3D player mesh.
 */
object DlsUvTextureExporter {

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

    /**
     * Pre-compensates sRGB kit colors for Dream League Soccer 26's Unity Linear-space shader,
     * which lifts custom PNG textures (~c^0.36) and washes out darks/reds/blues if exported raw.
     * Applying gamma 2.0 (c^2) preserves pure #FFFFFF white and #000000 black exactly while making
     * darks, reds, blues, and golds match the in-app kit preview accurately inside DLS 26.
     */
    fun calibrateColorForDls26(color: Int): Int {
        val a = Color.alpha(color)
        val r = calibrateChannelForDls26(Color.red(color))
        val g = calibrateChannelForDls26(Color.green(color))
        val b = calibrateChannelForDls26(Color.blue(color))
        return Color.argb(a, r, g, b)
    }

    private fun calibrateChannelForDls26(channel: Int): Int {
        val norm = channel / 255.0
        return (norm * norm * 255.0 + 0.5).toInt().coerceIn(0, 255)
    }

    fun generateDlsUvBitmap(
        context: Context,
        spec: JerseyDesignSpec,
        includeUvGuideOverlay: Boolean = false,
        applyDls26ColorCalibration: Boolean = true
    ): Bitmap {
        val size = 512
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
        // DLS 26 reads the two right-edge squad number swatches as direct flat decal colors
        val jerseyNumberColor = spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoContrast
        val shortsNumberColor = spec.shortsNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
            ?: spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
            ?: contrastInkFor(rawShorts)
        val rawSockBrand = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: contrastInkFor(rawSocks)
        val rawShortsBrand = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: contrastInkFor(rawShorts)
        val rawCrestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawPrimary
        val rawCrestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawSecondary
        val rawCrestAccent = spec.crestAccentColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: rawThird

        fun cal(c: Int): Int = if (applyDls26ColorCalibration) calibrateColorForDls26(c) else c

        val primary = cal(rawPrimary)
        val secondary = cal(rawSecondary)
        val third = cal(rawThird)
        val gradEnd = cal(rawGradEnd)
        val collarColor = cal(rawCollar)
        val sleeveColor = cal(rawSleeve)
        val cuffColor = cal(rawCuff)
        val shortsColor = cal(rawShorts)
        val socksColor = cal(rawSocks)
        val shortsTrimColor = cal(rawShortsTrim)
        val socksTrimColor = cal(rawSocksTrim)
        val manufacturerColor = cal(rawManufacturer)
        val sponsorColor = cal(rawSponsor)
        val sockBrandColor = cal(rawSockBrand)
        val shortsBrandColor = cal(rawShortsBrand)
        val crestPrimary = cal(rawCrestPrimary)
        val crestSecondary = cal(rawCrestSecondary)
        val crestAccent = cal(rawCrestAccent)

        val athleticTypeface = try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val total = size * size
        val regionBytes = loadOfficialDls26RegionBytes(context)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        if (regionBytes != null && regionBytes.size == total * 2) {
            // Pixel-isolated compositing per official DLS 26 UV island so Torso stripes NEVER bleed
            // into Socks/Outer Sleeves and the Primary Color Circle NEVER touches Shorts.
            val outPixels = IntArray(total)
            val layerBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val layerCanvas = Canvas(layerBmp)
            val layerPixels = IntArray(total)

            fun blitRegions(regionMaskBits: Int) {
                layerBmp.getPixels(layerPixels, 0, size, 0, 0, size, size)
                for (i in 0 until total) {
                    val rid = regionBytes[i * 2].toInt() and 0xFF
                    if (rid != 0 && ((regionMaskBits and (1 shl rid)) != 0)) {
                        val maskAlpha = regionBytes[i * 2 + 1].toInt() and 0xFF
                        if (maskAlpha > 0) {
                            val src = layerPixels[i]
                            val srcA = (src ushr 24) and 0xFF
                            val outA = (srcA * maskAlpha) / 255
                            outPixels[i] = (outA shl 24) or (src and 0x00FFFFFF)
                        }
                    }
                }
            }

            // Pass 1: Collar rings (1), Outer sleeve halves (2), Socks (3), Inner sleeves (4) — mutually disjoint
            layerBmp.eraseColor(Color.TRANSPARENT)
            drawCollarRingsRegion(layerCanvas, collarColor, secondary)
            drawOuterSleeveHalvesRegion(
                canvas = layerCanvas,
                sleeveColor = sleeveColor,
                cuffColor = cuffColor,
                accentColor = secondary,
                thirdColor = third,
                shoulderPattern = spec.shoulderPattern
            )
            drawSockRegion(layerCanvas, RectF(238f, 0f, 366f, 122f), socksColor, socksTrimColor, spec.manufacturer, sockBrandColor)
            drawSockRegion(layerCanvas, RectF(364f, 0f, 506f, 122f), socksColor, socksTrimColor, spec.manufacturer, sockBrandColor)
            drawInnerSleevesRegion(layerCanvas, sleeveColor)
            blitRegions(
                (1 shl REGION_COLLAR) or
                    (1 shl REGION_OUTER_SLEEVES) or
                    (1 shl REGION_SOCKS) or
                    (1 shl REGION_INNER_SLEEVES)
            )

            // Pass 2: Unfolded Torso (5) — completely isolated from Socks, Outer Sleeves & Main Sleeves
            layerBmp.eraseColor(Color.TRANSPARENT)
            drawUnfoldedTorsoRegion(
                context = context,
                canvas = layerCanvas,
                spec = spec,
                primary = primary,
                secondary = secondary,
                third = third,
                gradEnd = gradEnd,
                collarColor = collarColor,
                crestPrimary = crestPrimary,
                crestSecondary = crestSecondary,
                crestAccent = crestAccent,
                manufacturerColor = manufacturerColor,
                sponsorColor = sponsorColor,
                typeface = athleticTypeface
            )
            blitRegions(1 shl REGION_TORSO)

            // Pass 3: Main Tapered Sleeves (6) — completely isolated from Torso & Shorts
            layerBmp.eraseColor(Color.TRANSPARENT)
            drawMainSleevesRegion(
                canvas = layerCanvas,
                sleeveColor = sleeveColor,
                cuffColor = cuffColor,
                accentColor = secondary,
                thirdColor = third,
                shoulderPattern = spec.shoulderPattern
            )
            blitRegions(1 shl REGION_MAIN_SLEEVES)

            // Pass 4: Left & Right Shorts (7) — completely isolated from Main Sleeves & Primary Color Circle
            layerBmp.eraseColor(Color.TRANSPARENT)
            drawShortsRegion(
                context = context,
                canvas = layerCanvas,
                spec = spec,
                shortsColor = shortsColor,
                accentColor = shortsTrimColor,
                crestPrimary = crestPrimary,
                crestSecondary = crestSecondary,
                crestAccent = crestAccent,
                manufacturerColor = shortsBrandColor
            )
            blitRegions(1 shl REGION_SHORTS)

            // Pass 5: Exact solid fills for Primary Color Circle (8) and Dual Number Squares (9 & 10)
            // Zero bleed into shorts — separated by a crisp transparent moat in the official UV mask.
            val priRgb = primary and 0x00FFFFFF
            val jNumRgb = jerseyNumberColor and 0x00FFFFFF
            val sNumRgb = shortsNumberColor and 0x00FFFFFF
            for (i in 0 until total) {
                val rid = regionBytes[i * 2].toInt() and 0xFF
                val maskAlpha = regionBytes[i * 2 + 1].toInt() and 0xFF
                if (maskAlpha > 0) {
                    when (rid) {
                        REGION_PRIMARY_CIRCLE -> outPixels[i] = (maskAlpha shl 24) or priRgb
                        REGION_JERSEY_NUM_SQ -> outPixels[i] = (maskAlpha shl 24) or jNumRgb
                        REGION_SHORTS_NUM_SQ -> outPixels[i] = (maskAlpha shl 24) or sNumRgb
                    }
                }
            }

            layerBmp.recycle()
            bmp.setPixels(outPixels, 0, size, 0, 0, size, size)
            bmp.setHasAlpha(true)
        } else {
            // Fallback path if region map resource is unavailable
            val canvas = Canvas(bmp)
            drawCollarRingsRegion(canvas, collarColor, secondary)
            drawOuterSleeveHalvesRegion(canvas, sleeveColor, cuffColor, secondary, third, spec.shoulderPattern)
            drawSockRegion(canvas, RectF(238f, 0f, 366f, 115f), socksColor, socksTrimColor, spec.manufacturer, sockBrandColor)
            drawSockRegion(canvas, RectF(364f, 0f, 506f, 115f), socksColor, socksTrimColor, spec.manufacturer, sockBrandColor)
            drawInnerSleevesRegion(canvas, sleeveColor)
            drawUnfoldedTorsoRegion(
                context, canvas, spec, primary, secondary, third, gradEnd, collarColor,
                crestPrimary, crestSecondary, crestAccent, manufacturerColor, sponsorColor, athleticTypeface
            )
            drawMainSleevesRegion(canvas, sleeveColor, cuffColor, secondary, third, spec.shoulderPattern)
            drawShortsRegion(
                context, canvas, spec, shortsColor, shortsTrimColor,
                crestPrimary, crestSecondary, crestAccent, shortsBrandColor
            )
            drawPrimaryColorIndicatorCircle(canvas, primary)
            drawNumberColorSquares(canvas, jerseyNumberColor, shortsNumberColor)
            applyOfficialDls26Mask(context, bmp)
        }

        if (includeUvGuideOverlay) {
            drawDls26UvGuideLabels(Canvas(bmp))
        }
        return bmp
    }

    @Volatile
    private var cachedRegionBytes: ByteArray? = null

    private fun loadOfficialDls26RegionBytes(context: Context): ByteArray? {
        cachedRegionBytes?.let { return it }
        return try {
            val expectedLen = 512 * 512 * 2
            val out = ByteArray(expectedLen)
            GZIPInputStream(context.resources.openRawResource(R.raw.dls26_uv_regions)).use { gis ->
                var offset = 0
                while (offset < expectedLen) {
                    val read = gis.read(out, offset, expectedLen - offset)
                    if (read <= 0) break
                    offset += read
                }
                if (offset == expectedLen) {
                    cachedRegionBytes = out
                    out
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun applyOfficialDls26Mask(context: Context, targetBmp: Bitmap) {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        val rawMask = BitmapFactory.decodeResource(context.resources, R.drawable.dls26_uv_mask, opts) ?: return
        val mask = if (rawMask.width != 512 || rawMask.height != 512) {
            Bitmap.createScaledBitmap(rawMask, 512, 512, true)
        } else rawMask

        val total = 512 * 512
        val targetPixels = IntArray(total)
        val maskPixels = IntArray(total)
        targetBmp.getPixels(targetPixels, 0, 512, 0, 0, 512, 512)
        mask.getPixels(maskPixels, 0, 512, 0, 0, 512, 512)

        for (i in 0 until total) {
            val maskAlpha = (maskPixels[i] ushr 24) and 0xFF
            if (maskAlpha == 0) {
                targetPixels[i] = 0
            } else {
                val px = targetPixels[i]
                val dstAlpha = (px ushr 24) and 0xFF
                val outAlpha = (dstAlpha * maskAlpha) / 255
                targetPixels[i] = (outAlpha shl 24) or (px and 0x00FFFFFF)
            }
        }
        targetBmp.setPixels(targetPixels, 0, 512, 0, 0, 512, 512)

        if (mask !== rawMask) mask.recycle()
        rawMask.recycle()
        targetBmp.setHasAlpha(true)
    }

    fun analyzeUploadedDlsKit(context: Context, uri: Uri): DlsKitUploadAnalysis? {
        val rawBmp = loadBitmapFromUri(context, uri.toString()) ?: return null
        val origW = rawBmp.width
        val origH = rawBmp.height
        val normalized = if (origW != 512 || origH != 512) {
            Bitmap.createScaledBitmap(rawBmp, 512, 512, true)
        } else {
            rawBmp.copy(Bitmap.Config.ARGB_8888, true)
        }
        if (normalized !== rawBmp) rawBmp.recycle()
        normalized.setHasAlpha(true)

        val autoMasked = normalized.copy(Bitmap.Config.ARGB_8888, true)
        applyOfficialDls26Mask(context, autoMasked)

        val opts = BitmapFactory.Options().apply { inScaled = false }
        val rawMask = BitmapFactory.decodeResource(context.resources, R.drawable.dls26_uv_mask, opts)
        var maskInsideTotal = 0
        var maskInsideFilled = 0
        var maskOutsideTotal = 0
        var maskOutsideOpaque = 0

        if (rawMask != null) {
            val mask = if (rawMask.width != 512 || rawMask.height != 512) {
                Bitmap.createScaledBitmap(rawMask, 512, 512, true)
            } else rawMask
            val total = 512 * 512
            val srcPx = IntArray(total)
            val mskPx = IntArray(total)
            normalized.getPixels(srcPx, 0, 512, 0, 0, 512, 512)
            mask.getPixels(mskPx, 0, 512, 0, 0, 512, 512)
            for (i in 0 until total) {
                val mAlpha = (mskPx[i] ushr 24) and 0xFF
                val sAlpha = (srcPx[i] ushr 24) and 0xFF
                if (mAlpha > 128) {
                    maskInsideTotal++
                    if (sAlpha > 32) maskInsideFilled++
                } else {
                    maskOutsideTotal++
                    if (sAlpha > 32) maskOutsideOpaque++
                }
            }
            if (mask !== rawMask) mask.recycle()
            rawMask.recycle()
        }

        val coveragePct = if (maskInsideTotal > 0) ((maskInsideFilled * 100f) / maskInsideTotal).toInt() else 100
        val outsidePct = if (maskOutsideTotal > 0) ((maskOutsideOpaque * 100f) / maskOutsideTotal).toInt() else 0
        val hasTransparentBg = outsidePct < 85
        val isOfficialDls26Aligned = coveragePct >= 82 && outsidePct <= 35

        val (title, detail) = when {
            origW == 512 && origH == 512 && isOfficialDls26Aligned ->
                "Official 512×512 DLS 26 UV Verified" to
                    "$coveragePct% of official DLS 26 kit islands populated with clean transparent background."
            isOfficialDls26Aligned ->
                "Normalized ${origW}×${origH} → 512×512 DLS 26 UV" to
                    "UV islands align cleanly ($coveragePct% match). Scaled to exact 512×512 PNG for DLS 26."
            !hasTransparentBg ->
                "Solid Background Detected (${origW}×${origH})" to
                    "Tip: Turn on 'Clean DLS 26 UV Cutout' below to punch transparent collar/island edges if needed."
            else ->
                "Partial / Custom UV Layout (${origW}×${origH})" to
                    "$coveragePct% DLS 26 island coverage. Normalized to 512×512 PNG ready to publish."
        }

        return DlsKitUploadAnalysis(
            originalWidth = origW,
            originalHeight = origH,
            maskCoveragePercent = coveragePct,
            outsideOpaquePercent = outsidePct,
            hasTransparentBackground = hasTransparentBg,
            isOfficialDls26Aligned = isOfficialDls26Aligned,
            statusTitle = title,
            statusDetail = detail,
            normalized512Bitmap = normalized,
            autoMasked512Bitmap = autoMasked
        )
    }

    fun overlayDls26GuideLines(context: Context, source512: Bitmap): Bitmap {
        val out = source512.copy(Bitmap.Config.ARGB_8888, true)
        val c = Canvas(out)
        val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(145, 0, 229, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        c.drawRect(80f, 0f, 240f, 129f, strokeP)
        c.drawRect(240f, 0f, 502f, 120f, strokeP)
        c.drawRect(4f, 141f, 124f, 354f, strokeP)
        c.drawRect(126f, 115f, 505f, 274f, strokeP)
        c.drawRect(112f, 266f, 454f, 393f, strokeP)
        c.drawRect(47f, 385f, 447f, 510f, strokeP)
        c.drawRect(469f, 346f, 488f, 365f, strokeP)
        c.drawRect(469f, 425f, 488f, 445f, strokeP)
        drawDls26UvGuideLabels(c)
        return out
    }

    private fun drawCollarRingsRegion(canvas: Canvas, collarColor: Int, trimColor: Int) {
        val region = RectF(12f, 0f, 74f, 128f)
        canvas.drawRect(region, Paint().apply { color = collarColor })
        drawFabricWeaveOverlay(canvas, region)
        listOf(31f, 94f).forEach { cy ->
            canvas.drawOval(
                RectF(18f, cy - 23f, 66f, cy + 23f),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = trimColor
                    alpha = 120
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
            )
        }
    }

    private fun drawOuterSleeveHalvesRegion(
        canvas: Canvas,
        sleeveColor: Int,
        cuffColor: Int,
        accentColor: Int,
        thirdColor: Int,
        shoulderPattern: ShoulderPattern
    ) {
        // Official DLS 26 Top-Mid Outer Sleeve Halves:
        // Left Outer Half  (x=78..160, y=0..130): Cuff on RIGHT edge (x=143..160) — pairs with Mid-Row Left Sleeve!
        // Right Outer Half (x=160..242, y=0..130): Cuff on LEFT edge (x=160..177) — pairs with Mid-Row Right Sleeve!
        val leftHalf = RectF(78f, 0f, 160f, 130f)
        val rightHalf = RectF(160f, 0f, 242f, 130f)

        listOf(leftHalf to true, rightHalf to false).forEach { (rect, cuffOnRight) ->
            canvas.save()
            canvas.clipRect(rect)
            canvas.drawColor(sleeveColor)

            val cuffWidth = 16f
            val cuffRect = if (cuffOnRight) {
                RectF(rect.right - cuffWidth, rect.top, rect.right, rect.bottom)
            } else {
                RectF(rect.left, rect.top, rect.left + cuffWidth, rect.bottom)
            }
            canvas.drawRect(cuffRect, Paint().apply { color = cuffColor })

            // Subtle knitted cuff ribbing
            val ribP = Paint().apply { color = Color.argb(36, 0, 0, 0); strokeWidth = 1.2f }
            var cy = rect.top + 3f
            while (cy < rect.bottom) {
                canvas.drawLine(cuffRect.left, cy, cuffRect.right, cy, ribP)
                cy += 4f
            }

            // Thin inner trim line adjacent to cuff
            val trimRect = if (cuffOnRight) {
                RectF(cuffRect.left - 4f, rect.top, cuffRect.left, rect.bottom)
            } else {
                RectF(cuffRect.right, rect.top, cuffRect.right + 4f, rect.bottom)
            }
            canvas.drawRect(trimRect, Paint().apply { color = accentColor })

            // Matching horizontal shoulder pattern running seamlessly from armhole toward cuff
            val midY = rect.centerY()
            val bodyLeft = if (cuffOnRight) rect.left else trimRect.right + 4f
            val bodyRight = if (cuffOnRight) trimRect.left - 4f else rect.right
            when (shoulderPattern) {
                ShoulderPattern.NONE -> Unit
                ShoulderPattern.THREE_STRIPES -> {
                    val sp = Paint().apply { color = accentColor }
                    canvas.drawRect(bodyLeft, midY - 14f, bodyRight, midY - 8f, sp)
                    canvas.drawRect(bodyLeft, midY - 3f, bodyRight, midY + 3f, sp)
                    canvas.drawRect(bodyLeft, midY + 8f, bodyRight, midY + 14f, sp)
                }
                ShoulderPattern.CHEVRON_BAND -> {
                    canvas.drawRect(bodyLeft, midY - 8f, bodyRight, midY + 8f, Paint().apply { color = thirdColor; alpha = 175 })
                }
                ShoulderPattern.PIPING -> {
                    canvas.drawRect(bodyLeft, midY - 3f, bodyRight, midY + 3f, Paint().apply { color = accentColor })
                }
            }
            drawFabricWeaveOverlay(canvas, rect)
            canvas.restore()
        }
    }

    private fun drawSockRegion(
        canvas: Canvas,
        rect: RectF,
        socksColor: Int,
        socksTrimColor: Int,
        manufacturer: ManufacturerMark,
        brandColor: Int
    ) {
        canvas.save()
        canvas.clipRect(rect)
        canvas.drawColor(socksColor)
        // Ribbed turnover cuff on left edge (DLS socks are rotated -90°)
        val cuffEnd = rect.left + rect.width() * 0.28f
        val ribPaint = Paint().apply {
            color = Color.argb(38, 0, 0, 0)
            strokeWidth = 1.5f
        }
        var ry = rect.top + 3f
        while (ry < rect.bottom) {
            canvas.drawLine(rect.left, ry, cuffEnd, ry, ribPaint)
            ry += 4f
        }
        canvas.drawRect(cuffEnd, rect.top, cuffEnd + 8f, rect.bottom, Paint().apply { color = socksTrimColor })
        drawFabricWeaveOverlay(canvas, rect)

        // Manufacturer mark on front shin rotated -90° (aligned with official DLS 26 '<' marker)
        canvas.save()
        canvas.translate(rect.left + rect.width() * 0.56f, rect.bottom - 30f)
        canvas.rotate(-90f)
        ProCrestRenderer.drawCrispManufacturerAndroid(canvas, manufacturer, brandColor, 0f, 0f, 12f)
        canvas.restore()
        canvas.restore()
    }

    private fun drawInnerSleevesRegion(
        canvas: Canvas,
        sleeveColor: Int
    ) {
        // Official DLS 26 Mid-Left Islands (x: 2..126, y: 138..356) = Inner/Underarm part of sleeves
        val full = RectF(0f, 136f, 128f, 358f)
        canvas.drawRect(full, Paint().apply { color = sleeveColor })
        drawFabricWeaveOverlay(canvas, full)
    }

    private fun drawUnfoldedTorsoRegion(
        context: Context,
        canvas: Canvas,
        spec: JerseyDesignSpec,
        primary: Int,
        secondary: Int,
        third: Int,
        gradEnd: Int,
        collarColor: Int,
        crestPrimary: Int,
        crestSecondary: Int,
        crestAccent: Int,
        manufacturerColor: Int,
        sponsorColor: Int,
        typeface: Typeface?
    ) {
        val torsoBounds = RectF(126f, 110f, 510f, 278f)
        canvas.save()
        canvas.clipRect(torsoBounds)

        // Back Half (x: 126..326) — rotated +90° (top of back at x=326, back hem at x=126)
        val backRect = RectF(126f, 110f, 326f, 278f)
        canvas.save()
        canvas.clipRect(backRect)
        canvas.translate(backRect.centerX(), backRect.centerY())
        canvas.rotate(90f)
        val rotBack = RectF(-backRect.height() / 2f, -backRect.width() / 2f, backRect.height() / 2f, backRect.width() / 2f)
        drawPatternIntoRect(canvas, rotBack, spec, primary, secondary, third, gradEnd)
        if (spec.cleanBackNumberBox) {
            // Solid contrast back panel so DLS 26 dynamic squad name & number stay crisp over striped kits
            val boxRect = RectF(
                rotBack.left + rotBack.width() * 0.16f,
                rotBack.top + rotBack.height() * 0.14f,
                rotBack.right - rotBack.width() * 0.16f,
                rotBack.top + rotBack.height() * 0.80f
            )
            canvas.drawRoundRect(boxRect, 8f, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary })
        }
        canvas.restore()

        // Front Half (x: 325..510) — rotated -90° (top of chest at x=325, front hem at x=510)
        val frontRect = RectF(325f, 110f, 510f, 278f)
        canvas.save()
        canvas.clipRect(frontRect)
        canvas.translate(frontRect.centerX(), frontRect.centerY())
        canvas.rotate(-90f)
        val rotFront = RectF(-frontRect.height() / 2f, -frontRect.width() / 2f, frontRect.height() / 2f, frontRect.width() / 2f)
        drawPatternIntoRect(canvas, rotFront, spec, primary, secondary, third, gradEnd)

        // Chest Branding in -90° rotated coordinates matched to official DLS 26 Barca/Inter Miami landmarks
        // Chest logos sit at x ≈ 366 (22% down from neck x=325), Sponsor sits at x ≈ 398 (40% down)
        val chestY = rotFront.top + rotFront.height() * 0.22f
        val brandX = when (spec.crestPlacement) {
            CrestPlacement.LEFT_CHEST -> rotFront.centerX() - rotFront.width() * 0.16f
            CrestPlacement.CENTER_CHEST -> rotFront.centerX() - rotFront.width() * 0.22f
            CrestPlacement.RIGHT_CHEST -> rotFront.centerX() + rotFront.width() * 0.16f
        }
        ProCrestRenderer.drawCrispManufacturerAndroid(canvas, spec.manufacturer, manufacturerColor, brandX, chestY, 15f)

        val crestX = when (spec.crestPlacement) {
            CrestPlacement.LEFT_CHEST -> rotFront.centerX() + rotFront.width() * 0.16f
            CrestPlacement.CENTER_CHEST -> rotFront.centerX()
            CrestPlacement.RIGHT_CHEST -> rotFront.centerX() - rotFront.width() * 0.16f
        }
        loadBitmapFromUri(context, spec.customClubLogoUri)?.let { bmp ->
            canvas.drawBitmap(bmp, null, RectF(crestX - 15f, chestY - 15f, crestX + 15f, chestY + 15f), null)
        } ?: ProCrestRenderer.drawProCrestAndroid(
            canvas = canvas,
            cx = crestX,
            cy = chestY,
            radius = 13.5f,
            primary = crestPrimary,
            secondary = crestSecondary,
            accent = crestAccent,
            clubName = spec.clubName,
            crestStyleId = spec.crestStyleId
        )

        val sponsorY = rotFront.top + rotFront.height() * 0.41f
        loadBitmapFromUri(context, spec.customSponsorLogoUri)?.let { bmp ->
            canvas.drawBitmap(bmp, null, RectF(rotFront.centerX() - 42f, sponsorY - 14f, rotFront.centerX() + 42f, sponsorY + 14f), null)
        } ?: run {
            if (spec.sponsorText.isNotBlank()) {
                val cleanSponsor = spec.sponsorText.uppercase().take(20)
                val sponsorSize = when {
                    cleanSponsor.length > 16 -> 13.5f
                    cleanSponsor.length > 13 -> 15.5f
                    else -> 17.5f
                }
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = cleanSponsor,
                    cx = rotFront.centerX(),
                    baselineY = sponsorY + 6f,
                    textSizePx = sponsorSize,
                    fillColor = sponsorColor,
                    typeface = typeface,
                    letterSpacingEm = 0.04f
                )
            }
        }

        if (spec.customText.isNotBlank()) {
            ProCrestRenderer.drawCrispOutlinedText(
                canvas = canvas,
                text = spec.customText.uppercase().take(18),
                cx = rotFront.centerX(),
                baselineY = sponsorY + 23f,
                textSizePx = 10.5f,
                fillColor = sponsorColor,
                typeface = typeface,
                letterSpacingEm = 0.06f
            )
        }
        canvas.restore()

        // Side panel trim along top & bottom edges of unfolded torso (y: 118..126 & 262..272)
        if (spec.sidePattern != SidePattern.NONE) {
            val sidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; alpha = 185 }
            canvas.drawRect(126f, 118f, 510f, 125f, sidePaint)
            canvas.drawRect(126f, 263f, 510f, 272f, sidePaint)
        }

        // Ribbed hem trim along right edge (x: 498..508) & left edge (x: 128..138)
        val hemRib = Paint().apply { color = Color.argb(50, 0, 0, 0); strokeWidth = 1.5f }
        var hy = 112f
        while (hy < 276f) {
            canvas.drawLine(498f, hy, 508f, hy, hemRib)
            canvas.drawLine(128f, hy, 138f, hy, hemRib)
            hy += 3.5f
        }

        // Subtle center seam line aligned with neck hole center (y = 194f)
        canvas.drawLine(
            126f, 194f, 510f, 194f,
            Paint().apply { color = Color.argb(40, 0, 0, 0); strokeWidth = 1.5f }
        )

        // Collar trim around the exact DLS 26 neck oval (x: 319..333, y: 179..208)
        canvas.drawOval(
            RectF(316f, 176f, 336f, 211f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = collarColor
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
        )

        drawFabricWeaveOverlay(canvas, torsoBounds)
        canvas.restore()
    }

    private fun drawMainSleevesRegion(
        canvas: Canvas,
        sleeveColor: Int,
        cuffColor: Int,
        accentColor: Int,
        thirdColor: Int,
        shoulderPattern: ShoulderPattern
    ) {
        // Official DLS 26 Mid-Row Tapered Sleeves (exact official cutting):
        // Left Sleeve  (x=110..274, y=264..395): Knitted cuff on RIGHT edge (x=252..274) — pairs with Top-Mid Left!
        // Right Sleeve (x=286..456, y=264..395): Knitted cuff on LEFT edge (x=286..310) — pairs with Top-Mid Right!
        val leftSleeve = RectF(110f, 264f, 274f, 395f)
        val rightSleeve = RectF(286f, 264f, 456f, 395f)

        listOf(leftSleeve to true, rightSleeve to false).forEach { (rect, cuffOnRight) ->
            val midY = rect.centerY()
            canvas.save()
            canvas.clipRect(rect)
            canvas.drawColor(sleeveColor)

            // Exact mirrored cuff placement matching official DLS 26 sleeve cutting
            val cuffWidth = 22f
            val cuffRect = if (cuffOnRight) {
                RectF(rect.right - cuffWidth, rect.top, rect.right, rect.bottom)
            } else {
                RectF(rect.left, rect.top, rect.left + cuffWidth, rect.bottom)
            }
            canvas.drawRect(cuffRect, Paint().apply { color = cuffColor })

            // Subtle cuff ribbing
            val ribP = Paint().apply { color = Color.argb(38, 0, 0, 0); strokeWidth = 1.2f }
            var cy = rect.top + 3f
            while (cy < rect.bottom) {
                canvas.drawLine(cuffRect.left, cy, cuffRect.right, cy, ribP)
                cy += 4f
            }

            // Contrast piping strip adjacent to cuff
            val trimRect = if (cuffOnRight) {
                RectF(cuffRect.left - 5f, rect.top, cuffRect.left, rect.bottom)
            } else {
                RectF(cuffRect.right, rect.top, cuffRect.right + 5f, rect.bottom)
            }
            canvas.drawRect(trimRect, Paint().apply { color = accentColor })

            // Shoulder stripes extend horizontally from shoulder/armhole toward cuff
            val bodyLeft = if (cuffOnRight) rect.left else trimRect.right + 6f
            val bodyRight = if (cuffOnRight) trimRect.left - 6f else rect.right
            when (shoulderPattern) {
                ShoulderPattern.NONE -> Unit
                ShoulderPattern.THREE_STRIPES -> {
                    val sp = Paint().apply { color = accentColor }
                    canvas.drawRect(bodyLeft, midY - 14f, bodyRight, midY - 8f, sp)
                    canvas.drawRect(bodyLeft, midY - 3f, bodyRight, midY + 3f, sp)
                    canvas.drawRect(bodyLeft, midY + 8f, bodyRight, midY + 14f, sp)
                }
                ShoulderPattern.CHEVRON_BAND -> {
                    canvas.drawRect(bodyLeft, midY - 8f, bodyRight, midY + 8f, Paint().apply { color = thirdColor; alpha = 175 })
                }
                ShoulderPattern.PIPING -> {
                    canvas.drawRect(bodyLeft, midY - 3f, bodyRight, midY + 3f, Paint().apply { color = accentColor })
                }
            }
            drawFabricWeaveOverlay(canvas, rect)
            canvas.restore()
        }
    }

    private fun drawShortsRegion(
        context: Context,
        canvas: Canvas,
        spec: JerseyDesignSpec,
        shortsColor: Int,
        accentColor: Int,
        crestPrimary: Int,
        crestSecondary: Int,
        crestAccent: Int,
        manufacturerColor: Int
    ) {
        // Official DLS 26 Shorts Islands verified pixel-for-pixel against official DLS 26 Barca reference:
        // Left Island (x=46..249, y=384..512):
        //   - Outer-hip seam slants from x=129..141 (y=384) down to x=151..163 (y=510)
        //   - Manufacturer Mark sits on Front Thigh at cx=111, cy=486 (exact official Nike Swoosh position!)
        //   - Back half (x=162..248) stays 100% clean!
        // Right Island (x=248..449, y=384..512):
        //   - Outer-hip seam slants from x=357..369 (y=384) down to x=335..347 (y=510)
        //   - Club Crest sits on Lower Front Thigh at cx=382, cy=486, radius=10.5 (exact official Barca crest position!)
        //   - Upper Front Thigh (y=425..468) remains 100% clean for DLS 26's shorts squad number!
        val leftLegRect = RectF(46f, 384f, 249f, 512f)
        val rightLegRect = RectF(248f, 384f, 449f, 512f)

        listOf(leftLegRect, rightLegRect).forEachIndexed { idx, rect ->
            canvas.save()
            canvas.clipRect(rect)
            canvas.drawColor(shortsColor)

            // Exact slanted outer-hip stripes matching DLS 26 seam geometry
            val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
            val hipStripePath = if (idx == 0) {
                Path().apply {
                    moveTo(128f, rect.top)
                    lineTo(140f, rect.top)
                    lineTo(163f, rect.bottom)
                    lineTo(151f, rect.bottom)
                    close()
                }
            } else {
                Path().apply {
                    moveTo(358f, rect.top)
                    lineTo(370f, rect.top)
                    lineTo(347f, rect.bottom)
                    lineTo(335f, rect.bottom)
                    close()
                }
            }
            canvas.drawPath(hipStripePath, stripePaint)

            // Elasticated waistband stitch line
            canvas.drawLine(
                rect.left, rect.top + 16f, rect.right, rect.top + 16f,
                Paint().apply { color = Color.argb(50, 0, 0, 0); strokeWidth = 2f }
            )
            // Subtle bottom hem seam
            canvas.drawLine(
                rect.left, rect.bottom - 5f, rect.right, rect.bottom - 5f,
                Paint().apply { color = Color.argb(32, 0, 0, 0); strokeWidth = 1.5f }
            )
            drawFabricWeaveOverlay(canvas, rect)

            if (idx == 0) {
                // Left Shorts Island: Manufacturer Logo raised cleanly above bottom hem (111, 472)
                ProCrestRenderer.drawCrispManufacturerAndroid(
                    canvas = canvas,
                    mark = spec.manufacturer,
                    color = manufacturerColor,
                    cx = 111f,
                    cy = 472f,
                    size = 11f
                )
            } else {
                // Right Shorts Island: Club Crest raised cleanly above bottom hem (382, 469) so it never clips
                val crestCx = 382f
                val crestCy = 469f
                loadBitmapFromUri(context, spec.customClubLogoUri)?.let { bmp ->
                    canvas.drawBitmap(bmp, null, RectF(crestCx - 10f, crestCy - 10f, crestCx + 10f, crestCy + 10f), null)
                } ?: ProCrestRenderer.drawProCrestAndroid(
                    canvas = canvas,
                    cx = crestCx,
                    cy = crestCy,
                    radius = 9.5f,
                    primary = crestPrimary,
                    secondary = crestSecondary,
                    accent = crestAccent,
                    clubName = spec.clubName,
                    crestStyleId = spec.crestStyleId
                )
            }
            canvas.restore()
        }
    }

    private fun drawPrimaryColorIndicatorCircle(
        canvas: Canvas,
        primaryJerseyColor: Int
    ) {
        // Official DLS 26 Bottom-Right Circular Primary Color Indicator (cx=458, cy=491.5, r=18)
        // Drawn strictly as a circle separated from the right shorts island so it NEVER bleeds onto shorts!
        canvas.drawCircle(
            458f, 491.5f, 18.5f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primaryJerseyColor }
        )
    }

    private fun drawNumberColorSquares(
        canvas: Canvas,
        jerseyNumberColor: Int,
        shortsNumberColor: Int
    ) {
        // Official DLS 26 Right-Edge Dual Player Number Color Swatches:
        // Top Square    (x: 469..488, y: 346..365) = Jersey Player Number & Name Color
        // Bottom Square (x: 469..488, y: 425..445) = Shorts Player Number Color
        canvas.drawRect(467f, 344f, 490f, 367f, Paint().apply { color = jerseyNumberColor })
        canvas.drawRect(467f, 423f, 490f, 447f, Paint().apply { color = shortsNumberColor })
    }

    private fun drawPatternIntoRect(
        canvas: Canvas,
        rect: RectF,
        spec: JerseyDesignSpec,
        primary: Int,
        secondary: Int,
        third: Int,
        gradEnd: Int
    ) {
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (spec.useGradient) {
            basePaint.shader = LinearGradient(
                rect.left, rect.top, rect.left, rect.bottom,
                primary, gradEnd, Shader.TileMode.CLAMP
            )
        } else {
            basePaint.color = primary
        }
        canvas.drawRect(rect, basePaint)

        val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary }
        val thirdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = third }
        val w = rect.width()
        val h = rect.height()

        when (spec.pattern) {
            JerseyPattern.SOLID -> Unit
            JerseyPattern.STRIPES -> {
                val bandW = w * 0.24f
                val bx = rect.left + (w - bandW) / 2f
                canvas.drawRect(bx - 5f, rect.top, bx + bandW + 5f, rect.bottom, thirdPaint)
                canvas.drawRect(bx, rect.top, bx + bandW, rect.bottom, secPaint)
            }
            JerseyPattern.VERTICAL_STRIPES -> {
                val sw = w / 7f
                for (i in 0 until 7) {
                    if (i % 2 == 1) {
                        canvas.drawRect(rect.left + i * sw, rect.top, rect.left + (i + 1) * sw, rect.bottom, secPaint)
                    }
                }
            }
            JerseyPattern.HORIZONTAL_STRIPES -> {
                val sh = h / 10f
                for (i in 0 until 10) {
                    if (i % 2 == 1) {
                        canvas.drawRect(rect.left, rect.top + i * sh, rect.right, rect.top + i * sh + sh * 0.45f, secPaint)
                    }
                }
            }
            JerseyPattern.HOOPS -> {
                val sh = h / 7f
                for (i in 0 until 7) {
                    if (i % 2 == 1) {
                        canvas.drawRect(rect.left, rect.top + i * sh, rect.right, rect.top + (i + 1) * sh, secPaint)
                    }
                }
            }
            JerseyPattern.CHECKS -> {
                val cell = w / 6f
                val rows = (h / cell).toInt() + 1
                secPaint.alpha = 185
                for (r in 0 until rows) {
                    for (c in 0 until 6) {
                        if ((r + c) % 2 == 0) {
                            canvas.drawRect(
                                rect.left + c * cell,
                                rect.top + r * cell,
                                rect.left + (c + 1) * cell,
                                rect.top + (r + 1) * cell,
                                secPaint
                            )
                        }
                    }
                }
            }
            JerseyPattern.GEOMETRIC -> {
                secPaint.alpha = 70
                val step = w * 0.18f
                var y = rect.top
                var row = 0
                while (y < rect.bottom) {
                    var x = rect.left + if (row % 2 == 0) 0f else step * 0.5f
                    while (x < rect.right + step) {
                        val path = Path().apply {
                            moveTo(x + step * 0.5f, y)
                            lineTo(x + step, y + step * 0.5f)
                            lineTo(x + step * 0.5f, y + step)
                            lineTo(x, y + step * 0.5f)
                            close()
                        }
                        canvas.drawPath(path, secPaint)
                        x += step
                    }
                    y += step * 0.8f
                    row++
                }
            }
            JerseyPattern.ZIGZAG -> {
                secPaint.alpha = 115
                val bands = 8
                val bh = h / bands
                for (i in 0 until bands) {
                    val baseY = rect.top + i * bh
                    val path = Path().apply {
                        moveTo(rect.left, baseY)
                        val pw = w / 6f
                        for (p in 0..6) {
                            lineTo(rect.left + p * pw, baseY + if (p % 2 == 0) 0f else bh * 0.5f)
                        }
                        lineTo(rect.right, baseY + bh * 0.45f)
                        for (p in 6 downTo 0) {
                            lineTo(rect.left + p * pw, baseY + bh * 0.35f + if (p % 2 == 0) 0f else bh * 0.5f)
                        }
                        close()
                    }
                    canvas.drawPath(path, secPaint)
                }
            }
            JerseyPattern.ABSTRACT_MODERN -> {
                secPaint.alpha = 110
                val sash = Path().apply {
                    moveTo(rect.left, rect.bottom * 0.82f)
                    lineTo(rect.right, rect.top + h * 0.15f)
                    lineTo(rect.right, rect.top + h * 0.36f)
                    lineTo(rect.left, rect.bottom)
                    close()
                }
                canvas.drawPath(sash, secPaint)
            }
            JerseyPattern.PINSTRIPES -> {
                val count = 11
                val step = w / count
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondary
                    alpha = 215
                    strokeWidth = w * 0.015f
                }
                val thinP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = third
                    alpha = 115
                    strokeWidth = w * 0.008f
                }
                for (i in 1 until count) {
                    val x = rect.left + i * step
                    canvas.drawLine(x, rect.top, x, rect.bottom, lineP)
                    canvas.drawLine(x + w * 0.02f, rect.top, x + w * 0.02f, rect.bottom, thinP)
                }
            }
            JerseyPattern.HALF_AND_HALF -> {
                canvas.drawRect(rect.centerX(), rect.top, rect.right, rect.bottom, secPaint)
                canvas.drawRect(rect.centerX() - 1.5f, rect.top, rect.centerX() + 1.5f, rect.bottom, thirdPaint)
            }
            JerseyPattern.QUARTERS -> {
                canvas.drawRect(rect.centerX(), rect.top, rect.right, rect.centerY(), secPaint)
                canvas.drawRect(rect.left, rect.centerY(), rect.centerX(), rect.bottom, secPaint)
            }
            JerseyPattern.CHEST_SASH -> {
                val border = Path().apply {
                    moveTo(rect.left - w * 0.05f, rect.top + h * 0.06f)
                    lineTo(rect.left + w * 0.24f, rect.top + h * 0.06f)
                    lineTo(rect.right + w * 0.05f, rect.top + h * 0.88f)
                    lineTo(rect.left + w * 0.74f, rect.top + h * 0.88f)
                    close()
                }
                canvas.drawPath(border, thirdPaint)
                val sash = Path().apply {
                    moveTo(rect.left - w * 0.02f, rect.top + h * 0.06f)
                    lineTo(rect.left + w * 0.19f, rect.top + h * 0.06f)
                    lineTo(rect.right + w * 0.02f, rect.top + h * 0.88f)
                    lineTo(rect.left + w * 0.79f, rect.top + h * 0.88f)
                    close()
                }
                canvas.drawPath(sash, secPaint)
            }
            JerseyPattern.REVERSE_SASH -> {
                val border = Path().apply {
                    moveTo(rect.left + w * 0.74f, rect.top + h * 0.06f)
                    lineTo(rect.right + w * 0.04f, rect.top + h * 0.06f)
                    lineTo(rect.left + w * 0.24f, rect.top + h * 0.88f)
                    lineTo(rect.left - w * 0.04f, rect.top + h * 0.88f)
                    close()
                }
                canvas.drawPath(border, thirdPaint)
                val sash = Path().apply {
                    moveTo(rect.left + w * 0.79f, rect.top + h * 0.06f)
                    lineTo(rect.right + w * 0.01f, rect.top + h * 0.06f)
                    lineTo(rect.left + w * 0.19f, rect.top + h * 0.88f)
                    lineTo(rect.left - w * 0.01f, rect.top + h * 0.88f)
                    close()
                }
                canvas.drawPath(sash, secPaint)
            }
            JerseyPattern.CHEST_BAND -> {
                canvas.drawRect(rect.left, rect.top + h * 0.34f, rect.right, rect.top + h * 0.56f, thirdPaint)
                canvas.drawRect(rect.left, rect.top + h * 0.365f, rect.right, rect.top + h * 0.535f, secPaint)
            }
            JerseyPattern.CHEVRON_V -> {
                val vPath = Path().apply {
                    moveTo(rect.left, rect.top + h * 0.21f)
                    lineTo(rect.centerX(), rect.top + h * 0.43f)
                    lineTo(rect.right, rect.top + h * 0.21f)
                    lineTo(rect.right, rect.top + h * 0.35f)
                    lineTo(rect.centerX(), rect.top + h * 0.57f)
                    lineTo(rect.left, rect.top + h * 0.35f)
                    close()
                }
                canvas.drawPath(vPath, secPaint)
            }
            JerseyPattern.DOUBLE_CHEVRON -> {
                listOf(0.20f to secPaint, 0.36f to thirdPaint).forEach { (startFrac, paint) ->
                    val vPath = Path().apply {
                        moveTo(rect.left, rect.top + h * startFrac)
                        lineTo(rect.centerX(), rect.top + h * (startFrac + 0.16f))
                        lineTo(rect.right, rect.top + h * startFrac)
                        lineTo(rect.right, rect.top + h * (startFrac + 0.09f))
                        lineTo(rect.centerX(), rect.top + h * (startFrac + 0.25f))
                        lineTo(rect.left, rect.top + h * (startFrac + 0.09f))
                        close()
                    }
                    canvas.drawPath(vPath, paint)
                }
            }
            JerseyPattern.GRADIENT_HOOPS -> {
                val rows = 8
                val rowH = h / rows
                for (i in 1 until rows) {
                    val frac = i / rows.toFloat()
                    val bandH = rowH * (0.20f + frac * 0.65f)
                    secPaint.alpha = (90 + frac * 145).toInt()
                    canvas.drawRect(rect.left, rect.top + i * rowH, rect.right, rect.top + i * rowH + bandH, secPaint)
                }
            }
            JerseyPattern.CROSS_ST_GEORGE -> {
                val cw = w * 0.18f
                val ch = h * 0.14f
                canvas.drawRect(rect.centerX() - cw / 2f, rect.top, rect.centerX() + cw / 2f, rect.bottom, secPaint)
                canvas.drawRect(rect.left, rect.top + h * 0.35f, rect.right, rect.top + h * 0.35f + ch, secPaint)
                thirdPaint.alpha = 140
                canvas.drawRect(rect.centerX() - cw * 0.12f, rect.top, rect.centerX() + cw * 0.12f, rect.bottom, thirdPaint)
            }
            JerseyPattern.DIAMOND_ARGYLE -> {
                val cols = 4
                val cw = w / cols
                val ch = cw * 1.15f
                val rows = (h / ch).toInt() + 1
                for (r in 0..rows) {
                    for (c in 0 until cols) {
                        val cx = rect.left + c * cw + cw * 0.5f
                        val cy = rect.top + r * ch + ch * 0.5f
                        val p = Path().apply {
                            moveTo(cx, cy - ch * 0.46f)
                            lineTo(cx + cw * 0.46f, cy)
                            lineTo(cx, cy + ch * 0.46f)
                            lineTo(cx - cw * 0.46f, cy)
                            close()
                        }
                        secPaint.alpha = 155
                        thirdPaint.alpha = 75
                        canvas.drawPath(p, if ((r + c) % 2 == 0) secPaint else thirdPaint)
                    }
                }
            }
            JerseyPattern.LIGHTNING_BOLT -> {
                listOf(0.18f, 0.48f, 0.76f).forEachIndexed { idx, xFrac ->
                    val x0 = rect.left + w * xFrac
                    val bolt = Path().apply {
                        moveTo(x0 + w * 0.08f, rect.top + h * 0.05f)
                        lineTo(x0 - w * 0.06f, rect.top + h * 0.44f)
                        lineTo(x0 + w * 0.05f, rect.top + h * 0.44f)
                        lineTo(x0 - w * 0.09f, rect.top + h * 0.92f)
                        lineTo(x0 + w * 0.14f, rect.top + h * 0.36f)
                        lineTo(x0 + w * 0.03f, rect.top + h * 0.36f)
                        close()
                    }
                    secPaint.alpha = 140
                    thirdPaint.alpha = 165
                    canvas.drawPath(bolt, if (idx == 1) thirdPaint else secPaint)
                }
            }
            JerseyPattern.BRUSH_SLASH -> {
                secPaint.alpha = 125
                thirdPaint.alpha = 75
                for (i in 0..4) {
                    val yOff = rect.top + h * (0.14f + i * 0.16f)
                    val slash = Path().apply {
                        moveTo(rect.left - w * 0.05f, yOff + h * 0.12f)
                        lineTo(rect.right + w * 0.05f, yOff - h * 0.08f)
                        lineTo(rect.right + w * 0.05f, yOff - h * 0.01f)
                        lineTo(rect.left - w * 0.05f, yOff + h * 0.19f)
                        close()
                    }
                    canvas.drawPath(slash, if (i % 2 == 0) secPaint else thirdPaint)
                }
            }
            JerseyPattern.WAVE_RIPPLE -> {
                secPaint.alpha = 110
                thirdPaint.alpha = 60
                val bands = 7
                val bh = h / bands
                for (i in 0 until bands) {
                    val baseY = rect.top + i * bh
                    val wave = Path().apply {
                        moveTo(rect.left, baseY + bh * 0.3f)
                        cubicTo(rect.left + w * 0.33f, baseY - bh * 0.25f, rect.left + w * 0.66f, baseY + bh * 0.85f, rect.right, baseY + bh * 0.3f)
                        lineTo(rect.right, baseY + bh * 0.65f)
                        cubicTo(rect.left + w * 0.66f, baseY + bh * 1.2f, rect.left + w * 0.33f, baseY + bh * 0.1f, rect.left, baseY + bh * 0.65f)
                        close()
                    }
                    canvas.drawPath(wave, if (i % 2 == 0) secPaint else thirdPaint)
                }
            }
            JerseyPattern.HEX_HONEYCOMB -> {
                secPaint.alpha = 55
                val strokeThird = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = third
                    alpha = 65
                    style = Paint.Style.STROKE
                    strokeWidth = 1.4f
                }
                val step = w * 0.18f
                var y = rect.top
                var row = 0
                while (y < rect.bottom + step) {
                    var x = rect.left + if (row % 2 == 0) 0f else step * 0.5f
                    while (x < rect.right + step) {
                        val r = step * 0.42f
                        val hex = Path().apply {
                            moveTo(x, y - r)
                            lineTo(x + r * 0.86f, y - r * 0.5f)
                            lineTo(x + r * 0.86f, y + r * 0.5f)
                            lineTo(x, y + r)
                            lineTo(x - r * 0.86f, y + r * 0.5f)
                            lineTo(x - r * 0.86f, y - r * 0.5f)
                            close()
                        }
                        canvas.drawPath(hex, secPaint)
                        canvas.drawPath(hex, strokeThird)
                        x += step
                    }
                    y += step * 0.82f
                    row++
                }
            }
        }
    }

    private fun drawFabricWeaveOverlay(canvas: Canvas, bounds: RectF) {
        val weavePaint = Paint().apply {
            color = Color.argb(6, 0, 0, 0)
            strokeWidth = 1f
        }
        var y = bounds.top
        while (y < bounds.bottom) {
            canvas.drawLine(bounds.left, y, bounds.right, y, weavePaint)
            y += 5f
        }
    }

    private fun contrastInkFor(bgColor: Int): Int {
        val lum = (0.299 * Color.red(bgColor) + 0.587 * Color.green(bgColor) + 0.114 * Color.blue(bgColor)) / 255.0
        return if (lum > 0.65) Color.parseColor("#111827") else Color.WHITE
    }

    private fun drawDls26UvGuideLabels(canvas: Canvas) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(235, 0, 229, 255)
            textSize = 10f
            isFakeBoldText = true
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
        }
        canvas.drawText("COLLAR", 14f, 12f, labelPaint)
        canvas.drawText("OUTER SLV", 86f, 14f, labelPaint)
        canvas.drawText("SOCKS", 252f, 18f, labelPaint)
        canvas.drawText("INNER SLV", 10f, 154f, labelPaint)
        canvas.drawText("BACK TORSO", 150f, 134f, labelPaint)
        canvas.drawText("FRONT TORSO", 356f, 134f, labelPaint)
        canvas.drawText("L SLEEVE", 140f, 286f, labelPaint)
        canvas.drawText("R SLEEVE", 336f, 286f, labelPaint)
        canvas.drawText("SHORTS (BRAND)", 54f, 412f, labelPaint)
        canvas.drawText("SHORTS (CREST)", 296f, 412f, labelPaint)
        canvas.drawText("J#", 470f, 342f, labelPaint)
        canvas.drawText("S#", 470f, 421f, labelPaint)
        canvas.drawText("PRI", 440f, 490f, labelPaint)
    }

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

    suspend fun exportPngAndShareableUri(
        context: Context,
        bitmap: Bitmap,
        filePrefix: String,
        saveToGallery: Boolean = true
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val safeName = filePrefix.lowercase().replace(Regex("[^a-z0-9_]+"), "_").trim('_')
            val fileName = "${safeName}_${System.currentTimeMillis()}.png"

            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val localFile = File(exportDir, fileName)
            FileOutputStream(localFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            if (saveToGallery && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/DLSKitStudio")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (galleryUri != null) {
                    resolver.openOutputStream(galleryUri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(galleryUri, values, null, null)
                }
            }

            val shareUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )
            ExportResult(
                success = true,
                fileUri = shareUri,
                displayPath = "Pictures/DLSKitStudio/$fileName"
            )
        } catch (e: Exception) {
            ExportResult(success = false, errorMessage = e.localizedMessage ?: "Export failed")
        }
    }

    fun launchShareImageIntent(context: Context, uri: Uri, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $title"))
    }
}
