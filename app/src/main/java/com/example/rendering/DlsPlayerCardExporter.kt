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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import com.example.data.model.DlsPlayerCardSpec
import com.example.data.model.FlagLayout
import com.example.data.model.PlayerCardTier
import com.example.data.model.PlayerCountry
import com.example.data.model.dlsStatColorHex
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders authentic Dream League Soccer 26 Player Cards:
 *  1) Full Horizontal Stats Card (1280 x 760 PNG) matching the official DLS profile panel:
 *     - Top full-name banner, 2x4 color-coded circular stats grid (SPE/ACC/STA/STR + CON/PAS/SHO/TAC or GKH/GKR),
 *       height + preferred foot footer, and right-side DLS Shield Card.
 *  2) Standalone Transparent DLS Shield Card (720 x 960 PNG) for thumbnails & edits.
 */
object DlsPlayerCardExporter {

    private fun safeColor(hex: String, fallback: Int = Color.WHITE): Int = try {
        Color.parseColor(hex)
    } catch (_: Exception) {
        fallback
    }

    fun renderFullStatsCardBitmap(
        context: Context,
        spec: DlsPlayerCardSpec,
        width: Int = 1280,
        height: Int = 760
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val w = width.toFloat()
        val h = height.toFloat()

        val outerRect = RectF(4f, 4f, w - 4f, h - 4f)
        val cornerRadius = h * 0.042f

        // Clip outer rounded card container
        val clipPath = Path().apply {
            addRoundRect(outerRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)

        // Main dark slate body gradient
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h,
                intArrayOf(Color.parseColor("#54606D"), Color.parseColor("#475360"), Color.parseColor("#3E4A56")),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(outerRect, bodyPaint)

        // Top Name Header Strip (exact lighter slate bar)
        val headerHeight = h * 0.165f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, headerHeight,
                Color.parseColor("#6B7682"),
                Color.parseColor("#586470"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, headerHeight, headerPaint)
        canvas.drawLine(
            0f, headerHeight, w, headerHeight,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#343F4A")
                strokeWidth = 3f
            }
        )

        // Bottom Height & Foot Footer Strip
        val footerTop = h * 0.855f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, footerTop, 0f, h,
                Color.parseColor("#394552"),
                Color.parseColor("#2E3843"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, footerTop, w, h, footerPaint)
        canvas.drawLine(
            0f, footerTop, w, footerTop,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#28323D")
                strokeWidth = 2.5f
            }
        )

        // Full Player Name in Top Banner
        val maxNameWidth = w * 0.68f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = h * 0.102f
            letterSpacing = 0.02f
            setShadowLayer(4f, 0f, 2f, Color.argb(120, 0, 0, 0))
        }
        val displayName = spec.fullName.trim().ifBlank { "CUSTOM PLAYER" }.uppercase()
        while (namePaint.measureText(displayName) > maxNameWidth && namePaint.textSize > h * 0.055f) {
            namePaint.textSize -= 2f
        }
        canvas.drawText(displayName, w * 0.032f, headerHeight * 0.75f, namePaint)

        // 2x4 Circular Stat Badges Grid on Left
        val isGk = spec.position.isGoalkeeper
        val topRowStats = listOf(
            "SPE" to spec.spe,
            "ACC" to spec.acc,
            "STA" to spec.sta,
            "STR" to spec.str
        )
        val bottomRowStats = if (isGk) {
            listOf(
                "CON" to spec.con,
                "PAS" to spec.pas,
                "GKH" to spec.gkh,
                "GKR" to spec.gkr
            )
        } else {
            listOf(
                "CON" to spec.con,
                "PAS" to spec.pas,
                "SHO" to spec.sho,
                "TAC" to spec.tac
            )
        }

        val gridLeft = w * 0.024f
        val gridRight = w * 0.562f
        val colWidth = (gridRight - gridLeft) / 4f
        val circleRadius = min(colWidth * 0.46f, h * 0.118f)

        val row1LabelY = headerHeight + h * 0.095f
        val row1CircleCy = row1LabelY + circleRadius + h * 0.015f

        val row2LabelY = row1CircleCy + circleRadius + h * 0.088f
        val row2CircleCy = row2LabelY + circleRadius + h * 0.015f

        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = h * 0.058f
            textAlign = Paint.Align.CENTER
            setShadowLayer(3f, 0f, 2f, Color.argb(110, 0, 0, 0))
        }
        val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06080B")
            style = Paint.Style.FILL
        }
        val circleRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(65, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
        }
        val statValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        fun drawStatRow(items: List<Pair<String, Int>>, labelY: Float, circleCy: Float) {
            items.forEachIndexed { idx, (label, rawValue) ->
                val clamped = rawValue.coerceIn(1, 100)
                val cx = gridLeft + colWidth * (idx + 0.5f)
                canvas.drawText(label, cx, labelY, statLabelPaint)
                canvas.drawCircle(cx, circleCy, circleRadius, circleFillPaint)
                canvas.drawCircle(cx, circleCy, circleRadius - 1f, circleRimPaint)

                statValPaint.color = safeColor(dlsStatColorHex(clamped))
                statValPaint.textSize = if (clamped >= 100) circleRadius * 0.88f else circleRadius * 1.02f
                val textOffset = (statValPaint.descent() + statValPaint.ascent()) / 2f
                canvas.drawText(clamped.toString(), cx, circleCy - textOffset, statValPaint)
            }
        }

        drawStatRow(topRowStats, row1LabelY, row1CircleCy)
        drawStatRow(bottomRowStats, row2LabelY, row2CircleCy)

        // Bottom Footer: Height (cm) + Preferred Foot
        val footerBaseline = footerTop + (h - footerTop) * 0.68f
        val heightValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = h * 0.072f
        }
        val cmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DCE6F0")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = h * 0.048f
        }
        val heightStr = "${spec.heightCm.coerceIn(150, 215)}"
        val startX = w * 0.032f
        canvas.drawText(heightStr, startX, footerBaseline, heightValPaint)
        val cmX = startX + heightValPaint.measureText(heightStr) + 4f
        canvas.drawText("cm", cmX, footerBaseline, cmPaint)
        val footX = cmX + cmPaint.measureText("cm") + w * 0.042f
        canvas.drawText(spec.preferredFoot.label, footX, footerBaseline, heightValPaint)

        // Right-Side Iconic DLS Player Shield Card (overlaps header & footer slightly just like official DLS!)
        val shieldRect = RectF(
            w * 0.578f,
            h * 0.112f,
            w * 0.946f,
            h * 0.892f
        )
        val customPhoto = decodeCustomPhoto(context, spec.customPhotoUri)
        drawDlsShieldCard(canvas, shieldRect, spec, customPhoto)
        customPhoto?.recycle()

        canvas.restore()

        // Outer metallic rim stroke
        canvas.drawRoundRect(
            outerRect,
            cornerRadius,
            cornerRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.parseColor("#738190")
            }
        )
        return bmp
    }

    /**
     * Standalone transparent-background DLS Shield Card (720 x 960 PNG).
     */
    fun renderShieldOnlyBitmap(
        context: Context,
        spec: DlsPlayerCardSpec,
        width: Int = 720,
        height: Int = 960
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val padX = width * 0.07f
        val padY = height * 0.045f
        val rect = RectF(padX, padY, width - padX, height - padY)
        val customPhoto = decodeCustomPhoto(context, spec.customPhotoUri)
        drawDlsShieldCard(canvas, rect, spec, customPhoto)
        customPhoto?.recycle()
        return bmp
    }

    private fun buildDlsShieldPath(rect: RectF): Path {
        val l = rect.left
        val t = rect.top
        val r = rect.right
        val b = rect.bottom
        val w = rect.width()
        val h = rect.height()
        val cx = rect.centerX()

        return Path().apply {
            // Iconic arched top crest + straight sides + curved bottom point
            moveTo(l + w * 0.04f, t + h * 0.105f)
            quadTo(cx, t - h * 0.025f, r - w * 0.04f, t + h * 0.105f)
            quadTo(r, t + h * 0.125f, r, t + h * 0.175f)
            lineTo(r, b - h * 0.155f)
            quadTo(r, b - h * 0.075f, cx + w * 0.16f, b - h * 0.055f)
            quadTo(cx + w * 0.045f, b - h * 0.03f, cx, b)
            quadTo(cx - w * 0.045f, b - h * 0.03f, cx - w * 0.16f, b - h * 0.055f)
            quadTo(l, b - h * 0.075f, l, b - h * 0.155f)
            lineTo(l, t + h * 0.175f)
            quadTo(l, t + h * 0.125f, l + w * 0.04f, t + h * 0.105f)
            close()
        }
    }

    private fun drawDlsShieldCard(
        canvas: Canvas,
        rect: RectF,
        spec: DlsPlayerCardSpec,
        customPhoto: Bitmap?
    ) {
        val w = rect.width()
        val h = rect.height()
        val tier = spec.tier
        val primary = safeColor(tier.primaryHex)
        val secondary = safeColor(tier.secondaryHex)
        val borderCol = safeColor(tier.borderHex)
        val textCol = safeColor(tier.textColorHex)

        val outerPath = buildDlsShieldPath(rect)
        val innerRect = RectF(
            rect.left + w * 0.036f,
            rect.top + h * 0.028f,
            rect.right - w * 0.036f,
            rect.bottom - h * 0.032f
        )
        val innerPath = buildDlsShieldPath(innerRect)

        // Drop shadow under shield
        canvas.drawPath(
            outerPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 0, 0, 0)
                setShadowLayer(18f, 0f, 8f, Color.argb(170, 0, 0, 0))
            }
        )

        // Outer metallic gold/tier bezel frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(borderCol, primary, secondary, borderCol),
                floatArrayOf(0f, 0.32f, 0.72f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(outerPath, framePaint)

        // Clip inside inner shield
        canvas.save()
        canvas.clipPath(innerPath)

        // Rich tier gradient backdrop
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                innerRect.left, innerRect.top, innerRect.right, innerRect.bottom,
                intArrayOf(primary, secondary, primary),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(innerRect, bgPaint)

        // Authentic DLS low-poly faceted crystal shards inside shield
        drawShieldFacetPattern(canvas, innerRect, tier)

        // Portrait zone (upper-right 60% of shield)
        val portraitArea = RectF(
            innerRect.left + innerRect.width() * 0.24f,
            innerRect.top + innerRect.height() * 0.04f,
            innerRect.right - innerRect.width() * 0.02f,
            innerRect.top + innerRect.height() * 0.60f
        )
        if (customPhoto != null) {
            drawCustomPortrait(canvas, portraitArea, customPhoto, spec)
        } else {
            drawStylizedPlayerAvatar(canvas, portraitArea, spec)
        }

        // Clean nameplate band (brighter frosted band behind short name)
        val nameBandTop = innerRect.top + innerRect.height() * 0.575f
        val nameBandBottom = innerRect.top + innerRect.height() * 0.735f
        val bandColor = when (tier) {
            PlayerCardTier.LEGENDARY_GOLD -> Color.parseColor("#FFEC6E")
            PlayerCardTier.MAXED_BLACK -> Color.parseColor("#111620")
            PlayerCardTier.RARE_BLUE -> Color.parseColor("#1D6FE0")
            PlayerCardTier.COMMON_SILVER -> Color.parseColor("#EEF3F8")
            PlayerCardTier.SPECIAL_ICON -> Color.parseColor("#30106B")
        }
        canvas.drawRect(
            innerRect.left,
            nameBandTop,
            innerRect.right,
            nameBandBottom,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bandColor
                alpha = 235
            }
        )
        // Subtle separator line above nameplate
        canvas.drawLine(
            innerRect.left,
            nameBandTop,
            innerRect.right,
            nameBandTop,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(60, 0, 0, 0)
                strokeWidth = 2f
            }
        )

        // Rating Badge Circle (top-left inside shield)
        val ratingR = innerRect.width() * 0.165f
        val ratingCx = innerRect.left + innerRect.width() * 0.225f
        val ratingCy = innerRect.top + innerRect.height() * 0.255f
        val ratingFill = if (spec.ratingBoosted) {
            Color.parseColor("#3598DB")
        } else {
            when (tier) {
                PlayerCardTier.MAXED_BLACK -> Color.parseColor("#1F2937")
                PlayerCardTier.SPECIAL_ICON -> Color.parseColor("#0EA5E9")
                else -> Color.parseColor("#2A83C8")
            }
        }
        val ratingRing = if (spec.ratingBoosted) {
            Color.parseColor("#34E878")
        } else {
            borderCol
        }
        canvas.drawCircle(ratingCx, ratingCy, ratingR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ratingFill })
        canvas.drawCircle(
            ratingCx,
            ratingCy,
            ratingR,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = ratingR * 0.18f
                color = ratingRing
            }
        )
        val ratingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = if (spec.rating >= 100) ratingR * 0.94f else ratingR * 1.12f
            textAlign = Paint.Align.CENTER
        }
        val rOff = (ratingTextPaint.descent() + ratingTextPaint.ascent()) / 2f
        canvas.drawText(spec.rating.coerceIn(40, 100).toString(), ratingCx, ratingCy - rOff, ratingTextPaint)

        // Short Card Name (centered in nameplate)
        val shortName = spec.cardShortName.trim().ifBlank {
            spec.fullName.trim().substringAfterLast(' ').ifBlank { "PLAYER" }
        }.uppercase()
        val shortNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textCol
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = innerRect.height() * 0.082f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.02f
        }
        val maxShortW = innerRect.width() * 0.88f
        while (shortNamePaint.measureText(shortName) > maxShortW && shortNamePaint.textSize > innerRect.height() * 0.045f) {
            shortNamePaint.textSize -= 1.5f
        }
        val nameCenterY = (nameBandTop + nameBandBottom) * 0.5f
        val snOff = (shortNamePaint.descent() + shortNamePaint.ascent()) / 2f
        canvas.drawText(shortName, innerRect.centerX(), nameCenterY - snOff, shortNamePaint)

        // Bottom Info Strip inside Shield: [Country Flag] [Green Stamina Bolt Bar] [Position Tag]
        val rowTop = innerRect.top + innerRect.height() * 0.748f
        val rowHeight = innerRect.height() * 0.098f
        val rowBottom = rowTop + rowHeight
        val padSide = innerRect.width() * 0.075f

        val flagRect = RectF(
            innerRect.left + padSide,
            rowTop,
            innerRect.left + padSide + innerRect.width() * 0.21f,
            rowBottom
        )
        val posRect = RectF(
            innerRect.right - padSide - innerRect.width() * 0.23f,
            rowTop,
            innerRect.right - padSide,
            rowBottom
        )
        val staminaRect = RectF(
            flagRect.right + innerRect.width() * 0.032f,
            rowTop,
            posRect.left - innerRect.width() * 0.032f,
            rowBottom
        )

        // 1. Vector Country Flag with clean white border
        drawVectorFlag(canvas, flagRect, spec.country)
        canvas.drawRect(
            flagRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.4f
                color = Color.WHITE
            }
        )

        // 2. Green Energy / Stamina Bar with Lightning Bolt
        canvas.drawRect(
            staminaRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#183022") }
        )
        val filledW = staminaRect.width() * spec.staminaPercent.coerceIn(0.15f, 1f)
        val staminaFillRect = RectF(staminaRect.left, staminaRect.top, staminaRect.left + filledW, staminaRect.bottom)
        canvas.drawRect(
            staminaFillRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    staminaRect.left, staminaRect.top, staminaRect.right, staminaRect.bottom,
                    Color.parseColor("#2EE660"),
                    Color.parseColor("#14A83F"),
                    Shader.TileMode.CLAMP
                )
            }
        )
        canvas.drawRect(
            staminaRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.2f
                color = Color.argb(180, 255, 255, 255)
            }
        )
        drawLightningBolt(canvas, staminaRect)

        // 3. Position Pill Badge (CF / CM / CB / GK)
        canvas.drawRect(
            posRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = safeColor(spec.position.badgeColorHex) }
        )
        canvas.drawRect(
            posRect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.2f
                color = Color.WHITE
            }
        )
        val posPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = rowHeight * 0.68f
            textAlign = Paint.Align.CENTER
        }
        val posOff = (posPaint.descent() + posPaint.ascent()) / 2f
        canvas.drawText(spec.position.code, posRect.centerX(), posRect.centerY() - posOff, posPaint)

        // Bottom Star Icon
        val starCy = innerRect.top + innerRect.height() * 0.905f
        val starR = innerRect.width() * 0.068f
        val starColor = when (tier) {
            PlayerCardTier.LEGENDARY_GOLD -> Color.parseColor("#C78312")
            PlayerCardTier.MAXED_BLACK -> Color.parseColor("#F7C948")
            PlayerCardTier.RARE_BLUE -> Color.parseColor("#9AD0FF")
            PlayerCardTier.COMMON_SILVER -> Color.parseColor("#64748B")
            PlayerCardTier.SPECIAL_ICON -> Color.parseColor("#00F5D4")
        }
        drawFivePointStar(canvas, innerRect.centerX(), starCy, starR, starColor)

        canvas.restore()

        // Inner bevel stroke & crisp outer highlight
        canvas.drawPath(
            innerPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = Color.argb(110, 0, 0, 0)
            }
        )
        canvas.drawPath(
            outerPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = Color.argb(210, 255, 255, 255)
            }
        )
    }

    private fun drawShieldFacetPattern(canvas: Canvas, rect: RectF, tier: PlayerCardTier) {
        val w = rect.width()
        val h = rect.height()
        val lightCol = when (tier) {
            PlayerCardTier.LEGENDARY_GOLD -> Color.argb(62, 255, 252, 210)
            PlayerCardTier.MAXED_BLACK -> Color.argb(32, 247, 201, 72)
            PlayerCardTier.RARE_BLUE -> Color.argb(46, 180, 226, 255)
            PlayerCardTier.COMMON_SILVER -> Color.argb(55, 255, 255, 255)
            PlayerCardTier.SPECIAL_ICON -> Color.argb(48, 0, 245, 212)
        }
        val darkCol = when (tier) {
            PlayerCardTier.LEGENDARY_GOLD -> Color.argb(38, 210, 125, 0)
            PlayerCardTier.MAXED_BLACK -> Color.argb(55, 0, 0, 0)
            PlayerCardTier.RARE_BLUE -> Color.argb(42, 8, 42, 110)
            PlayerCardTier.COMMON_SILVER -> Color.argb(35, 70, 88, 110)
            PlayerCardTier.SPECIAL_ICON -> Color.argb(55, 20, 0, 60)
        }
        val pLight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lightCol; style = Paint.Style.FILL }
        val pDark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = darkCol; style = Paint.Style.FILL }

        fun tri(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, paint: Paint) {
            val path = Path().apply {
                moveTo(rect.left + x1 * w, rect.top + y1 * h)
                lineTo(rect.left + x2 * w, rect.top + y2 * h)
                lineTo(rect.left + x3 * w, rect.top + y3 * h)
                close()
            }
            canvas.drawPath(path, paint)
        }

        tri(0f, 0.08f, 0.45f, 0f, 0.28f, 0.35f, pLight)
        tri(0.45f, 0f, 1f, 0.12f, 0.62f, 0.32f, pDark)
        tri(0f, 0.35f, 0.28f, 0.35f, 0.12f, 0.62f, pDark)
        tri(0.28f, 0.35f, 0.68f, 0.26f, 0.52f, 0.58f, pLight)
        tri(0.68f, 0.26f, 1f, 0.44f, 0.78f, 0.62f, pLight)
        tri(0f, 0.72f, 0.35f, 0.86f, 0.08f, 1f, pLight)
        tri(0.65f, 0.85f, 1f, 0.72f, 0.92f, 1f, pDark)

        // Soft studio glow behind player portrait
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                rect.left + w * 0.62f,
                rect.top + h * 0.34f,
                w * 0.48f,
                Color.argb(85, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(rect.left + w * 0.62f, rect.top + h * 0.34f, w * 0.48f, glow)
    }

    private fun drawCustomPortrait(
        canvas: Canvas,
        area: RectF,
        bitmap: Bitmap,
        spec: DlsPlayerCardSpec
    ) {
        canvas.save()
        canvas.clipRect(area)
        val scaleBase = max(area.width() / bitmap.width.toFloat(), area.height() / bitmap.height.toFloat())
        val finalScale = scaleBase * spec.photoScale.coerceIn(0.6f, 2.2f)
        val drawW = bitmap.width * finalScale
        val drawH = bitmap.height * finalScale
        val cx = area.centerX() + spec.photoOffsetX * area.width() * 0.35f
        val cy = area.centerY() + spec.photoOffsetY * area.height() * 0.35f
        val dst = RectF(cx - drawW / 2f, cy - drawH / 2f, cx + drawW / 2f, cy + drawH / 2f)
        canvas.drawBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
    }

    /**
     * Clean stylized pro footballer bust silhouette wearing a club-colored jersey
     * (used as default portrait until the user uploads a custom player photo).
     */
    private fun drawStylizedPlayerAvatar(canvas: Canvas, area: RectF, spec: DlsPlayerCardSpec) {
        canvas.save()
        canvas.clipRect(area)

        val cx = area.centerX() + area.width() * 0.04f
        val w = area.width()
        val h = area.height()
        val jerseyCol = safeColor(spec.clubAccentHex, Color.parseColor("#1D4ED8"))
        val darkJersey = Color.rgb(
            (Color.red(jerseyCol) * 0.62f).toInt(),
            (Color.green(jerseyCol) * 0.62f).toInt(),
            (Color.blue(jerseyCol) * 0.62f).toInt()
        )
        val skinTone = Color.parseColor("#D9A679")
        val skinShadow = Color.parseColor("#B68156")
        val hairCol = Color.parseColor("#1B1E26")

        // Shoulders & Jersey Torso
        val torsoPath = Path().apply {
            moveTo(cx - w * 0.46f, area.bottom + h * 0.04f)
            quadTo(cx - w * 0.44f, area.bottom - h * 0.25f, cx - w * 0.16f, area.bottom - h * 0.31f)
            lineTo(cx + w * 0.16f, area.bottom - h * 0.31f)
            quadTo(cx + w * 0.44f, area.bottom - h * 0.25f, cx + w * 0.46f, area.bottom + h * 0.04f)
            close()
        }
        canvas.drawPath(
            torsoPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    cx - w * 0.4f, area.bottom - h * 0.32f,
                    cx + w * 0.4f, area.bottom,
                    jerseyCol, darkJersey, Shader.TileMode.CLAMP
                )
            }
        )
        // Collar V-trim
        val collarPath = Path().apply {
            moveTo(cx - w * 0.14f, area.bottom - h * 0.31f)
            lineTo(cx, area.bottom - h * 0.19f)
            lineTo(cx + w * 0.14f, area.bottom - h * 0.31f)
        }
        canvas.drawPath(
            collarPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = w * 0.042f
                color = Color.WHITE
                strokeJoin = Paint.Join.ROUND
            }
        )

        // Neck
        val neckRect = RectF(
            cx - w * 0.10f,
            area.bottom - h * 0.44f,
            cx + w * 0.10f,
            area.bottom - h * 0.24f
        )
        canvas.drawRoundRect(neckRect, w * 0.05f, w * 0.05f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = skinShadow })

        // Ears
        val headCy = area.top + h * 0.37f
        val headRx = w * 0.21f
        val headRy = h * 0.245f
        canvas.drawOval(
            RectF(cx - headRx - w * 0.045f, headCy - headRy * 0.18f, cx - headRx + w * 0.03f, headCy + headRy * 0.28f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = skinShadow }
        )
        canvas.drawOval(
            RectF(cx + headRx - w * 0.03f, headCy - headRy * 0.18f, cx + headRx + w * 0.045f, headCy + headRy * 0.28f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = skinShadow }
        )

        // Head Oval
        canvas.drawOval(
            RectF(cx - headRx, headCy - headRy, cx + headRx, headCy + headRy),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    cx - headRx, headCy - headRy,
                    cx + headRx, headCy + headRy,
                    skinTone, skinShadow, Shader.TileMode.CLAMP
                )
            }
        )

        // Athletic Hair Fade Crown
        val hairPath = Path().apply {
            moveTo(cx - headRx * 0.96f, headCy - headRy * 0.12f)
            quadTo(cx - headRx * 1.02f, headCy - headRy * 1.08f, cx, headCy - headRy * 1.06f)
            quadTo(cx + headRx * 1.02f, headCy - headRy * 1.08f, cx + headRx * 0.96f, headCy - headRy * 0.12f)
            quadTo(cx + headRx * 0.72f, headCy - headRy * 0.56f, cx, headCy - headRy * 0.52f)
            quadTo(cx - headRx * 0.72f, headCy - headRy * 0.56f, cx - headRx * 0.96f, headCy - headRy * 0.12f)
            close()
        }
        canvas.drawPath(hairPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = hairCol })

        // Subtle minimal facial shading (brows/eyes)
        val featurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 28, 22, 18)
            strokeWidth = w * 0.022f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx - headRx * 0.52f, headCy - headRy * 0.08f, cx - headRx * 0.16f, headCy - headRy * 0.06f, featurePaint)
        canvas.drawLine(cx + headRx * 0.16f, headCy - headRy * 0.06f, cx + headRx * 0.52f, headCy - headRy * 0.08f, featurePaint)
        canvas.drawCircle(cx - headRx * 0.34f, headCy + headRy * 0.06f, w * 0.022f, featurePaint)
        canvas.drawCircle(cx + headRx * 0.34f, headCy + headRy * 0.06f, w * 0.022f, featurePaint)

        // Subtle initials badge on jersey chest
        val initials = spec.cardShortName.take(2).uppercase()
        val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(165, 255, 255, 255)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = w * 0.14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(initials, cx + w * 0.16f, area.bottom - h * 0.06f, monoPaint)

        canvas.restore()
    }

    private fun drawLightningBolt(canvas: Canvas, rect: RectF) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val bw = rect.height() * 0.56f
        val bh = rect.height() * 0.84f
        val bolt = Path().apply {
            moveTo(cx + bw * 0.12f, cy - bh * 0.50f)
            lineTo(cx - bw * 0.36f, cy + bh * 0.04f)
            lineTo(cx - bw * 0.02f, cy + bh * 0.04f)
            lineTo(cx - bw * 0.16f, cy + bh * 0.50f)
            lineTo(cx + bw * 0.38f, cy - bh * 0.06f)
            lineTo(cx + bw * 0.04f, cy - bh * 0.06f)
            close()
        }
        canvas.drawPath(bolt, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0B1116") })
    }

    private fun drawFivePointStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        val innerR = radius * 0.44f
        val path = Path()
        for (i in 0 until 10) {
            val angle = -PI / 2.0 + i * PI / 5.0
            val r = if (i % 2 == 0) radius else innerR
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }

    fun drawVectorFlag(canvas: Canvas, rect: RectF, country: PlayerCountry) {
        canvas.save()
        canvas.clipRect(rect)
        val p1 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = safeColor(country.c1) }
        val p2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = safeColor(country.c2) }
        val p3 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = safeColor(country.c3) }
        val l = rect.left
        val t = rect.top
        val w = rect.width()
        val h = rect.height()

        when (country.layout) {
            FlagLayout.VERTICAL_TRICOLOR -> {
                canvas.drawRect(l, t, l + w / 3f, t + h, p1)
                canvas.drawRect(l + w / 3f, t, l + 2f * w / 3f, t + h, p2)
                canvas.drawRect(l + 2f * w / 3f, t, l + w, t + h, p3)
            }
            FlagLayout.HORIZONTAL_TRICOLOR -> {
                canvas.drawRect(l, t, l + w, t + h / 3f, p1)
                canvas.drawRect(l, t + h / 3f, l + w, t + 2f * h / 3f, p2)
                canvas.drawRect(l, t + 2f * h / 3f, l + w, t + h, p3)
            }
            FlagLayout.HORIZONTAL_BICOLOR -> {
                canvas.drawRect(l, t, l + w, t + h / 2f, p1)
                canvas.drawRect(l, t + h / 2f, l + w, t + h, p2)
            }
            FlagLayout.SPAIN -> {
                canvas.drawRect(l, t, l + w, t + h * 0.25f, p1)
                canvas.drawRect(l, t + h * 0.25f, l + w, t + h * 0.75f, p2)
                canvas.drawRect(l, t + h * 0.75f, l + w, t + h, p3)
                canvas.drawCircle(l + w * 0.30f, t + h * 0.5f, h * 0.13f, p1)
            }
            FlagLayout.ARGENTINA -> {
                canvas.drawRect(l, t, l + w, t + h / 3f, p1)
                canvas.drawRect(l, t + h / 3f, l + w, t + 2f * h / 3f, p2)
                canvas.drawRect(l, t + 2f * h / 3f, l + w, t + h, p1)
                canvas.drawCircle(l + w * 0.5f, t + h * 0.5f, h * 0.12f, p3)
            }
            FlagLayout.BRAZIL -> {
                canvas.drawRect(rect, p1)
                val diamond = Path().apply {
                    moveTo(rect.centerX(), t + h * 0.12f)
                    lineTo(l + w * 0.88f, rect.centerY())
                    lineTo(rect.centerX(), t + h * 0.88f)
                    lineTo(l + w * 0.12f, rect.centerY())
                    close()
                }
                canvas.drawPath(diamond, p2)
                canvas.drawCircle(rect.centerX(), rect.centerY(), h * 0.22f, p3)
            }
            FlagLayout.PORTUGAL -> {
                canvas.drawRect(l, t, l + w * 0.38f, t + h, p1)
                canvas.drawRect(l + w * 0.38f, t, l + w, t + h, p2)
                canvas.drawCircle(l + w * 0.38f, rect.centerY(), h * 0.22f, p3)
            }
            FlagLayout.NORDIC_CROSS -> {
                canvas.drawRect(rect, p1)
                canvas.drawRect(l + w * 0.28f, t, l + w * 0.44f, t + h, p2)
                canvas.drawRect(l, t + h * 0.38f, l + w, t + h * 0.62f, p2)
                if (country == PlayerCountry.NORWAY) {
                    canvas.drawRect(l + w * 0.32f, t, l + w * 0.40f, t + h, p3)
                    canvas.drawRect(l, t + h * 0.44f, l + w, t + h * 0.56f, p3)
                }
            }
            FlagLayout.UNION_JACK -> {
                // England St George's Cross
                canvas.drawRect(rect, p1)
                canvas.drawRect(l + w * 0.42f, t, l + w * 0.58f, t + h, p2)
                canvas.drawRect(l, t + h * 0.38f, l + w, t + h * 0.62f, p2)
            }
            FlagLayout.JAPAN -> {
                canvas.drawRect(rect, p1)
                canvas.drawCircle(rect.centerX(), rect.centerY(), h * 0.28f, p2)
            }
            FlagLayout.SOUTH_KOREA -> {
                canvas.drawRect(rect, p1)
                canvas.drawArc(
                    RectF(rect.centerX() - h * 0.26f, rect.centerY() - h * 0.26f, rect.centerX() + h * 0.26f, rect.centerY() + h * 0.26f),
                    180f, 180f, true, p2
                )
                canvas.drawArc(
                    RectF(rect.centerX() - h * 0.26f, rect.centerY() - h * 0.26f, rect.centerX() + h * 0.26f, rect.centerY() + h * 0.26f),
                    0f, 180f, true, p3
                )
            }
            FlagLayout.USA -> {
                val stripeH = h / 7f
                for (i in 0 until 7) {
                    canvas.drawRect(l, t + i * stripeH, l + w, t + (i + 1) * stripeH, if (i % 2 == 0) p1 else p2)
                }
                canvas.drawRect(l, t, l + w * 0.44f, t + h * 0.56f, p3)
            }
            FlagLayout.MOROCCO -> {
                canvas.drawRect(rect, p1)
                drawFivePointStar(canvas, rect.centerX(), rect.centerY(), h * 0.24f, safeColor(country.c2))
            }
            FlagLayout.CROATIA -> {
                canvas.drawRect(l, t, l + w, t + h / 3f, p1)
                canvas.drawRect(l, t + h / 3f, l + w, t + 2f * h / 3f, p2)
                canvas.drawRect(l, t + 2f * h / 3f, l + w, t + h, p3)
                canvas.drawRect(rect.centerX() - h * 0.14f, rect.centerY() - h * 0.14f, rect.centerX() + h * 0.14f, rect.centerY() + h * 0.14f, p1)
            }
            FlagLayout.SWITZERLAND -> {
                canvas.drawRect(rect, p1)
                canvas.drawRect(rect.centerX() - w * 0.08f, t + h * 0.20f, rect.centerX() + w * 0.08f, t + h * 0.80f, p2)
                canvas.drawRect(l + w * 0.26f, rect.centerY() - h * 0.12f, l + w * 0.74f, rect.centerY() + h * 0.12f, p2)
            }
            FlagLayout.TURKEY -> {
                canvas.drawRect(rect, p1)
                canvas.drawCircle(l + w * 0.42f, rect.centerY(), h * 0.26f, p2)
                canvas.drawCircle(l + w * 0.48f, rect.centerY(), h * 0.21f, p1)
                drawFivePointStar(canvas, l + w * 0.66f, rect.centerY(), h * 0.13f, Color.WHITE)
            }
            FlagLayout.CANADA -> {
                canvas.drawRect(l, t, l + w * 0.26f, t + h, p1)
                canvas.drawRect(l + w * 0.26f, t, l + w * 0.74f, t + h, p2)
                canvas.drawRect(l + w * 0.74f, t, l + w, t + h, p1)
                drawFivePointStar(canvas, rect.centerX(), rect.centerY(), h * 0.22f, safeColor(country.c1))
            }
            FlagLayout.SCOTLAND -> {
                canvas.drawRect(rect, p1)
                val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = safeColor(country.c2)
                    strokeWidth = h * 0.20f
                }
                canvas.drawLine(l, t, l + w, t + h, xPaint)
                canvas.drawLine(l + w, t, l, t + h, xPaint)
            }
            FlagLayout.SENEGAL -> {
                canvas.drawRect(l, t, l + w / 3f, t + h, p1)
                canvas.drawRect(l + w / 3f, t, l + 2f * w / 3f, t + h, p2)
                canvas.drawRect(l + 2f * w / 3f, t, l + w, t + h, p3)
                drawFivePointStar(canvas, rect.centerX(), rect.centerY(), h * 0.17f, safeColor(country.c1))
            }
            FlagLayout.GHANA -> {
                canvas.drawRect(l, t, l + w, t + h / 3f, p1)
                canvas.drawRect(l, t + h / 3f, l + w, t + 2f * h / 3f, p2)
                canvas.drawRect(l, t + 2f * h / 3f, l + w, t + h, p3)
                drawFivePointStar(canvas, rect.centerX(), rect.centerY(), h * 0.15f, Color.BLACK)
            }
            FlagLayout.CAMEROON -> {
                canvas.drawRect(l, t, l + w / 3f, t + h, p1)
                canvas.drawRect(l + w / 3f, t, l + 2f * w / 3f, t + h, p2)
                canvas.drawRect(l + 2f * w / 3f, t, l + w, t + h, p3)
                drawFivePointStar(canvas, rect.centerX(), rect.centerY(), h * 0.17f, safeColor(country.c3))
            }
            FlagLayout.GEORGIA -> {
                canvas.drawRect(rect, p1)
                canvas.drawRect(l + w * 0.42f, t, l + w * 0.58f, t + h, p2)
                canvas.drawRect(l, t + h * 0.38f, l + w, t + h * 0.62f, p2)
            }
        }
        canvas.restore()
    }

    private fun decodeCustomPhoto(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }
}
