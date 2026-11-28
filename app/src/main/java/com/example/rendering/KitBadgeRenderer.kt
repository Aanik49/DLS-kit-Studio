package com.example.rendering

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.ChestBadgeStyle
import com.example.data.model.JerseyPattern
import com.example.data.model.SleevePatchStyle
import com.example.data.model.SponsorStyle
import com.example.data.model.toAndroidColorInt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared vector renderers for:
 *  1) 4 Goalkeeper-specific patterns (Paint Splatter, Prism Shatter, Digi Camo, Sonic Burst)
 *  2) 6 Vector Football Sponsor Styles
 *  3) 4 Competition Sleeve Patches
 *  4) 3 World-Champion / Trophy Chest Badges
 *  5) Captain Armband
 *
 * Rendered identically across Compose live preview, 1080x1920 Matchday Poster,
 * and 512x512 official DLS 26 UV sprite sheets.
 */
object KitBadgeRenderer {

    // =========================================================================
    // 1. Goalkeeper Pattern Pack — Compose DrawScope
    // =========================================================================

    fun DrawScope.drawGoalkeeperPatternCompose(
        pattern: JerseyPattern,
        secondary: Color,
        third: Color,
        left: Float,
        top: Float,
        w: Float,
        h: Float
    ) {
        when (pattern) {
            JerseyPattern.GK_SPLATTER -> {
                val blobs = listOf(
                    Triple(0.18f, 0.18f, 0.11f),
                    Triple(0.78f, 0.24f, 0.13f),
                    Triple(0.42f, 0.36f, 0.09f),
                    Triple(0.84f, 0.56f, 0.12f),
                    Triple(0.22f, 0.62f, 0.14f),
                    Triple(0.58f, 0.78f, 0.11f),
                    Triple(0.14f, 0.86f, 0.08f),
                    Triple(0.74f, 0.88f, 0.09f)
                )
                blobs.forEachIndexed { idx, (fx, fy, fr) ->
                    val bx = left + w * fx
                    val by = top + h * fy
                    val br = w * fr
                    val col = if (idx % 2 == 0) secondary.copy(alpha = 0.68f) else third.copy(alpha = 0.55f)
                    drawCircle(col, radius = br, center = Offset(bx, by))
                    // Paint splash satellites
                    for (k in 0 until 4) {
                        val a = (k * 90f + idx * 33f) * (PI / 180.0)
                        val sx = bx + cos(a).toFloat() * br * 1.45f
                        val sy = by + sin(a).toFloat() * br * 1.35f
                        drawCircle(col, radius = br * 0.28f, center = Offset(sx, sy))
                    }
                }
            }
            JerseyPattern.GK_SHATTER -> {
                val shards = listOf(
                    listOf(0.0f to 0.05f, 0.42f to 0.18f, 0.12f to 0.42f),
                    listOf(0.55f to 0.04f, 1.0f to 0.22f, 0.68f to 0.46f),
                    listOf(0.18f to 0.40f, 0.62f to 0.32f, 0.48f to 0.68f),
                    listOf(0.58f to 0.44f, 1.0f to 0.55f, 0.72f to 0.86f),
                    listOf(0.0f to 0.58f, 0.38f to 0.66f, 0.10f to 0.96f),
                    listOf(0.36f to 0.72f, 0.88f to 0.82f, 0.52f to 1.0f)
                )
                shards.forEachIndexed { idx, pts ->
                    val p = Path().apply {
                        moveTo(left + w * pts[0].first, top + h * pts[0].second)
                        lineTo(left + w * pts[1].first, top + h * pts[1].second)
                        lineTo(left + w * pts[2].first, top + h * pts[2].second)
                        close()
                    }
                    val fill = if (idx % 2 == 0) secondary.copy(alpha = 0.52f) else third.copy(alpha = 0.34f)
                    drawPath(p, color = fill)
                    drawPath(p, color = third.copy(alpha = 0.45f), style = Stroke(width = 1.6f))
                }
            }
            JerseyPattern.GK_CAMO_GRID -> {
                val cols = 6
                val rows = 8
                val cw = w / cols
                val ch = h / rows
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val hash = (r * 7 + c * 13 + r * c * 3) % 5
                        if (hash == 0 || hash == 2) {
                            drawRect(
                                color = secondary.copy(alpha = if (hash == 0) 0.58f else 0.30f),
                                topLeft = Offset(left + c * cw, top + r * ch),
                                size = Size(cw * 1.05f, ch * 1.05f)
                            )
                        } else if (hash == 4) {
                            drawRect(
                                color = third.copy(alpha = 0.28f),
                                topLeft = Offset(left + c * cw, top + r * ch),
                                size = Size(cw * 0.92f, ch * 0.62f)
                            )
                        }
                    }
                }
            }
            JerseyPattern.GK_SONIC_BURST -> {
                val cx = left + w * 0.5f
                val cy = top + h * 0.46f
                for (i in 1..6) {
                    val r = w * (0.14f * i)
                    drawCircle(
                        color = if (i % 2 == 0) secondary.copy(alpha = 0.48f) else third.copy(alpha = 0.32f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.036f)
                    )
                }
                for (k in 0 until 8) {
                    val rad = (k * 45f + 22.5f) * (PI / 180.0)
                    drawLine(
                        color = secondary.copy(alpha = 0.42f),
                        start = Offset(cx + cos(rad).toFloat() * w * 0.12f, cy + sin(rad).toFloat() * w * 0.12f),
                        end = Offset(cx + cos(rad).toFloat() * w * 0.72f, cy + sin(rad).toFloat() * w * 0.72f),
                        strokeWidth = w * 0.022f,
                        cap = StrokeCap.Round
                    )
                }
            }
            else -> Unit
        }
    }

    // =========================================================================
    // 1B. Goalkeeper Pattern Pack — Android Canvas (UV + Poster + Showcase)
    // =========================================================================

    fun drawGoalkeeperPatternAndroid(
        canvas: Canvas,
        pattern: JerseyPattern,
        secondary: Int,
        third: Int,
        left: Float,
        top: Float,
        w: Float,
        h: Float
    ) {
        val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary }
        val thirdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = third }

        when (pattern) {
            JerseyPattern.GK_SPLATTER -> {
                val blobs = listOf(
                    Triple(0.18f, 0.18f, 0.11f),
                    Triple(0.78f, 0.24f, 0.13f),
                    Triple(0.42f, 0.36f, 0.09f),
                    Triple(0.84f, 0.56f, 0.12f),
                    Triple(0.22f, 0.62f, 0.14f),
                    Triple(0.58f, 0.78f, 0.11f),
                    Triple(0.14f, 0.86f, 0.08f),
                    Triple(0.74f, 0.88f, 0.09f)
                )
                blobs.forEachIndexed { idx, (fx, fy, fr) ->
                    val bx = left + w * fx
                    val by = top + h * fy
                    val br = w * fr
                    val p = if (idx % 2 == 0) {
                        secPaint.apply { alpha = 175 }
                    } else {
                        thirdPaint.apply { alpha = 145 }
                    }
                    canvas.drawCircle(bx, by, br, p)
                    for (k in 0 until 4) {
                        val a = (k * 90f + idx * 33f) * (PI / 180.0)
                        canvas.drawCircle(
                            bx + cos(a).toFloat() * br * 1.45f,
                            by + sin(a).toFloat() * br * 1.35f,
                            br * 0.28f,
                            p
                        )
                    }
                }
            }
            JerseyPattern.GK_SHATTER -> {
                val shards = listOf(
                    listOf(0.0f to 0.05f, 0.42f to 0.18f, 0.12f to 0.42f),
                    listOf(0.55f to 0.04f, 1.0f to 0.22f, 0.68f to 0.46f),
                    listOf(0.18f to 0.40f, 0.62f to 0.32f, 0.48f to 0.68f),
                    listOf(0.58f to 0.44f, 1.0f to 0.55f, 0.72f to 0.86f),
                    listOf(0.0f to 0.58f, 0.38f to 0.66f, 0.10f to 0.96f),
                    listOf(0.36f to 0.72f, 0.88f to 0.82f, 0.52f to 1.0f)
                )
                val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = third
                    alpha = 120
                    style = Paint.Style.STROKE
                    strokeWidth = 1.6f
                }
                shards.forEachIndexed { idx, pts ->
                    val path = AndroidPath().apply {
                        moveTo(left + w * pts[0].first, top + h * pts[0].second)
                        lineTo(left + w * pts[1].first, top + h * pts[1].second)
                        lineTo(left + w * pts[2].first, top + h * pts[2].second)
                        close()
                    }
                    val fillP = if (idx % 2 == 0) secPaint.apply { alpha = 138 } else thirdPaint.apply { alpha = 92 }
                    canvas.drawPath(path, fillP)
                    canvas.drawPath(path, strokeP)
                }
            }
            JerseyPattern.GK_CAMO_GRID -> {
                val cols = 6
                val rows = 8
                val cw = w / cols
                val ch = h / rows
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val hash = (r * 7 + c * 13 + r * c * 3) % 5
                        if (hash == 0 || hash == 2) {
                            secPaint.alpha = if (hash == 0) 150 else 82
                            canvas.drawRect(left + c * cw, top + r * ch, left + (c + 1.05f) * cw, top + (r + 1.05f) * ch, secPaint)
                        } else if (hash == 4) {
                            thirdPaint.alpha = 78
                            canvas.drawRect(left + c * cw, top + r * ch, left + (c + 0.92f) * cw, top + (r + 0.62f) * ch, thirdPaint)
                        }
                    }
                }
            }
            JerseyPattern.GK_SONIC_BURST -> {
                val cx = left + w * 0.5f
                val cy = top + h * 0.46f
                val ringP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = w * 0.036f
                }
                for (i in 1..6) {
                    ringP.color = if (i % 2 == 0) secondary else third
                    ringP.alpha = if (i % 2 == 0) 128 else 85
                    canvas.drawCircle(cx, cy, w * (0.14f * i), ringP)
                }
                val rayP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondary
                    alpha = 110
                    strokeWidth = w * 0.022f
                    strokeCap = Paint.Cap.ROUND
                }
                for (k in 0 until 8) {
                    val rad = (k * 45f + 22.5f) * (PI / 180.0)
                    canvas.drawLine(
                        cx + cos(rad).toFloat() * w * 0.12f,
                        cy + sin(rad).toFloat() * w * 0.12f,
                        cx + cos(rad).toFloat() * w * 0.72f,
                        cy + sin(rad).toFloat() * w * 0.72f,
                        rayP
                    )
                }
            }
            else -> Unit
        }
    }

    // =========================================================================
    // 2. Vector Football Sponsor Styles (Android Canvas & Compose wrapper)
    // =========================================================================

    fun drawStyledSponsorAndroid(
        canvas: Canvas,
        text: String,
        style: SponsorStyle,
        cx: Float,
        cy: Float,
        textSizePx: Float,
        fillColor: Int,
        accentColor: Int,
        typeface: Typeface?
    ) {
        val clean = text.trim().uppercase().take(20)
        if (clean.isEmpty()) return

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            this.style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            this.style = Paint.Style.STROKE
            strokeWidth = (textSizePx * 0.10f).coerceAtLeast(1.2f)
        }

        when (style) {
            SponsorStyle.CLASSIC_BOLD -> {
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx,
                    baselineY = cy + textSizePx * 0.34f,
                    textSizePx = textSizePx,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.07f
                )
            }
            SponsorStyle.AERO_WINGS -> {
                // Sleek twin aerodynamic wing emblem above text
                val wSpan = textSizePx * 1.9f
                val wY = cy - textSizePx * 0.42f
                val wings = AndroidPath().apply {
                    moveTo(cx, wY + textSizePx * 0.22f)
                    lineTo(cx - wSpan, wY - textSizePx * 0.24f)
                    lineTo(cx - wSpan * 0.55f, wY + textSizePx * 0.12f)
                    lineTo(cx, wY - textSizePx * 0.05f)
                    lineTo(cx + wSpan * 0.55f, wY + textSizePx * 0.12f)
                    lineTo(cx + wSpan, wY - textSizePx * 0.24f)
                    close()
                }
                canvas.drawPath(wings, iconPaint)
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx,
                    baselineY = cy + textSizePx * 0.72f,
                    textSizePx = textSizePx * 0.88f,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.09f
                )
            }
            SponsorStyle.TECH_PILL -> {
                val estW = (clean.length * textSizePx * 0.58f + textSizePx * 1.4f).coerceAtLeast(textSizePx * 3.8f)
                val pillH = textSizePx * 1.55f
                val rect = RectF(cx - estW / 2f, cy - pillH / 2f, cx + estW / 2f, cy + pillH / 2f)
                canvas.drawRoundRect(rect, pillH * 0.45f, pillH * 0.45f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = fillColor
                    alpha = 38
                })
                canvas.drawRoundRect(rect, pillH * 0.45f, pillH * 0.45f, strokePaint)
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx,
                    baselineY = cy + textSizePx * 0.34f,
                    textSizePx = textSizePx * 0.86f,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.08f
                )
            }
            SponsorStyle.STAR_AIRLINES -> {
                // 3 curved flight arcs + star above sponsor wordmark
                val arcY = cy - textSizePx * 0.38f
                drawFivePointStarAndroid(canvas, cx, arcY - textSizePx * 0.18f, textSizePx * 0.36f, iconPaint)
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = (textSizePx * 0.10f).coerceAtLeast(1.1f)
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(cx - textSizePx * 1.7f, arcY + textSizePx * 0.05f, cx - textSizePx * 0.52f, arcY - textSizePx * 0.15f, lineP)
                canvas.drawLine(cx + textSizePx * 0.52f, arcY - textSizePx * 0.15f, cx + textSizePx * 1.7f, arcY + textSizePx * 0.05f, lineP)
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx,
                    baselineY = cy + textSizePx * 0.72f,
                    textSizePx = textSizePx * 0.88f,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.06f
                )
            }
            SponsorStyle.CROWN_BANK -> {
                // Minimal 3-peak crown + twin underline bars
                val topY = cy - textSizePx * 0.72f
                val cw = textSizePx * 0.95f
                val crown = AndroidPath().apply {
                    moveTo(cx - cw, topY + textSizePx * 0.46f)
                    lineTo(cx - cw * 0.85f, topY)
                    lineTo(cx - cw * 0.32f, topY + textSizePx * 0.25f)
                    lineTo(cx, topY - textSizePx * 0.14f)
                    lineTo(cx + cw * 0.32f, topY + textSizePx * 0.25f)
                    lineTo(cx + cw * 0.85f, topY)
                    lineTo(cx + cw, topY + textSizePx * 0.46f)
                    close()
                }
                canvas.drawPath(crown, iconPaint)
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx,
                    baselineY = cy + textSizePx * 0.62f,
                    textSizePx = textSizePx * 0.86f,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.09f
                )
                val barW = textSizePx * 2.1f
                canvas.drawRect(cx - barW, cy + textSizePx * 0.82f, cx + barW, cy + textSizePx * 0.94f, iconPaint)
            }
            SponsorStyle.BOLT_ENERGY -> {
                // Lightning icon on left + bold wordmark
                val bx = cx - (clean.length * textSizePx * 0.27f) - textSizePx * 0.55f
                val bolt = AndroidPath().apply {
                    moveTo(bx + textSizePx * 0.20f, cy - textSizePx * 0.58f)
                    lineTo(bx - textSizePx * 0.28f, cy + textSizePx * 0.08f)
                    lineTo(bx + textSizePx * 0.06f, cy + textSizePx * 0.08f)
                    lineTo(bx - textSizePx * 0.16f, cy + textSizePx * 0.62f)
                    lineTo(bx + textSizePx * 0.32f, cy - textSizePx * 0.06f)
                    lineTo(bx - textSizePx * 0.02f, cy - textSizePx * 0.06f)
                    close()
                }
                canvas.drawPath(bolt, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor })
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = clean,
                    cx = cx + textSizePx * 0.22f,
                    baselineY = cy + textSizePx * 0.34f,
                    textSizePx = textSizePx * 0.92f,
                    fillColor = fillColor,
                    typeface = typeface,
                    letterSpacingEm = 0.07f
                )
            }
        }
    }

    // =========================================================================
    // 3. World-Champion / Trophy Chest Badge (Android Canvas)
    // =========================================================================

    fun drawChestBadgeAndroid(
        canvas: Canvas,
        badge: ChestBadgeStyle,
        cx: Float,
        cy: Float,
        radius: Float
    ) {
        if (badge == ChestBadgeStyle.NONE) return
        val gold = AndroidColor.parseColor("#EAB308")
        val darkGold = AndroidColor.parseColor("#A16207")
        val cream = AndroidColor.parseColor("#FEF9C3")

        val shield = AndroidPath().apply {
            moveTo(cx, cy - radius * 1.12f)
            lineTo(cx + radius * 0.84f, cy - radius * 0.72f)
            lineTo(cx + radius * 0.76f, cy + radius * 0.35f)
            quadTo(cx + radius * 0.55f, cy + radius * 0.92f, cx, cy + radius * 1.15f)
            quadTo(cx - radius * 0.55f, cy + radius * 0.92f, cx - radius * 0.76f, cy + radius * 0.35f)
            lineTo(cx - radius * 0.84f, cy - radius * 0.72f)
            close()
        }

        val bg = when (badge) {
            ChestBadgeStyle.WORLD_CHAMPION_GOLD -> gold
            ChestBadgeStyle.CLUB_WORLD_SHIELD -> AndroidColor.parseColor("#0F172A")
            ChestBadgeStyle.CONTINENTAL_TROPHY -> AndroidColor.parseColor("#1D4ED8")
            ChestBadgeStyle.NONE -> return
        }
        val fg = when (badge) {
            ChestBadgeStyle.WORLD_CHAMPION_GOLD -> AndroidColor.WHITE
            ChestBadgeStyle.CLUB_WORLD_SHIELD -> gold
            ChestBadgeStyle.CONTINENTAL_TROPHY -> cream
            ChestBadgeStyle.NONE -> return
        }

        canvas.drawPath(shield, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg })
        canvas.drawPath(shield, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (badge == ChestBadgeStyle.WORLD_CHAMPION_GOLD) darkGold else gold
            style = Paint.Style.STROKE
            strokeWidth = (radius * 0.14f).coerceAtLeast(1.2f)
        })

        // Stylized trophy cup silhouette inside badge
        val cup = AndroidPath().apply {
            moveTo(cx - radius * 0.36f, cy - radius * 0.48f)
            lineTo(cx + radius * 0.36f, cy - radius * 0.48f)
            quadTo(cx + radius * 0.28f, cy + radius * 0.14f, cx + radius * 0.08f, cy + radius * 0.24f)
            lineTo(cx + radius * 0.10f, cy + radius * 0.54f)
            lineTo(cx + radius * 0.28f, cy + radius * 0.68f)
            lineTo(cx - radius * 0.28f, cy + radius * 0.68f)
            lineTo(cx - radius * 0.10f, cy + radius * 0.54f)
            lineTo(cx - radius * 0.08f, cy + radius * 0.24f)
            quadTo(cx - radius * 0.28f, cy + radius * 0.14f, cx - radius * 0.36f, cy - radius * 0.48f)
            close()
        }
        canvas.drawPath(cup, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fg })
        drawFivePointStarAndroid(canvas, cx, cy - radius * 0.75f, radius * 0.18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fg })
    }

    // =========================================================================
    // 4. Competition Sleeve Patch (Android Canvas)
    // =========================================================================

    fun drawSleevePatchAndroid(
        canvas: Canvas,
        patch: SleevePatchStyle,
        cx: Float,
        cy: Float,
        radius: Float
    ) {
        if (patch == SleevePatchStyle.NONE) return
        when (patch) {
            SleevePatchStyle.UCL_STARBALL -> {
                canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#0B1D51") })
                canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = (radius * 0.14f).coerceAtLeast(1.1f)
                })
                val starP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE }
                drawFivePointStarAndroid(canvas, cx, cy, radius * 0.34f, starP)
                for (i in 0 until 6) {
                    val a = i * 60.0 * PI / 180.0
                    drawFivePointStarAndroid(
                        canvas,
                        cx + cos(a).toFloat() * radius * 0.60f,
                        cy + sin(a).toFloat() * radius * 0.60f,
                        radius * 0.20f,
                        starP
                    )
                }
            }
            SleevePatchStyle.PREMIER_LION -> {
                canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#3B0764") })
                canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#00FF87")
                    style = Paint.Style.STROKE
                    strokeWidth = (radius * 0.16f).coerceAtLeast(1.2f)
                })
                val crown = AndroidPath().apply {
                    moveTo(cx - radius * 0.52f, cy + radius * 0.34f)
                    lineTo(cx - radius * 0.45f, cy - radius * 0.22f)
                    lineTo(cx - radius * 0.16f, cy + radius * 0.05f)
                    lineTo(cx, cy - radius * 0.44f)
                    lineTo(cx + radius * 0.16f, cy + radius * 0.05f)
                    lineTo(cx + radius * 0.45f, cy - radius * 0.22f)
                    lineTo(cx + radius * 0.52f, cy + radius * 0.34f)
                    close()
                }
                canvas.drawPath(crown, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE })
            }
            SleevePatchStyle.WORLD_GLOBE -> {
                canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#0284C7") })
                val ringP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#FACC15")
                    style = Paint.Style.STROKE
                    strokeWidth = (radius * 0.13f).coerceAtLeast(1.0f)
                }
                canvas.drawCircle(cx, cy, radius * 0.92f, ringP)
                canvas.drawOval(RectF(cx - radius * 0.42f, cy - radius * 0.85f, cx + radius * 0.42f, cy + radius * 0.85f), ringP)
                canvas.drawLine(cx - radius * 0.85f, cy, cx + radius * 0.85f, cy, ringP)
            }
            SleevePatchStyle.SUPER_CUP -> {
                val hex = AndroidPath().apply {
                    for (i in 0 until 6) {
                        val a = (i * 60f - 90f) * (PI / 180.0)
                        val x = cx + cos(a).toFloat() * radius
                        val y = cy + sin(a).toFloat() * radius
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                canvas.drawPath(hex, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#DC2626") })
                canvas.drawPath(hex, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#FACC15")
                    style = Paint.Style.STROKE
                    strokeWidth = (radius * 0.16f).coerceAtLeast(1.1f)
                })
                drawFivePointStarAndroid(canvas, cx, cy, radius * 0.48f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE })
            }
            SleevePatchStyle.NONE -> Unit
        }
    }

    // =========================================================================
    // 5. Captain Armband (Android Canvas + Compose DrawScope)
    // =========================================================================

    fun drawCaptainArmbandAndroid(
        canvas: Canvas,
        rect: RectF,
        bandColor: Int,
        verticalC: Boolean = false
    ) {
        canvas.drawRect(rect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bandColor })
        // Crisp dark contrast trim edges
        val trimColor = contrastInkFor(bandColor)
        val trimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = trimColor; alpha = 210 }
        if (verticalC) {
            canvas.drawRect(rect.left, rect.top, rect.left + 2.2f, rect.bottom, trimPaint)
            canvas.drawRect(rect.right - 2.2f, rect.top, rect.right, rect.bottom, trimPaint)
        } else {
            canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 2.2f, trimPaint)
            canvas.drawRect(rect.left, rect.bottom - 2.2f, rect.right, rect.bottom, trimPaint)
        }
        val cPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trimColor
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            textSize = (minOf(rect.width(), rect.height()) * 0.74f).coerceAtLeast(9f)
        }
        canvas.save()
        canvas.translate(rect.centerX(), rect.centerY())
        if (verticalC) canvas.rotate(-90f)
        canvas.drawText("C", 0f, cPaint.textSize * 0.35f, cPaint)
        canvas.restore()
    }

    fun DrawScope.drawChestBadgeCompose(badge: ChestBadgeStyle, center: Offset, radius: Float) {
        drawChestBadgeAndroid(drawContext.canvas.nativeCanvas, badge, center.x, center.y, radius)
    }

    fun DrawScope.drawSleevePatchCompose(patch: SleevePatchStyle, center: Offset, radius: Float) {
        drawSleevePatchAndroid(drawContext.canvas.nativeCanvas, patch, center.x, center.y, radius)
    }

    private fun drawFivePointStarAndroid(canvas: Canvas, cx: Float, cy: Float, outerR: Float, paint: Paint) {
        val innerR = outerR * 0.42f
        val path = AndroidPath()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * 36.0 - 90.0) * PI / 180.0
            val x = cx + cos(angle).toFloat() * r
            val y = cy + sin(angle).toFloat() * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun contrastInkFor(bgColor: Int): Int {
        val lum = (0.299 * AndroidColor.red(bgColor) + 0.587 * AndroidColor.green(bgColor) + 0.114 * AndroidColor.blue(bgColor)) / 255.0
        return if (lum > 0.62) AndroidColor.parseColor("#0B0D12") else AndroidColor.WHITE
    }
}
