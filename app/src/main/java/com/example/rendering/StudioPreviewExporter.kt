package com.example.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.CollarStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt

object StudioPreviewExporter {

    /**
     * Generates a 1024x1024 high-resolution Front & Back Jersey Showcase PNG
     * (distinct from the 512x512 DLS UV texture sheet).
     */
    fun generateJerseyShowcaseBitmap(context: Context, spec: JerseyDesignSpec): Bitmap {
        val size = 1024
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, size.toFloat(),
                Color.rgb(7, 11, 22),
                Color.rgb(16, 28, 56),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        val athleticFont = try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

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

        // Header badge & club crest
        ProCrestRenderer.drawProCrestAndroid(
            canvas = canvas,
            cx = size / 2f,
            cy = 58f,
            radius = 28f,
            primary = crestPrimary,
            secondary = crestSecondary,
            accent = crestAccent,
            clubName = spec.clubName,
            crestStyleId = spec.crestStyleId
        )

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 48f
            typeface = athleticFont
            isFakeBoldText = true
            letterSpacing = 0.06f
        }
        canvas.drawText(spec.clubName.uppercase(), size / 2f, 134f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 229, 255)
            textAlign = Paint.Align.CENTER
            textSize = 24f
            typeface = athleticFont
            letterSpacing = 0.12f
        }
        canvas.drawText("${spec.slotType.label.uppercase()}  •  PRO DLS 26 SHOWCASE", size / 2f, 170f, subPaint)

        // Draw Tailored Front & Back silhouettes side by side
        drawShowcaseShirt(canvas, 56f, 212f, 428f, 520f, primary, secondary, third, sleeveColor, cuffColor, collarColor, spec, false, athleticFont)
        drawShowcaseShirt(canvas, 540f, 212f, 428f, 520f, primary, secondary, third, sleeveColor, cuffColor, collarColor, spec, true, athleticFont)

        // Footer labels
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textAlign = Paint.Align.CENTER
            textSize = 25f
            typeface = athleticFont
        }
        canvas.drawText("FRONT VIEW", 270f, 790f, tagPaint)
        canvas.drawText("BACK VIEW", 754f, 790f, tagPaint)

        // Full kit palette chips (Shirt Primary/Sec/Accent + Shorts + Socks)
        val chipY = 878f
        val chips = listOf(primary, secondary, third, shortsColor, socksColor)
        chips.forEachIndexed { idx, col ->
            val cx = 312f + idx * 100f
            canvas.drawCircle(cx, chipY, 24f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col })
            canvas.drawCircle(cx, chipY, 24f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })
        }

        return bmp
    }

    private fun drawShowcaseShirt(
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
        canvas.drawRect(RectF(torsoLeft - 10f, top, torsoRight + 10f, top + h), Paint().apply { color = primary })
        drawShowcasePattern(canvas, spec.pattern, secondary, third, torsoLeft, top, torsoRight - torsoLeft, h)
        canvas.restore()

        // Sleeve cuffs
        val cuffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cuffColor
            strokeWidth = w * 0.032f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.BUTT
        }
        canvas.drawLine(sleeveTipL, sleeveTipY, sleeveInL, sleeveInY, cuffPaint)
        canvas.drawLine(sleeveTipR, sleeveTipY, sleeveInR, sleeveInY, cuffPaint)

        // Subtle cylindrical lighting
        val volPaint = Paint().apply {
            shader = LinearGradient(
                left, top, left + w, top,
                intArrayOf(
                    Color.argb(65, 0, 0, 0),
                    Color.argb(0, 0, 0, 0),
                    Color.argb(38, 255, 255, 255),
                    Color.argb(0, 0, 0, 0),
                    Color.argb(65, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(left, top, left + w, top + h), volPaint)
        canvas.restore()

        canvas.drawPath(shirtPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        })

        // Authentic DLS 26 Crew Neck Collar
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
            ProCrestRenderer.drawProCrestAndroid(
                canvas = canvas,
                cx = crestX,
                cy = chestY,
                radius = w * 0.046f,
                primary = crestPrimary,
                secondary = crestSecondary,
                accent = crestAccent,
                clubName = spec.clubName,
                crestStyleId = spec.crestStyleId
            )

            if (spec.sponsorText.isNotBlank()) {
                ProCrestRenderer.drawCrispOutlinedText(
                    canvas = canvas,
                    text = spec.sponsorText.uppercase().take(16),
                    cx = cx,
                    baselineY = top + h * 0.465f,
                    textSizePx = w * 0.052f,
                    fillColor = sponsorColor,
                    typeface = font,
                    letterSpacingEm = 0.07f
                )
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
                    textSizePx = 30f,
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
                    textSizePx = 110f,
                    fillColor = numberColor,
                    typeface = font,
                    letterSpacingEm = 0.02f
                )
            }
        }
    }

    private fun drawShowcasePattern(
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
        val cx = left + w / 2f

        when (pattern) {
            JerseyPattern.SOLID -> Unit
            JerseyPattern.STRIPES -> {
                canvas.drawRect(cx - w * 0.13f, top, cx + w * 0.13f, top + h, thirdPaint)
                canvas.drawRect(cx - w * 0.105f, top, cx + w * 0.105f, top + h, secPaint)
            }
            JerseyPattern.VERTICAL_STRIPES -> {
                val sw = w / 7f
                for (i in 0 until 7) if (i % 2 == 1) {
                    canvas.drawRect(left + i * sw, top, left + (i + 1) * sw, top + h, secPaint)
                }
            }
            JerseyPattern.PINSTRIPES -> {
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; strokeWidth = w * 0.015f }
                val step = w / 11f
                for (i in 1 until 11) {
                    canvas.drawLine(left + i * step, top, left + i * step, top + h, p)
                }
            }
            JerseyPattern.HORIZONTAL_STRIPES,
            JerseyPattern.HOOPS,
            JerseyPattern.GRADIENT_HOOPS -> {
                val rows = 7
                val sh = h / rows
                for (i in 1 until rows) if (i % 2 == 1) {
                    canvas.drawRect(left, top + i * sh, left + w, top + (i + 1) * sh, secPaint)
                }
            }
            JerseyPattern.CHECKS, JerseyPattern.DIAMOND_ARGYLE -> {
                val cell = w / 5f
                val rows = (h / cell).toInt() + 1
                secPaint.alpha = 180
                for (r in 0 until rows) for (c in 0 until 5) if ((r + c) % 2 == 0) {
                    canvas.drawRect(left + c * cell, top + r * cell, left + (c + 1) * cell, top + (r + 1) * cell, secPaint)
                }
            }
            JerseyPattern.HALF_AND_HALF -> canvas.drawRect(cx, top, left + w, top + h, secPaint)
            JerseyPattern.QUARTERS -> {
                canvas.drawRect(cx, top, left + w, top + h * 0.5f, secPaint)
                canvas.drawRect(left, top + h * 0.5f, cx, top + h, secPaint)
            }
            JerseyPattern.CHEST_BAND -> {
                canvas.drawRect(left, top + h * 0.34f, left + w, top + h * 0.56f, thirdPaint)
                canvas.drawRect(left, top + h * 0.365f, left + w, top + h * 0.535f, secPaint)
            }
            JerseyPattern.CHEVRON_V, JerseyPattern.DOUBLE_CHEVRON -> {
                val v = Path().apply {
                    moveTo(left, top + h * 0.21f)
                    lineTo(cx, top + h * 0.43f)
                    lineTo(left + w, top + h * 0.21f)
                    lineTo(left + w, top + h * 0.35f)
                    lineTo(cx, top + h * 0.57f)
                    lineTo(left, top + h * 0.35f)
                    close()
                }
                canvas.drawPath(v, secPaint)
            }
            JerseyPattern.CROSS_ST_GEORGE -> {
                val cw = w * 0.18f
                canvas.drawRect(cx - cw / 2f, top, cx + cw / 2f, top + h, secPaint)
                canvas.drawRect(left, top + h * 0.35f, left + w, top + h * 0.49f, secPaint)
            }
            JerseyPattern.REVERSE_SASH -> {
                val sash = Path().apply {
                    moveTo(left + w * 0.78f, top + h * 0.06f)
                    lineTo(left + w * 1.02f, top + h * 0.06f)
                    lineTo(left + w * 0.20f, top + h * 0.88f)
                    lineTo(left - w * 0.02f, top + h * 0.88f)
                    close()
                }
                canvas.drawPath(sash, secPaint)
            }
            else -> {
                val sash = Path().apply {
                    moveTo(left - w * 0.02f, top + h * 0.06f)
                    lineTo(left + w * 0.20f, top + h * 0.06f)
                    lineTo(left + w * 1.02f, top + h * 0.88f)
                    lineTo(left + w * 0.78f, top + h * 0.88f)
                    close()
                }
                canvas.drawPath(sash, secPaint.apply { alpha = 185 })
            }
        }
    }
}
