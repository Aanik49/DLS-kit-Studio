package com.example.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.LogoBorderStyle
import com.example.data.model.LogoDesignSpec
import com.example.data.model.LogoElementType
import com.example.data.model.LogoFontStyle
import com.example.data.model.LogoLayer
import com.example.data.model.LogoShape
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun InteractiveLogoCanvas(
    spec: LogoDesignSpec,
    selectedLayerId: String?,
    showGuideGrid: Boolean = false,
    onLayerDragged: (layerId: String, dxFraction: Float, dyFraction: Float) -> Unit = { _, _, _ -> },
    onSelectLayer: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val athleticFont = remember {
        try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }
    }
    val sansFont = remember {
        try {
            ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
        } catch (_: Exception) {
            Typeface.SANS_SERIF
        }
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(selectedLayerId, spec.layers) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        val normX = (startOffset.x - w / 2f) / w
                        val normY = (startOffset.y - h / 2f) / h
                        val hit = spec.layers.lastOrNull { layer ->
                            val dx = normX - layer.offsetX
                            val dy = normY - layer.offsetY
                            (dx * dx + dy * dy) < (0.18f * layer.scale) * (0.18f * layer.scale)
                        }
                        if (hit != null) {
                            onSelectLayer(hit.id)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val targetId = selectedLayerId ?: spec.layers.lastOrNull()?.id
                        if (targetId != null) {
                            val w = size.width.toFloat().coerceAtLeast(1f)
                            val h = size.height.toFloat().coerceAtLeast(1f)
                            onLayerDragged(targetId, dragAmount.x / w, dragAmount.y / h)
                        }
                    }
                )
            }
    ) {
        if (showGuideGrid) {
            val gridColor = Color.White.copy(alpha = 0.08f)
            drawLine(gridColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = 2f)
            drawLine(gridColor, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = 2f)
            drawCircle(gridColor, radius = size.minDimension * 0.42f, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        }

        drawLogoToNativeCanvas(
            canvas = drawContext.canvas.nativeCanvas,
            width = size.width,
            height = size.height,
            spec = spec,
            selectedLayerId = selectedLayerId,
            athleticFont = athleticFont,
            sansFont = sansFont
        )
    }
}

object LogoBitmapExporter {
    fun renderLogoBitmap(
        context: Context,
        spec: LogoDesignSpec,
        pixelSize: Int = 512,
        transparentBg: Boolean = true
    ): Bitmap {
        val bmp = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        if (!transparentBg) {
            canvas.drawColor(android.graphics.Color.rgb(8, 14, 30))
        }
        val athleticFont = try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }
        val sansFont = try {
            ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
        } catch (_: Exception) {
            Typeface.SANS_SERIF
        }
        drawLogoToNativeCanvas(
            canvas = canvas,
            width = pixelSize.toFloat(),
            height = pixelSize.toFloat(),
            spec = spec,
            selectedLayerId = null,
            athleticFont = athleticFont,
            sansFont = sansFont
        )
        return bmp
    }
}

fun drawLogoToNativeCanvas(
    canvas: Canvas,
    width: Float,
    height: Float,
    spec: LogoDesignSpec,
    selectedLayerId: String?,
    athleticFont: Typeface?,
    sansFont: Typeface?
) {
    val s = min(width, height)
    val cx = width / 2f
    val cy = height / 2f
    val pad = s * 0.08f
    val crestPath = buildCrestPath(spec.shape, cx, cy, s - pad * 2f)

    val bgPrimary = parseColorHex(spec.bgColorHex).toAndroidColorInt()
    val bgSecondary = parseColorHex(spec.secondaryColorHex).toAndroidColorInt()
    val borderColor = parseColorHex(spec.borderColorHex).toAndroidColorInt()
    val textColor = parseColorHex(spec.textColorHex).toAndroidColorInt()

    // Base shield fill
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    if (spec.useGradient) {
        fillPaint.shader = LinearGradient(
            cx, pad, cx, s - pad,
            bgPrimary, bgSecondary, Shader.TileMode.CLAMP
        )
    } else {
        fillPaint.color = bgPrimary
    }
    canvas.drawPath(crestPath, fillPaint)

    // Clip layers inside crest
    canvas.save()
    canvas.clipPath(crestPath)

    // Subtle specular crest sheen
    val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, s, s * 0.65f,
            android.graphics.Color.argb(45, 255, 255, 255),
            android.graphics.Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawPath(crestPath, sheenPaint)

    // Draw custom layers
    spec.layers.forEach { layer ->
        drawLogoLayer(canvas, layer, cx, cy, s, isSelected = layer.id == selectedLayerId)
    }

    // Club Name, Initials & Est. Year
    val activeTypeface = when (spec.fontStyle) {
        LogoFontStyle.ATHLETIC_CONDENSED -> athleticFont ?: Typeface.DEFAULT_BOLD
        LogoFontStyle.MODERN_SANS -> sansFont ?: Typeface.SANS_SERIF
        LogoFontStyle.CLASSIC_SERIF -> Typeface.SERIF
    }

    if (spec.clubName.isNotBlank()) {
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            textSize = s * 0.088f
            typeface = activeTypeface
            isFakeBoldText = true
            letterSpacing = 0.06f
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.argb(140, 0, 0, 0))
        }
        canvas.drawText(
            spec.clubName.uppercase().take(18),
            cx,
            cy + s * spec.textOffsetY,
            namePaint
        )
    }

    if (spec.initials.isNotBlank() || spec.establishedYear.isNotBlank()) {
        val subText = listOfNotNull(
            spec.initials.takeIf { it.isNotBlank() }?.uppercase(),
            spec.establishedYear.takeIf { it.isNotBlank() }?.let { "EST. $it" }
        ).joinToString(" • ")

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            textAlign = Paint.Align.CENTER
            textSize = s * 0.045f
            typeface = activeTypeface
            isFakeBoldText = true
            letterSpacing = 0.1f
        }
        canvas.drawText(subText, cx, cy + s * (spec.textOffsetY + 0.075f), subPaint)
    }

    canvas.restore()

    // Crest Borders
    if (spec.borderStyle != LogoBorderStyle.NONE) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = s * 0.028f
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(crestPath, borderPaint)

        if (spec.borderStyle == LogoBorderStyle.DOUBLE || spec.borderStyle == LogoBorderStyle.GOLD_TRIM) {
            val innerPath = buildCrestPath(spec.shape, cx, cy, (s - pad * 2f) * 0.90f)
            val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (spec.borderStyle == LogoBorderStyle.GOLD_TRIM) {
                    android.graphics.Color.rgb(255, 224, 102)
                } else {
                    textColor
                }
                alpha = 180
                style = Paint.Style.STROKE
                strokeWidth = s * 0.01f
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(innerPath, innerPaint)
        }
    }
}

private fun buildCrestPath(shape: LogoShape, cx: Float, cy: Float, diameter: Float): Path {
    val r = diameter / 2f
    return Path().apply {
        when (shape) {
            LogoShape.SHIELD -> {
                moveTo(cx - r * 0.88f, cy - r * 0.85f)
                quadTo(cx, cy - r * 0.98f, cx + r * 0.88f, cy - r * 0.85f)
                lineTo(cx + r * 0.86f, cy + r * 0.14f)
                quadTo(cx + r * 0.72f, cy + r * 0.76f, cx, cy + r * 0.98f)
                quadTo(cx - r * 0.72f, cy + r * 0.76f, cx - r * 0.86f, cy + r * 0.14f)
                close()
            }
            LogoShape.CIRCLE -> {
                addCircle(cx, cy, r * 0.92f, Path.Direction.CW)
            }
            LogoShape.OVAL -> {
                addOval(RectF(cx - r * 0.76f, cy - r * 0.95f, cx + r * 0.76f, cy + r * 0.95f), Path.Direction.CW)
            }
            LogoShape.DIAMOND -> {
                moveTo(cx, cy - r * 0.96f)
                lineTo(cx + r * 0.86f, cy)
                lineTo(cx, cy + r * 0.96f)
                lineTo(cx - r * 0.86f, cy)
                close()
            }
            LogoShape.MODERN_BADGE -> {
                moveTo(cx, cy - r * 0.95f)
                lineTo(cx + r * 0.84f, cy - r * 0.48f)
                lineTo(cx + r * 0.76f, cy + r * 0.48f)
                lineTo(cx, cy + r * 0.95f)
                lineTo(cx - r * 0.76f, cy + r * 0.48f)
                lineTo(cx - r * 0.84f, cy - r * 0.48f)
                close()
            }
            LogoShape.CLASSIC_CREST -> {
                moveTo(cx - r * 0.82f, cy - r * 0.86f)
                lineTo(cx - r * 0.35f, cy - r * 0.76f)
                lineTo(cx, cy - r * 0.96f)
                lineTo(cx + r * 0.35f, cy - r * 0.76f)
                lineTo(cx + r * 0.82f, cy - r * 0.86f)
                lineTo(cx + r * 0.80f, cy + r * 0.25f)
                quadTo(cx + r * 0.55f, cy + r * 0.80f, cx, cy + r * 0.98f)
                quadTo(cx - r * 0.55f, cy + r * 0.80f, cx - r * 0.80f, cy + r * 0.25f)
                close()
            }
        }
    }
}

private fun drawLogoLayer(
    canvas: Canvas,
    layer: LogoLayer,
    cx: Float,
    cy: Float,
    canvasSize: Float,
    isSelected: Boolean
) {
    val lx = cx + layer.offsetX * canvasSize
    val ly = cy + layer.offsetY * canvasSize
    val elemSize = canvasSize * 0.26f * layer.scale
    val colorInt = parseColorHex(layer.colorHex).toAndroidColorInt()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorInt
        alpha = (layer.opacity.coerceIn(0.1f, 1f) * 255).toInt()
    }

    canvas.save()
    canvas.translate(lx, ly)
    canvas.rotate(layer.rotationDeg)

    when (layer.type) {
        LogoElementType.STAR -> {
            val star = Path()
            val outer = elemSize * 0.55f
            val inner = outer * 0.42f
            for (i in 0 until 10) {
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val rad = if (i % 2 == 0) outer else inner
                val px = (cos(angle) * rad).toFloat()
                val py = (sin(angle) * rad).toFloat()
                if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
            }
            star.close()
            canvas.drawPath(star, paint)
        }
        LogoElementType.CROWN -> {
            val w = elemSize * 0.95f
            val h = elemSize * 0.65f
            val crown = Path().apply {
                moveTo(-w / 2f, h / 2f)
                lineTo(-w * 0.54f, -h * 0.35f)
                lineTo(-w * 0.22f, h * 0.05f)
                lineTo(0f, -h * 0.55f)
                lineTo(w * 0.22f, h * 0.05f)
                lineTo(w * 0.54f, -h * 0.35f)
                lineTo(w / 2f, h / 2f)
                close()
            }
            canvas.drawPath(crown, paint)
            canvas.drawRect(-w * 0.48f, h * 0.56f, w * 0.48f, h * 0.72f, paint)
        }
        LogoElementType.FOOTBALL -> {
            val r = elemSize * 0.55f
            canvas.drawCircle(0f, 0f, r, paint)
            val seamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                alpha = paint.alpha
                style = Paint.Style.FILL
            }
            val pent = Path()
            for (i in 0 until 5) {
                val a = Math.toRadians((i * 72 - 90).toDouble())
                val px = (cos(a) * r * 0.42f).toFloat()
                val py = (sin(a) * r * 0.42f).toFloat()
                if (i == 0) pent.moveTo(px, py) else pent.lineTo(px, py)
            }
            pent.close()
            canvas.drawPath(pent, seamPaint)
            seamPaint.style = Paint.Style.STROKE
            seamPaint.strokeWidth = r * 0.1f
            canvas.drawCircle(0f, 0f, r * 0.95f, seamPaint)
            for (i in 0 until 5) {
                val a = Math.toRadians((i * 72 - 90).toDouble())
                canvas.drawLine(
                    (cos(a) * r * 0.42f).toFloat(),
                    (sin(a) * r * 0.42f).toFloat(),
                    (cos(a) * r * 0.95f).toFloat(),
                    (sin(a) * r * 0.95f).toFloat(),
                    seamPaint
                )
            }
        }
        LogoElementType.EAGLE -> {
            val s = elemSize * 0.75f
            val eagle = Path().apply {
                moveTo(0f, -s * 0.7f)
                lineTo(s * 0.22f, -s * 0.45f)
                lineTo(s * 1.15f, -s * 0.55f)
                lineTo(s * 0.85f, -s * 0.05f)
                lineTo(s * 0.35f, s * 0.15f)
                lineTo(s * 0.22f, s * 0.72f)
                lineTo(0f, s * 0.55f)
                lineTo(-s * 0.22f, s * 0.72f)
                lineTo(-s * 0.35f, s * 0.15f)
                lineTo(-s * 0.85f, -s * 0.05f)
                lineTo(-s * 1.15f, -s * 0.55f)
                lineTo(-s * 0.22f, -s * 0.45f)
                close()
            }
            canvas.drawPath(eagle, paint)
        }
        LogoElementType.LION -> {
            val r = elemSize * 0.52f
            // Stylized fierce mane crown + crest silhouette
            val mane = Path()
            for (i in 0 until 16) {
                val a = Math.toRadians((i * 22.5).toDouble())
                val rad = if (i % 2 == 0) r * 1.12f else r * 0.72f
                val px = (cos(a) * rad).toFloat()
                val py = (sin(a) * rad).toFloat()
                if (i == 0) mane.moveTo(px, py) else mane.lineTo(px, py)
            }
            mane.close()
            canvas.drawPath(mane, paint)
            val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(120, 10, 15, 30)
            }
            canvas.drawCircle(0f, 0f, r * 0.52f, face)
        }
        LogoElementType.TIGER -> {
            val s = elemSize * 0.65f
            val head = Path().apply {
                moveTo(-s * 0.75f, -s * 0.65f)
                lineTo(-s * 0.35f, -s * 0.45f)
                lineTo(0f, -s * 0.55f)
                lineTo(s * 0.35f, -s * 0.45f)
                lineTo(s * 0.75f, -s * 0.65f)
                lineTo(s * 0.68f, s * 0.25f)
                lineTo(0f, s * 0.82f)
                lineTo(-s * 0.68f, s * 0.25f)
                close()
            }
            canvas.drawPath(head, paint)
            val stripe = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                strokeWidth = s * 0.14f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(-s * 0.35f, -s * 0.15f, -s * 0.1f, -s * 0.05f, stripe)
            canvas.drawLine(s * 0.35f, -s * 0.15f, s * 0.1f, -s * 0.05f, stripe)
            canvas.drawLine(0f, -s * 0.42f, 0f, -s * 0.18f, stripe)
        }
        LogoElementType.WINGS -> {
            val s = elemSize * 0.85f
            val leftWing = Path().apply {
                moveTo(-s * 0.1f, 0f)
                lineTo(-s * 1.1f, -s * 0.55f)
                lineTo(-s * 0.9f, -s * 0.15f)
                lineTo(-s * 0.75f, s * 0.15f)
                lineTo(-s * 0.45f, s * 0.35f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(s * 0.1f, 0f)
                lineTo(s * 1.1f, -s * 0.55f)
                lineTo(s * 0.9f, -s * 0.15f)
                lineTo(s * 0.75f, s * 0.15f)
                lineTo(s * 0.45f, s * 0.35f)
                close()
            }
            canvas.drawPath(leftWing, paint)
            canvas.drawPath(rightWing, paint)
        }
        LogoElementType.STRIPES -> {
            val w = elemSize * 1.8f
            val h = elemSize * 2.4f
            for (i in -2..2) {
                if (i % 2 == 0) {
                    canvas.drawRect(i * (w / 6f) - w * 0.06f, -h / 2f, i * (w / 6f) + w * 0.06f, h / 2f, paint)
                }
            }
        }
        LogoElementType.SHIELD_INNER -> {
            val inner = buildCrestPath(LogoShape.SHIELD, 0f, 0f, elemSize * 1.4f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = elemSize * 0.09f
            canvas.drawPath(inner, paint)
        }
        LogoElementType.LEAVES -> {
            val r = elemSize * 0.88f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = elemSize * 0.12f
            paint.strokeCap = Paint.Cap.ROUND
            canvas.drawArc(RectF(-r, -r, r, r), 130f, 100f, false, paint)
            canvas.drawArc(RectF(-r, -r, r, r), -50f, 100f, false, paint)
        }
        LogoElementType.GEOMETRIC -> {
            val hex = buildCrestPath(LogoShape.MODERN_BADGE, 0f, 0f, elemSize * 1.15f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = elemSize * 0.11f
            canvas.drawPath(hex, paint)
        }
    }

    if (isSelected) {
        val selPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(0, 229, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(-elemSize * 0.7f, -elemSize * 0.7f, elemSize * 0.7f, elemSize * 0.7f, selPaint)
    }

    canvas.restore()
}
