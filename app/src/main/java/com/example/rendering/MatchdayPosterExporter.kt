package com.example.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.ChestBadgeStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.SleevePatchStyle
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt

/**
 * Renders a 1080x1920 vertical 9:16 Matchday Poster & Squad Card PNG
 * optimized for Instagram Stories, TikTok, and WhatsApp Status:
 *  - Club Crest + Club Name + DLS 26 Slot Header
 *  - High-detail Front + Back Jersey Preview (with GK patterns, sponsor styles, patches, armband)
 *  - Player Squad Card Ribbon (Name, Squad Number, 5-chip full kit palette)
 *  - Scannable QR Code card encoding the DLS 26 Kit URL
 */
object MatchdayPosterExporter {

    const val POSTER_WIDTH = 1080
    const val POSTER_HEIGHT = 1920

    fun generateMatchdayPosterBitmap(
        context: Context,
        spec: JerseyDesignSpec,
        dlsKitUrl: String?,
        seasonLabel: String = "2025/26"
    ): Bitmap {
        val w = POSTER_WIDTH
        val h = POSTER_HEIGHT
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val primary = parseColorHex(spec.primaryColorHex).toAndroidColorInt()
        val secondary = parseColorHex(spec.secondaryColorHex).toAndroidColorInt()
        val third = parseColorHex(spec.thirdColorHex).toAndroidColorInt()
        val sleeveColor = parseColorHex(spec.sleeveColorHex).toAndroidColorInt()
        val cuffColor = parseColorHex(spec.cuffColorHex).toAndroidColorInt()
        val collarColor = parseColorHex(spec.collarColorHex).toAndroidColorInt()
        val shortsColor = parseColorHex(spec.shortsColorHex).toAndroidColorInt()
        val socksColor = parseColorHex(spec.socksColorHex).toAndroidColorInt()

        val crestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: primary
        val crestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: secondary
        val crestAccent = spec.crestAccentColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: third

        val athleticFont = try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        // 1. Dynamic dark stadium backdrop infused with club primary/secondary atmosphere
        val darkPrimary = blendColor(Color.rgb(8, 12, 24), primary, 0.28f)
        val deepBase = Color.rgb(6, 9, 18)
        canvas.drawRect(
            0f, 0f, w.toFloat(), h.toFloat(),
            Paint().apply {
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    intArrayOf(darkPrimary, deepBase, blendColor(deepBase, secondary, 0.22f)),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        )

        // Stadium spotlight glow behind jerseys
        canvas.drawCircle(
            w / 2f, 760f, 520f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    w / 2f, 760f, 520f,
                    Color.argb(85, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
        )

        // Diagonal broadcast accent beams
        val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(26, Color.red(primary), Color.green(primary), Color.blue(primary))
        }
        val beam = Path().apply {
            moveTo(0f, 240f)
            lineTo(w.toFloat(), 40f)
            lineTo(w.toFloat(), 210f)
            lineTo(0f, 410f)
            close()
        }
        canvas.drawPath(beam, beamPaint)

        // Subtle outer broadcast frame
        canvas.drawRoundRect(
            RectF(28f, 28f, w - 28f, h - 28f),
            36f, 36f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(72, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
        )

        // 2. Top Header: Matchday Tag + Large Club Crest + Club Name
        val pillRect = RectF(w / 2f - 230f, 64f, w / 2f + 230f, 118f)
        canvas.drawRoundRect(
            pillRect, 28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(55, Color.red(secondary), Color.green(secondary), Color.blue(secondary)) }
        )
        canvas.drawRoundRect(
            pillRect, 28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secondary
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
        )
        val pillText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 26f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.14f
        }
        canvas.drawText("DLS 26 MATCHDAY • ${spec.slotType.shortLabel} • $seasonLabel", w / 2f, 100f, pillText)

        // Club Crest
        val customCrestBmp = loadBitmapFromUri(context, spec.customClubLogoUri)
        if (customCrestBmp != null) {
            canvas.drawBitmap(customCrestBmp, null, RectF(w / 2f - 78f, 146f, w / 2f + 78f, 302f), null)
        } else {
            ProCrestRenderer.drawProCrestAndroid(
                canvas = canvas,
                cx = w / 2f,
                cy = 224f,
                radius = 74f,
                primary = crestPrimary,
                secondary = crestSecondary,
                accent = crestAccent,
                clubName = spec.clubName,
                crestStyleId = spec.crestStyleId
            )
        }

        // Club Name
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 72f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.06f
            setShadowLayer(12f, 0f, 4f, Color.argb(160, 0, 0, 0))
        }
        canvas.drawText(spec.clubName.ifBlank { "CUSTOM CLUB FC" }.uppercase().take(20), w / 2f, 386f, titlePaint)

        // Accent divider bar
        canvas.drawRoundRect(
            RectF(w / 2f - 140f, 408f, w / 2f + 140f, 416f),
            4f, 4f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary }
        )

        // 3. Center Dual Front & Back Jersey Showcase
        val customSponsorBmp = loadBitmapFromUri(context, spec.customSponsorLogoUri)
        val customStickerBmp = loadBitmapFromUri(context, spec.customStickerUri)
        drawPosterJersey(
            canvas = canvas,
            left = 54f,
            top = 460f,
            w = 468f,
            h = 568f,
            primary = primary,
            secondary = secondary,
            third = third,
            sleeveColor = sleeveColor,
            cuffColor = cuffColor,
            collarColor = collarColor,
            spec = spec,
            isBack = false,
            customCrestBmp = customCrestBmp,
            customSponsorBmp = customSponsorBmp,
            customStickerBmp = customStickerBmp,
            font = athleticFont
        )
        drawPosterJersey(
            canvas = canvas,
            left = 558f,
            top = 460f,
            w = 468f,
            h = 568f,
            primary = primary,
            secondary = secondary,
            third = third,
            sleeveColor = sleeveColor,
            cuffColor = cuffColor,
            collarColor = collarColor,
            spec = spec,
            isBack = true,
            customCrestBmp = customCrestBmp,
            customSponsorBmp = customSponsorBmp,
            customStickerBmp = customStickerBmp,
            font = athleticFont
        )

        val viewLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 198, 224)
            textAlign = Paint.Align.CENTER
            textSize = 26f
            typeface = athleticFont
            letterSpacing = 0.14f
        }
        canvas.drawText("FRONT KIT", 288f, 1072f, viewLabelPaint)
        canvas.drawText("BACK KIT", 792f, 1072f, viewLabelPaint)

        // 4. Squad Card Player Ribbon (Player Name + Giant Number + Full Kit Color Palette)
        val squadCard = RectF(64f, 1114f, w - 64f, 1368f)
        canvas.drawRoundRect(
            squadCard, 28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(195, 15, 23, 42) }
        )
        canvas.drawRoundRect(
            squadCard, 28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
        )

        // Giant squad number badge on left of Squad Card
        val numBadge = RectF(94f, 1142f, 290f, 1340f)
        canvas.drawRoundRect(
            numBadge, 22f, 22f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary }
        )
        val numColor = spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() }
            ?: computeReadableTextColor(
                parseColorHex(spec.primaryColorHex),
                parseColorHex(spec.secondaryColorHex),
                parseColorHex(spec.thirdColorHex)
            ).toAndroidColorInt()
        ProCrestRenderer.drawCrispOutlinedText(
            canvas = canvas,
            text = spec.playerNumber.ifBlank { "10" }.take(2),
            cx = numBadge.centerX(),
            baselineY = numBadge.centerY() + 44f,
            textSizePx = 126f,
            fillColor = numColor,
            typeface = athleticFont,
            letterSpacingEm = 0.02f
        )

        // Player name + kit specs on right of Squad Card
        val squadNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.LEFT
            textSize = 58f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.06f
        }
        canvas.drawText(
            spec.playerName.ifBlank { "CAPTAIN" }.uppercase().take(14),
            326f, 1212f, squadNamePaint
        )
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textAlign = Paint.Align.LEFT
            textSize = 26f
            typeface = athleticFont
            letterSpacing = 0.08f
        }
        canvas.drawText(
            "${spec.pattern.label.uppercase()}  •  ${spec.slotType.label.uppercase()}",
            326f, 1254f, metaPaint
        )

        // 5-dot full kit palette strip inside Squad Card
        val paletteChips = listOf(
            "PRI" to primary,
            "SEC" to secondary,
            "TRM" to third,
            "SHT" to shortsColor,
            "SCK" to socksColor
        )
        paletteChips.forEachIndexed { idx, (_, col) ->
            val cx = 348f + idx * 78f
            val cy = 1312f
            canvas.drawCircle(cx, cy, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col })
            canvas.drawCircle(cx, cy, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })
        }

        // 5. Bottom Scannable QR Code Card for DLS 26 Custom Kit URL
        val qrCard = RectF(64f, 1412f, w - 64f, 1824f)
        canvas.drawRoundRect(
            qrCard, 30f, 30f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(235, 13, 20, 36) }
        )
        canvas.drawRoundRect(
            qrCard, 30f, 30f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(115, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
        )

        val qrPayload = dlsKitUrl?.takeIf { it.isNotBlank() }
            ?: "DLS26-KIT:${spec.clubName.uppercase().take(18)}|${spec.slotType.shortLabel}|${spec.primaryColorHex}|${spec.secondaryColorHex}|${spec.shortsColorHex}"
        val qrBitmap = QrCodeEncoder.renderQrBitmap(
            content = qrPayload,
            pixelSize = 332,
            darkColor = Color.rgb(11, 13, 18),
            lightColor = Color.WHITE,
            quietZoneModules = 3
        )
        val qrFrame = RectF(98f, 1452f, 430f, 1784f)
        canvas.drawRoundRect(qrFrame, 22f, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        canvas.drawBitmap(qrBitmap, null, RectF(qrFrame.left + 8f, qrFrame.top + 8f, qrFrame.right - 8f, qrFrame.bottom - 8f), null)
        qrBitmap.recycle()

        val qrHeadline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.LEFT
            textSize = 44f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.05f
        }
        val qrAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 229, 255)
            textAlign = Paint.Align.LEFT
            textSize = 27f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.10f
        }
        val qrSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            textAlign = Paint.Align.LEFT
            textSize = 25f
            typeface = Typeface.SANS_SERIF
        }

        val textX = 468f
        canvas.drawText("SCAN FOR DLS 26 KIT", textX, 1518f, qrAccent)
        canvas.drawText(
            if (!dlsKitUrl.isNullOrBlank()) "LIVE IMPORT URL" else "KIT DESIGN CODE",
            textX, 1574f, qrHeadline
        )
        canvas.drawText("My Club → Customise → Kit", textX, 1626f, qrSub)
        canvas.drawText("Custom Kit → Paste 512×512 PNG", textX, 1662f, qrSub)

        val displayUrl = (dlsKitUrl?.takeIf { it.isNotBlank() } ?: "Publish Live URL to bind direct link")
        val clippedUrl = if (displayUrl.length > 34) displayUrl.take(33) + "…" else displayUrl
        canvas.drawText(
            clippedUrl,
            textX, 1726f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 22f
                typeface = Typeface.MONOSPACE
            }
        )
        canvas.drawText(
            "CREATED WITH DLS KIT STUDIO",
            textX, 1776f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 22f
                typeface = athleticFont
                letterSpacing = 0.14f
            }
        )

        return bmp
    }

    private fun drawPosterJersey(
        canvas: Canvas,
        left: Float,
        top: Float,
        w: Float,
        h: Float,
        primary: Int,
        secondary: Int,
        third: Int,
        sleeveColor: Int,
        cuffColor: Int,
        collarColor: Int,
        spec: JerseyDesignSpec,
        isBack: Boolean,
        customCrestBmp: Bitmap?,
        customSponsorBmp: Bitmap?,
        customStickerBmp: Bitmap?,
        font: Typeface?
    ) {
        val cx = left + w / 2f
        val torsoLeft = left + w * 0.258f
        val torsoRight = left + w * 0.742f
        val shoulderOuterLeft = left + w * 0.228f
        val shoulderOuterRight = left + w * 0.772f
        val neckY = top + h * 0.052f
        val shoulderY = top + h * 0.118f
        val armholeY = top + h * 0.395f
        val hemY = top + h * 0.945f
        val sleeveTipL = left + w * 0.095f
        val sleeveTipR = left + w * 0.905f
        val sleeveTipY = top + h * 0.415f
        val sleeveInL = left + w * 0.206f
        val sleeveInR = left + w * 0.794f
        val sleeveInY = top + h * 0.478f

        val shirtPath = Path().apply {
            moveTo(cx - w * 0.114f, neckY)
            cubicTo(cx - w * 0.175f, neckY + h * 0.024f, shoulderOuterLeft + w * 0.015f, shoulderY * 0.86f, shoulderOuterLeft, shoulderY)
            quadTo((shoulderOuterLeft + sleeveTipL) * 0.48f, (shoulderY + sleeveTipY) * 0.46f, sleeveTipL, sleeveTipY)
            quadTo((sleeveTipL + sleeveInL) / 2f, (sleeveTipY + sleeveInY) / 2f + h * 0.012f, sleeveInL, sleeveInY)
            quadTo(torsoLeft - w * 0.018f, armholeY + h * 0.032f, torsoLeft, armholeY)
            cubicTo(torsoLeft + w * 0.03f, top + h * 0.56f, torsoLeft + w * 0.02f, top + h * 0.78f, torsoLeft - w * 0.01f, hemY)
            quadTo(cx, hemY + h * 0.036f, torsoRight + w * 0.01f, hemY)
            cubicTo(torsoRight - w * 0.02f, top + h * 0.78f, torsoRight - w * 0.03f, top + h * 0.56f, torsoRight, armholeY)
            quadTo(torsoRight + w * 0.018f, armholeY + h * 0.032f, sleeveInR, sleeveInY)
            quadTo((sleeveTipR + sleeveInR) / 2f, (sleeveTipY + sleeveInY) / 2f + h * 0.012f, sleeveTipR, sleeveTipY)
            quadTo((shoulderOuterRight + sleeveTipR) * 0.52f, (shoulderY + sleeveTipY) * 0.46f, shoulderOuterRight, shoulderY)
            cubicTo(shoulderOuterRight - w * 0.015f, shoulderY * 0.86f, cx + w * 0.175f, neckY + h * 0.024f, cx + w * 0.114f, neckY)
            quadTo(cx, neckY + h * 0.055f, cx - w * 0.114f, neckY)
            close()
        }

        val torsoPath = Path().apply {
            moveTo(cx - w * 0.114f, neckY)
            lineTo(torsoLeft, shoulderY + h * 0.02f)
            lineTo(torsoLeft, armholeY)
            lineTo(torsoLeft - w * 0.01f, hemY)
            quadTo(cx, hemY + h * 0.036f, torsoRight + w * 0.01f, hemY)
            lineTo(torsoRight, armholeY)
            lineTo(torsoRight, shoulderY + h * 0.02f)
            lineTo(cx + w * 0.114f, neckY)
            close()
        }

        canvas.save()
        canvas.clipPath(shirtPath)
        canvas.drawRect(RectF(left, top, left + w, top + h), Paint().apply { color = sleeveColor })

        canvas.save()
        canvas.clipPath(torsoPath)
        if (spec.useGradient) {
            val gradEnd = parseColorHex(spec.gradientEndHex).toAndroidColorInt()
            canvas.drawRect(
                RectF(torsoLeft - 10f, top, torsoRight + 10f, top + h),
                Paint().apply {
                    shader = LinearGradient(0f, top, 0f, hemY, primary, gradEnd, Shader.TileMode.CLAMP)
                }
            )
        } else {
            canvas.drawRect(RectF(torsoLeft - 10f, top, torsoRight + 10f, top + h), Paint().apply { color = primary })
        }
        StudioPreviewExporter.drawShowcasePatternPublic(canvas, spec.pattern, secondary, third, torsoLeft, top, torsoRight - torsoLeft, h)
        canvas.restore()

        // Sleeve cuffs
        val cuffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cuffColor
            strokeWidth = w * 0.032f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(sleeveTipL, sleeveTipY, sleeveInL, sleeveInY, cuffPaint)
        canvas.drawLine(sleeveTipR, sleeveTipY, sleeveInR, sleeveInY, cuffPaint)

        // Sleeve patch (Right sleeve) & Captain armband (Left sleeve)
        if (!isBack) {
            if (spec.sleevePatch != SleevePatchStyle.NONE) {
                KitBadgeRenderer.drawSleevePatchAndroid(
                    canvas = canvas,
                    patch = spec.sleevePatch,
                    cx = left + w * 0.165f,
                    cy = top + h * 0.34f,
                    radius = w * 0.032f
                )
            }
            if (spec.captainArmbandEnabled) {
                val bandCol = parseColorHex(spec.captainArmbandColorHex).toAndroidColorInt()
                KitBadgeRenderer.drawCaptainArmbandAndroid(
                    canvas = canvas,
                    rect = RectF(left + w * 0.775f, top + h * 0.32f, left + w * 0.90f, top + h * 0.39f),
                    bandColor = bandCol,
                    verticalC = false
                )
            }
        }

        // Lighting depth
        canvas.drawRect(
            RectF(left, top, left + w, top + h),
            Paint().apply {
                shader = LinearGradient(
                    left, top, left + w, top,
                    intArrayOf(
                        Color.argb(62, 0, 0, 0),
                        Color.TRANSPARENT,
                        Color.argb(38, 255, 255, 255),
                        Color.TRANSPARENT,
                        Color.argb(62, 0, 0, 0)
                    ),
                    floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        )
        canvas.restore()

        canvas.drawPath(shirtPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.6f
        })

        // Crew neck collar
        val collarDepth = if (isBack) h * 0.048f else h * 0.072f
        val collarPath = Path().apply {
            moveTo(cx - w * 0.114f, neckY)
            quadTo(cx, neckY + collarDepth, cx + w * 0.114f, neckY)
        }
        canvas.drawPath(collarPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = collarColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
        })

        val autoTextColor = computeReadableTextColor(
            parseColorHex(spec.primaryColorHex),
            parseColorHex(spec.secondaryColorHex),
            parseColorHex(spec.thirdColorHex)
        ).toAndroidColorInt()
        val manufacturerColor = spec.manufacturerColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoTextColor
        val sponsorColor = spec.sponsorColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoTextColor
        val numberColor = spec.playerNumberColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: autoTextColor
        val crestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: primary
        val crestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: secondary
        val crestAccent = spec.crestAccentColorHex?.let { parseColorHex(it).toAndroidColorInt() } ?: third

        if (!isBack) {
            val chestY = top + h * 0.26f
            val brandX = when (spec.crestPlacement) {
                CrestPlacement.LEFT_CHEST -> cx - w * 0.118f
                CrestPlacement.CENTER_CHEST -> cx - w * 0.145f
                CrestPlacement.RIGHT_CHEST -> cx + w * 0.118f
            }
            val crestX = when (spec.crestPlacement) {
                CrestPlacement.LEFT_CHEST -> cx + w * 0.118f
                CrestPlacement.CENTER_CHEST -> cx
                CrestPlacement.RIGHT_CHEST -> cx - w * 0.118f
            }
            ProCrestRenderer.drawCrispManufacturerAndroid(canvas, spec.manufacturer, manufacturerColor, brandX, chestY, w * 0.056f)
            if (customCrestBmp != null) {
                val r = w * 0.036f
                canvas.drawBitmap(customCrestBmp, null, RectF(crestX - r, chestY - r, crestX + r, chestY + r), null)
            } else {
                ProCrestRenderer.drawProCrestAndroid(
                    canvas = canvas,
                    cx = crestX,
                    cy = chestY,
                    radius = w * 0.034f,
                    primary = crestPrimary,
                    secondary = crestSecondary,
                    accent = crestAccent,
                    clubName = spec.clubName,
                    crestStyleId = spec.crestStyleId
                )
            }

            if (spec.chestBadge != ChestBadgeStyle.NONE) {
                val badgeX = if (spec.crestPlacement == CrestPlacement.CENTER_CHEST) cx + w * 0.135f else cx
                KitBadgeRenderer.drawChestBadgeAndroid(canvas, spec.chestBadge, badgeX, chestY - h * 0.008f, w * 0.027f)
            }

            val sponsorY = top + h * 0.465f
            if (customSponsorBmp != null) {
                val spW = w * 0.30f * spec.customSponsorScale.coerceIn(0.4f, 1.8f)
                val spH = h * 0.12f * spec.customSponsorScale.coerceIn(0.4f, 1.8f)
                val sx = cx + spec.customSponsorOffsetX * w * 0.22f
                val sy = sponsorY + spec.customSponsorOffsetY * h * 0.22f
                canvas.drawBitmap(customSponsorBmp, null, RectF(sx - spW / 2f, sy - spH / 2f, sx + spW / 2f, sy + spH / 2f), null)
            } else if (spec.sponsorText.isNotBlank()) {
                KitBadgeRenderer.drawStyledSponsorAndroid(
                    canvas = canvas,
                    text = spec.sponsorText,
                    style = spec.sponsorStyle,
                    cx = cx,
                    cy = sponsorY,
                    textSizePx = w * 0.050f,
                    fillColor = sponsorColor,
                    accentColor = third,
                    typeface = font
                )
            }

            if (customStickerBmp != null) {
                val stSize = w * 0.16f * spec.customStickerScale.coerceIn(0.4f, 1.8f)
                val sx = cx + spec.customStickerOffsetX * w * 0.20f
                val sy = top + h * (0.48f + spec.customStickerOffsetY * 0.32f)
                canvas.drawBitmap(customStickerBmp, null, RectF(sx - stSize / 2f, sy - stSize / 2f, sx + stSize / 2f, sy + stSize / 2f), null)
            }
        } else {
            if (spec.cleanBackNumberBox) {
                canvas.drawRoundRect(
                    RectF(torsoLeft + (torsoRight - torsoLeft) * 0.15f, top + h * 0.16f, torsoRight - (torsoRight - torsoLeft) * 0.15f, top + h * 0.78f),
                    14f, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary }
                )
            }
            if (spec.playerName.isNotBlank()) {
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = spec.playerName.uppercase().take(12),
                    cx = cx,
                    baselineY = top + h * 0.26f,
                    textSizePx = 32f,
                    fillColor = numberColor,
                    typeface = font,
                    letterSpacingEm = 0.08f
                )
            }
            if (spec.playerNumber.isNotBlank()) {
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = spec.playerNumber.take(2),
                    cx = cx,
                    baselineY = top + h * 0.62f,
                    textSizePx = 118f,
                    fillColor = numberColor,
                    typeface = font,
                    letterSpacingEm = 0.02f
                )
            }
        }
    }

    private fun blendColor(a: Int, b: Int, ratio: Float): Int {
        val inv = 1f - ratio
        return Color.rgb(
            (Color.red(a) * inv + Color.red(b) * ratio).toInt().coerceIn(0, 255),
            (Color.green(a) * inv + Color.green(b) * ratio).toInt().coerceIn(0, 255),
            (Color.blue(a) * inv + Color.blue(b) * ratio).toInt().coerceIn(0, 255)
        )
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
}
