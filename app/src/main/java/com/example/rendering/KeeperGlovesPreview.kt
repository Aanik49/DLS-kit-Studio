package com.example.rendering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.parseColorHex

/**
 * Matching Pro Goalkeeper Gloves Preview (Left Backhand + Right Grip Palm)
 * dynamically styled to match the active GK kit colors & accent trim.
 */
@Composable
fun KeeperGlovesPreview(
    spec: JerseyDesignSpec,
    modifier: Modifier = Modifier
) {
    val mainColor = parseColorHex(spec.gkGloveColorHex ?: spec.primaryColorHex)
    val accentColor = parseColorHex(spec.secondaryColorHex)
    val trimColor = parseColorHex(spec.thirdColorHex)

    Canvas(modifier = modifier.aspectRatio(1.45f)) {
        val w = size.width
        val h = size.height
        val gloveW = w * 0.42f
        val gloveH = h * 0.88f

        // Left Glove (Backhand with geometric punch-zone graphics)
        withTransform({
            translate(left = w * 0.04f, top = h * 0.07f)
            rotate(degrees = -7f, pivot = Offset(gloveW * 0.5f, gloveH * 0.65f))
        }) {
            drawSingleKeeperGlove(
                w = gloveW,
                h = gloveH,
                primary = mainColor,
                secondary = accentColor,
                trim = trimColor,
                isPalmView = false
            )
        }

        // Right Glove (Latex Grip Palm with embossed flex channels)
        withTransform({
            translate(left = w * 0.54f, top = h * 0.07f)
            rotate(degrees = 7f, pivot = Offset(gloveW * 0.5f, gloveH * 0.65f))
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(gloveW * 0.5f, gloveH * 0.5f))
        }) {
            drawSingleKeeperGlove(
                w = gloveW,
                h = gloveH,
                primary = mainColor,
                secondary = accentColor,
                trim = trimColor,
                isPalmView = true
            )
        }
    }
}

private fun DrawScope.drawSingleKeeperGlove(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    trim: Color,
    isPalmView: Boolean
) {
    val baseFill = if (isPalmView) Color(0xFFF8FAFC) else primary
    val detailFill = if (isPalmView) primary else secondary

    // 4 anatomical fingers
    val fingerSpecs = listOf(
        Triple(0.18f, 0.14f, 0.36f),
        Triple(0.36f, 0.05f, 0.44f),
        Triple(0.54f, 0.08f, 0.41f),
        Triple(0.71f, 0.17f, 0.33f)
    )
    fingerSpecs.forEach { (fx, fy, fh) ->
        drawRoundRect(
            color = baseFill,
            topLeft = Offset(w * fx, h * fy),
            size = Size(w * 0.16f, h * fh),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Finger gusset / tip grip accent
        drawRoundRect(
            color = detailFill.copy(alpha = if (isPalmView) 0.85f else 0.78f),
            topLeft = Offset(w * (fx + 0.02f), h * (fy + 0.02f)),
            size = Size(w * 0.12f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
    }

    // Angled thumb
    val thumb = Path().apply {
        moveTo(w * 0.22f, h * 0.50f)
        lineTo(w * 0.04f, h * 0.38f)
        quadraticTo(w * 0.0f, h * 0.48f, w * 0.08f, h * 0.58f)
        lineTo(w * 0.24f, h * 0.68f)
        close()
    }
    drawPath(thumb, color = baseFill)
    drawPath(thumb, color = detailFill.copy(alpha = 0.55f), style = Stroke(width = 2f))

    // Main glove body (palm / backhand)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(baseFill, baseFill.copy(alpha = 0.92f))
        ),
        topLeft = Offset(w * 0.18f, h * 0.40f),
        size = Size(w * 0.68f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
    )

    if (!isPalmView) {
        // Backhand punch-zone chevron & neon stripes
        val punch = Path().apply {
            moveTo(w * 0.24f, h * 0.46f)
            lineTo(w * 0.52f, h * 0.57f)
            lineTo(w * 0.80f, h * 0.46f)
            lineTo(w * 0.80f, h * 0.55f)
            lineTo(w * 0.52f, h * 0.66f)
            lineTo(w * 0.24f, h * 0.55f)
            close()
        }
        drawPath(punch, color = secondary)
        drawLine(
            color = trim,
            start = Offset(w * 0.28f, h * 0.69f),
            end = Offset(w * 0.76f, h * 0.69f),
            strokeWidth = 3f
        )
    } else {
        // Latex palm ergonomic flex lines
        drawLine(
            color = secondary.copy(alpha = 0.35f),
            start = Offset(w * 0.26f, h * 0.52f),
            end = Offset(w * 0.78f, h * 0.56f),
            strokeWidth = 2.4f
        )
        drawLine(
            color = primary.copy(alpha = 0.45f),
            start = Offset(w * 0.32f, h * 0.63f),
            end = Offset(w * 0.72f, h * 0.68f),
            strokeWidth = 2.4f
        )
    }

    // Neoprene wrist cuff + lock strap
    drawRoundRect(
        color = secondary,
        topLeft = Offset(w * 0.26f, h * 0.75f),
        size = Size(w * 0.52f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = primary,
        topLeft = Offset(w * 0.23f, h * 0.80f),
        size = Size(w * 0.58f, h * 0.10f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    drawRoundRect(
        color = trim.copy(alpha = 0.85f),
        topLeft = Offset(w * 0.64f, h * 0.82f),
        size = Size(w * 0.13f, h * 0.06f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
}
