package com.example.rendering

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.ChestBadgeStyle
import com.example.data.model.CollarStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.JerseyTemplateFit
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.SleevePatchStyle
import com.example.data.model.SleeveStyle
import com.example.data.model.parseColorHex
import com.example.data.model.toAndroidColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberUriImageBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uriString) {
        if (uriString.isNullOrBlank()) {
            bitmap = null
        } else {
            bitmap = withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                        BitmapFactory.decodeStream(stream, null, opts)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
    return bitmap
}

@Composable
fun JerseyPreviewCanvas(
    spec: JerseyDesignSpec,
    isBackView: Boolean = false,
    showShortsPreview: Boolean = false,
    rotationAngleDeg: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customCrestBitmap = rememberUriImageBitmap(spec.customClubLogoUri)
    val customSponsorBitmap = rememberUriImageBitmap(spec.customSponsorLogoUri)
    val customStickerBitmap = rememberUriImageBitmap(spec.customStickerUri)
    val athleticTypeface = remember {
        try {
            ResourcesCompat.getFont(context, R.font.barlow_condensed)
        } catch (_: Exception) {
            android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val rawAngle = if (isBackView && rotationAngleDeg == 0f) 180f else rotationAngleDeg
    val normAngle = ((rawAngle % 360f) + 360f) % 360f

    Box(modifier = modifier.aspectRatio(if (showShortsPreview) 0.72f else 0.94f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val jerseyHeight = if (showShortsPreview) h * 0.64f else h * 0.94f
            val topOffset = h * 0.03f

            val isRightSide = normAngle in 45f..135f
            val isLeftSide = normAngle in 225f..315f
            val isBack = normAngle in 135f..225f

            if (isRightSide || isLeftSide) {
                val sideOffset = if (isRightSide) (normAngle - 90f) / 45f else (normAngle - 270f) / 45f
                drawProSideProfileKit(
                    spec = spec,
                    isLeftSide = isLeftSide,
                    tiltFrac = sideOffset.coerceIn(-1f, 1f),
                    top = topOffset,
                    width = w,
                    jerseyHeight = jerseyHeight,
                    fullHeight = h,
                    showShortsPreview = showShortsPreview
                )
            } else {
                // Subtle horizontal perspective foreshortening as user drags toward side angles
                val deltaFromFace = if (isBack) normAngle - 180f else {
                    if (normAngle > 180f) normAngle - 360f else normAngle
                }
                val scaleX = (1f - (kotlin.math.abs(deltaFromFace) / 45f) * 0.16f).coerceIn(0.82f, 1f)
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.scale(scaleX, 1f, w / 2f, h / 2f)

                drawProMatchJersey(
                    spec = spec,
                    isBackView = isBack,
                    top = topOffset,
                    width = w,
                    height = jerseyHeight,
                    customCrestBitmap = customCrestBitmap,
                    customSponsorBitmap = customSponsorBitmap,
                    customStickerBitmap = customStickerBitmap,
                    athleticTypeface = athleticTypeface
                )

                if (showShortsPreview) {
                    drawProShortsAndSocks(
                        spec = spec,
                        isBackView = isBack,
                        top = topOffset + jerseyHeight + h * 0.02f,
                        width = w,
                        height = h * 0.30f
                    )
                }
                drawContext.canvas.nativeCanvas.restore()
            }
        }
    }
}

private fun DrawScope.drawProMatchJersey(
    spec: JerseyDesignSpec,
    isBackView: Boolean,
    top: Float,
    width: Float,
    height: Float,
    customCrestBitmap: ImageBitmap?,
    customSponsorBitmap: ImageBitmap?,
    customStickerBitmap: ImageBitmap?,
    athleticTypeface: android.graphics.Typeface?
) {
    val primary = parseColorHex(spec.primaryColorHex)
    val secondary = parseColorHex(spec.secondaryColorHex)
    val third = parseColorHex(spec.thirdColorHex)
    val gradEnd = parseColorHex(spec.gradientEndHex, primary)
    val collarColor = parseColorHex(spec.collarColorHex)
    val sleeveColor = parseColorHex(spec.sleeveColorHex, primary)
    val cuffColor = parseColorHex(spec.cuffColorHex, secondary)

    val cx = width / 2f
    val waistInset = when (spec.templateFit) {
        JerseyTemplateFit.PRO_AERO -> width * 0.036f
        JerseyTemplateFit.CLASSIC_STADIUM -> width * 0.015f
        JerseyTemplateFit.RETRO_LOOSE -> -width * 0.012f
        JerseyTemplateFit.ELITE_RAGLAN -> width * 0.028f
    }

    val torsoLeft = width * 0.258f
    val torsoRight = width * 0.742f
    val shoulderOuterLeft = width * 0.230f
    val shoulderOuterRight = width * 0.770f
    val neckY = top + height * 0.052f
    val shoulderY = top + height * 0.118f
    val armholeBottomY = top + height * 0.395f
    val hemY = top + height * 0.945f

    // Authentic DLS 26 short-sleeve athletic drape (~30 deg hang)
    val sleeveTipXLeft = width * 0.095f
    val sleeveTipXRight = width * 0.905f
    val sleeveTipY = top + height * 0.415f
    val sleeveInnerXLeft = width * 0.206f
    val sleeveInnerXRight = width * 0.794f
    val sleeveInnerY = top + height * 0.478f

    val fullJerseyPath = Path().apply {
        moveTo(cx - width * 0.114f, neckY)
        cubicTo(
            cx - width * 0.175f, neckY + height * 0.024f,
            shoulderOuterLeft + width * 0.015f, shoulderY * 0.86f,
            shoulderOuterLeft, shoulderY
        )
        // Outer sleeve drape curve (natural downward deltoid slope)
        quadraticTo(
            (shoulderOuterLeft + sleeveTipXLeft) * 0.48f,
            (shoulderY + sleeveTipY) * 0.46f,
            sleeveTipXLeft,
            sleeveTipY
        )
        // Cuff opening edge
        quadraticTo(
            (sleeveTipXLeft + sleeveInnerXLeft) / 2f + width * 0.01f,
            (sleeveTipY + sleeveInnerY) / 2f + height * 0.012f,
            sleeveInnerXLeft,
            sleeveInnerY
        )
        // Under-sleeve seam up into armpit
        quadraticTo(
            torsoLeft - width * 0.018f,
            armholeBottomY + height * 0.032f,
            torsoLeft,
            armholeBottomY
        )
        // Athletic chest -> waist -> hip curve
        cubicTo(
            torsoLeft + waistInset, top + height * 0.56f,
            torsoLeft + waistInset * 0.65f, top + height * 0.78f,
            torsoLeft - width * 0.010f, hemY
        )
        // Tailored curved drop-tail hem
        quadraticTo(cx, hemY + height * 0.036f, torsoRight + width * 0.010f, hemY)
        cubicTo(
            torsoRight - waistInset * 0.65f, top + height * 0.78f,
            torsoRight - waistInset, top + height * 0.56f,
            torsoRight, armholeBottomY
        )
        quadraticTo(
            torsoRight + width * 0.018f,
            armholeBottomY + height * 0.032f,
            sleeveInnerXRight,
            sleeveInnerY
        )
        quadraticTo(
            (sleeveTipXRight + sleeveInnerXRight) / 2f - width * 0.01f,
            (sleeveTipY + sleeveInnerY) / 2f + height * 0.012f,
            sleeveTipXRight,
            sleeveTipY
        )
        quadraticTo(
            (shoulderOuterRight + sleeveTipXRight) * 0.52f + width * 0.008f,
            (shoulderY + sleeveTipY) * 0.46f,
            shoulderOuterRight,
            shoulderY
        )
        cubicTo(
            shoulderOuterRight - width * 0.015f, shoulderY * 0.86f,
            cx + width * 0.175f, neckY + height * 0.024f,
            cx + width * 0.114f, neckY
        )
        quadraticTo(cx, neckY + height * 0.055f, cx - width * 0.114f, neckY)
        close()
    }

    val torsoPath = Path().apply {
        moveTo(cx - width * 0.114f, neckY)
        cubicTo(
            cx - width * 0.175f, neckY + height * 0.024f,
            shoulderOuterLeft + width * 0.015f, shoulderY * 0.86f,
            torsoLeft + width * 0.004f, shoulderY + height * 0.02f
        )
        quadraticTo(torsoLeft + width * 0.012f, armholeBottomY * 0.72f, torsoLeft, armholeBottomY)
        cubicTo(
            torsoLeft + waistInset, top + height * 0.56f,
            torsoLeft + waistInset * 0.65f, top + height * 0.78f,
            torsoLeft - width * 0.010f, hemY
        )
        quadraticTo(cx, hemY + height * 0.036f, torsoRight + width * 0.010f, hemY)
        cubicTo(
            torsoRight - waistInset * 0.65f, top + height * 0.78f,
            torsoRight - waistInset, top + height * 0.56f,
            torsoRight, armholeBottomY
        )
        quadraticTo(torsoRight - width * 0.012f, armholeBottomY * 0.72f, torsoRight - width * 0.004f, shoulderY + height * 0.02f)
        cubicTo(
            shoulderOuterRight - width * 0.015f, shoulderY * 0.86f,
            cx + width * 0.175f, neckY + height * 0.024f,
            cx + width * 0.114f, neckY
        )
        close()
    }

    // Soft layered studio shadow behind shirt
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.translate(0f, height * 0.018f)
    drawPath(path = fullJerseyPath, color = Color.Black.copy(alpha = 0.24f))
    drawContext.canvas.nativeCanvas.restore()

    clipPath(fullJerseyPath) {
        // Sleeve fill with gentle shoulder-to-cuff gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(sleeveColor, sleeveColor.copy(alpha = 0.90f)),
                startY = top,
                endY = hemY
            ),
            topLeft = Offset(0f, top),
            size = Size(width, height)
        )

        // Torso fill + pattern
        clipPath(torsoPath) {
            val baseBrush = if (spec.useGradient) {
                Brush.verticalGradient(listOf(primary, gradEnd), startY = top, endY = hemY)
            } else {
                Brush.verticalGradient(listOf(primary, primary))
            }
            drawRect(brush = baseBrush, topLeft = Offset(torsoLeft - 24f, top), size = Size(width * 0.56f, height))

            drawJerseyPattern(
                pattern = spec.pattern,
                secondary = secondary,
                third = third,
                left = torsoLeft - width * 0.02f,
                top = top,
                w = (torsoRight - torsoLeft) + width * 0.04f,
                h = height
            )
        }

        // Authentic tailored armhole seam stitching
        val seamColor = Color.Black.copy(alpha = 0.20f)
        val seamHighlight = Color.White.copy(alpha = 0.06f)
        if (spec.templateFit == JerseyTemplateFit.ELITE_RAGLAN) {
            drawLine(seamColor, Offset(cx - width * 0.105f, neckY + height * 0.02f), Offset(torsoLeft, armholeBottomY), width * 0.005f)
            drawLine(seamColor, Offset(cx + width * 0.105f, neckY + height * 0.02f), Offset(torsoRight, armholeBottomY), width * 0.005f)
        } else {
            val leftSeam = Path().apply {
                moveTo(torsoLeft + width * 0.004f, shoulderY + height * 0.02f)
                quadraticTo(torsoLeft + width * 0.012f, armholeBottomY * 0.72f, torsoLeft, armholeBottomY)
            }
            val rightSeam = Path().apply {
                moveTo(torsoRight - width * 0.004f, shoulderY + height * 0.02f)
                quadraticTo(torsoRight - width * 0.012f, armholeBottomY * 0.72f, torsoRight, armholeBottomY)
            }
            drawPath(leftSeam, seamColor, style = Stroke(width * 0.005f))
            drawPath(rightSeam, seamColor, style = Stroke(width * 0.005f))
            drawPath(leftSeam, seamHighlight, style = Stroke(width * 0.002f))
        }

        // Subtle underarm fold shadows for realistic cloth drape
        drawLine(
            brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent)),
            start = Offset(torsoLeft, armholeBottomY),
            end = Offset(torsoLeft + waistInset * 0.7f, top + height * 0.60f),
            strokeWidth = width * 0.020f
        )
        drawLine(
            brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent)),
            start = Offset(torsoRight, armholeBottomY),
            end = Offset(torsoRight - waistInset * 0.7f, top + height * 0.60f),
            strokeWidth = width * 0.020f
        )

        // Shoulder Patterns
        drawShoulderPattern(
            pattern = spec.shoulderPattern,
            accent = secondary,
            third = third,
            cx = cx,
            neckY = neckY,
            shoulderY = shoulderY,
            sleeveTipXLeft = sleeveTipXLeft,
            sleeveTipXRight = sleeveTipXRight,
            sleeveTipY = sleeveTipY,
            width = width,
            height = height
        )

        // Realistic double-layered knitted sleeve cuffs
        val cuffStroke = width * 0.032f
        drawLine(cuffColor, Offset(sleeveTipXLeft, sleeveTipY), Offset(sleeveInnerXLeft, sleeveInnerY), cuffStroke, StrokeCap.Butt)
        drawLine(cuffColor, Offset(sleeveTipXRight, sleeveTipY), Offset(sleeveInnerXRight, sleeveInnerY), cuffStroke, StrokeCap.Butt)
        drawLine(Color.Black.copy(alpha = 0.18f), Offset(sleeveTipXLeft, sleeveTipY), Offset(sleeveInnerXLeft, sleeveInnerY), width * 0.005f, StrokeCap.Butt)
        drawLine(Color.Black.copy(alpha = 0.18f), Offset(sleeveTipXRight, sleeveTipY), Offset(sleeveInnerXRight, sleeveInnerY), width * 0.005f, StrokeCap.Butt)

        // Tailored bottom hem stitch + authentic jock tag badge
        val hemBandPath = Path().apply {
            moveTo(torsoLeft - width * 0.01f, hemY - height * 0.022f)
            quadraticTo(cx, hemY + height * 0.012f, torsoRight + width * 0.01f, hemY - height * 0.022f)
            lineTo(torsoRight + width * 0.01f, hemY + height * 0.04f)
            lineTo(torsoLeft - width * 0.01f, hemY + height * 0.04f)
            close()
        }
        drawPath(hemBandPath, color = Color.Black.copy(alpha = 0.10f))
        if (!isBackView) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.70f),
                topLeft = Offset(torsoRight - width * 0.082f, hemY - height * 0.048f),
                size = Size(width * 0.046f, height * 0.022f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
        }

        // Side panels
        drawSidePanels(
            sidePattern = spec.sidePattern,
            accent = secondary,
            third = third,
            torsoLeft = torsoLeft,
            torsoRight = torsoRight,
            armholeBottomY = armholeBottomY,
            hemY = hemY,
            width = width
        )

        // Ultra-subtle breathable micro-mesh & true-sRGB cylindrical depth shading
        // Kept minimal so preview colors match DLS 26 in-game colors 1:1
        drawRect(
            brush = Brush.horizontalGradient(
                0.00f to Color.Black.copy(alpha = 0.14f),
                0.18f to Color.Black.copy(alpha = 0.04f),
                0.50f to Color.White.copy(alpha = 0.05f),
                0.82f to Color.Black.copy(alpha = 0.04f),
                1.00f to Color.Black.copy(alpha = 0.14f)
            ),
            topLeft = Offset(0f, top),
            size = Size(width, height)
        )
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = 0.03f),
                0.35f to Color.Transparent,
                0.88f to Color.Black.copy(alpha = 0.06f),
                1.0f to Color.Black.copy(alpha = 0.12f)
            ),
            topLeft = Offset(0f, top),
            size = Size(width, height)
        )
    }

    // Crisp fine silhouette seam outline
    drawPath(
        path = fullJerseyPath,
        color = Color.Black.copy(alpha = 0.36f),
        style = Stroke(width = width * 0.0045f, join = StrokeJoin.Round)
    )

    // Authentic DLS 26 Crew Neck Collar
    drawCollar(
        collarColor = collarColor,
        accent = secondary,
        cx = cx,
        neckY = neckY,
        width = width,
        height = height,
        isBackView = isBackView
    )

    if (!isBackView) {
        drawFrontBranding(
            spec = spec,
            cx = cx,
            top = top,
            width = width,
            height = height,
            primary = primary,
            secondary = secondary,
            third = third,
            customCrestBitmap = customCrestBitmap,
            customSponsorBitmap = customSponsorBitmap,
            customStickerBitmap = customStickerBitmap,
            athleticTypeface = athleticTypeface
        )
    } else {
        drawBackPrint(
            spec = spec,
            cx = cx,
            top = top,
            width = width,
            height = height,
            primary = primary,
            secondary = secondary,
            third = third,
            athleticTypeface = athleticTypeface
        )
    }
}

private fun DrawScope.drawJerseyPattern(
    pattern: JerseyPattern,
    secondary: Color,
    third: Color,
    left: Float,
    top: Float,
    w: Float,
    h: Float
) {
    when (pattern) {
        JerseyPattern.SOLID -> Unit
        JerseyPattern.STRIPES -> {
            val bandW = w * 0.24f
            val bx = left + (w - bandW) / 2f
            drawRect(color = third, topLeft = Offset(bx - w * 0.025f, top), size = Size(bandW + w * 0.05f, h))
            drawRect(color = secondary, topLeft = Offset(bx, top), size = Size(bandW, h))
        }
        JerseyPattern.VERTICAL_STRIPES -> {
            val count = 7
            val stripeW = w / count
            for (i in 0 until count) {
                if (i % 2 == 1) {
                    drawRect(color = secondary, topLeft = Offset(left + i * stripeW, top), size = Size(stripeW, h))
                }
            }
        }
        JerseyPattern.HORIZONTAL_STRIPES -> {
            val count = 10
            val stripeH = h / count
            for (i in 0 until count) {
                if (i % 2 == 1) {
                    drawRect(color = secondary.copy(alpha = 0.88f), topLeft = Offset(left, top + i * stripeH), size = Size(w, stripeH * 0.45f))
                }
            }
        }
        JerseyPattern.HOOPS -> {
            val count = 7
            val hoopH = h / count
            for (i in 0 until count) {
                if (i % 2 == 1) {
                    drawRect(color = secondary, topLeft = Offset(left, top + i * hoopH), size = Size(w, hoopH))
                }
            }
        }
        JerseyPattern.CHECKS -> {
            val cols = 6
            val cell = w / cols
            val rows = (h / cell).toInt() + 1
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if ((r + c) % 2 == 0) {
                        drawRect(
                            color = secondary.copy(alpha = 0.72f),
                            topLeft = Offset(left + c * cell, top + r * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }
        JerseyPattern.GEOMETRIC -> {
            val step = w * 0.16f
            var y = top
            var row = 0
            while (y < top + h) {
                var x = left - step * 0.5f + (if (row % 2 == 0) 0f else step * 0.5f)
                while (x < left + w + step) {
                    val diamond = Path().apply {
                        moveTo(x + step * 0.5f, y)
                        lineTo(x + step, y + step * 0.55f)
                        lineTo(x + step * 0.5f, y + step * 1.1f)
                        lineTo(x, y + step * 0.55f)
                        close()
                    }
                    drawPath(diamond, color = secondary.copy(alpha = 0.22f))
                    drawPath(diamond, color = third.copy(alpha = 0.14f), style = Stroke(width = 1.5f))
                    x += step
                }
                y += step * 0.78f
                row++
            }
        }
        JerseyPattern.ZIGZAG -> {
            val bands = 8
            val bandH = h / bands
            for (i in 0 until bands) {
                val baseY = top + i * bandH
                val zig = Path().apply {
                    moveTo(left, baseY)
                    val peaks = 6
                    val pw = w / peaks
                    for (p in 0..peaks) {
                        lineTo(left + p * pw, baseY + (if (p % 2 == 0) 0f else bandH * 0.55f))
                    }
                    lineTo(left + w, baseY + bandH * 0.45f)
                    for (p in peaks downTo 0) {
                        lineTo(left + p * pw, baseY + bandH * 0.35f + (if (p % 2 == 0) 0f else bandH * 0.55f))
                    }
                    close()
                }
                drawPath(zig, color = if (i % 2 == 0) secondary.copy(alpha = 0.36f) else third.copy(alpha = 0.15f))
            }
        }
        JerseyPattern.ABSTRACT_MODERN -> {
            val sash = Path().apply {
                moveTo(left, top + h * 0.78f)
                lineTo(left + w, top + h * 0.16f)
                lineTo(left + w, top + h * 0.36f)
                lineTo(left, top + h * 0.98f)
                close()
            }
            drawPath(sash, color = secondary.copy(alpha = 0.34f))
            val shard = Path().apply {
                moveTo(left + w * 0.15f, top + h * 0.10f)
                lineTo(left + w * 0.92f, top + h * 0.52f)
                lineTo(left + w * 0.65f, top + h * 0.62f)
                close()
            }
            drawPath(shard, color = third.copy(alpha = 0.16f))
        }
        JerseyPattern.PINSTRIPES -> {
            val count = 11
            val step = w / count
            for (i in 1 until count) {
                val x = left + i * step
                drawLine(secondary.copy(alpha = 0.85f), Offset(x, top), Offset(x, top + h), strokeWidth = w * 0.014f)
                drawLine(third.copy(alpha = 0.45f), Offset(x + w * 0.018f, top), Offset(x + w * 0.018f, top + h), strokeWidth = w * 0.007f)
            }
        }
        JerseyPattern.HALF_AND_HALF -> {
            drawRect(color = secondary, topLeft = Offset(left + w * 0.5f, top), size = Size(w * 0.52f, h))
            drawLine(third.copy(alpha = 0.65f), Offset(left + w * 0.5f, top), Offset(left + w * 0.5f, top + h), strokeWidth = w * 0.014f)
        }
        JerseyPattern.QUARTERS -> {
            val midX = left + w * 0.5f
            val midY = top + h * 0.50f
            drawRect(color = secondary, topLeft = Offset(midX, top), size = Size(w * 0.52f, h * 0.50f))
            drawRect(color = secondary, topLeft = Offset(left, midY), size = Size(w * 0.50f, h * 0.52f))
            drawLine(third.copy(alpha = 0.45f), Offset(midX, top), Offset(midX, top + h), strokeWidth = w * 0.01f)
            drawLine(third.copy(alpha = 0.45f), Offset(left, midY), Offset(left + w, midY), strokeWidth = w * 0.01f)
        }
        JerseyPattern.CHEST_SASH -> {
            val borderSash = Path().apply {
                moveTo(left - w * 0.05f, top + h * 0.06f)
                lineTo(left + w * 0.24f, top + h * 0.06f)
                lineTo(left + w * 1.05f, top + h * 0.88f)
                lineTo(left + w * 0.74f, top + h * 0.88f)
                close()
            }
            drawPath(borderSash, color = third.copy(alpha = 0.9f))
            val sash = Path().apply {
                moveTo(left - w * 0.02f, top + h * 0.06f)
                lineTo(left + w * 0.19f, top + h * 0.06f)
                lineTo(left + w * 1.02f, top + h * 0.88f)
                lineTo(left + w * 0.79f, top + h * 0.88f)
                close()
            }
            drawPath(sash, color = secondary)
        }
        JerseyPattern.REVERSE_SASH -> {
            val borderSash = Path().apply {
                moveTo(left + w * 0.74f, top + h * 0.06f)
                lineTo(left + w * 1.04f, top + h * 0.06f)
                lineTo(left + w * 0.24f, top + h * 0.88f)
                lineTo(left - w * 0.04f, top + h * 0.88f)
                close()
            }
            drawPath(borderSash, color = third.copy(alpha = 0.88f))
            val sash = Path().apply {
                moveTo(left + w * 0.79f, top + h * 0.06f)
                lineTo(left + w * 1.01f, top + h * 0.06f)
                lineTo(left + w * 0.19f, top + h * 0.88f)
                lineTo(left - w * 0.01f, top + h * 0.88f)
                close()
            }
            drawPath(sash, color = secondary)
        }
        JerseyPattern.CHEST_BAND -> {
            drawRect(color = third, topLeft = Offset(left, top + h * 0.34f), size = Size(w, h * 0.22f))
            drawRect(color = secondary, topLeft = Offset(left, top + h * 0.365f), size = Size(w, h * 0.17f))
        }
        JerseyPattern.CHEVRON_V -> {
            val vOuter = Path().apply {
                moveTo(left, top + h * 0.21f)
                lineTo(left + w * 0.5f, top + h * 0.43f)
                lineTo(left + w, top + h * 0.21f)
                lineTo(left + w, top + h * 0.35f)
                lineTo(left + w * 0.5f, top + h * 0.57f)
                lineTo(left, top + h * 0.35f)
                close()
            }
            drawPath(vOuter, color = secondary)
            val vTrim = Path().apply {
                moveTo(left, top + h * 0.35f)
                lineTo(left + w * 0.5f, top + h * 0.57f)
                lineTo(left + w, top + h * 0.35f)
                lineTo(left + w, top + h * 0.385f)
                lineTo(left + w * 0.5f, top + h * 0.605f)
                lineTo(left, top + h * 0.385f)
                close()
            }
            drawPath(vTrim, color = third.copy(alpha = 0.85f))
        }
        JerseyPattern.DOUBLE_CHEVRON -> {
            listOf(0.20f to secondary, 0.36f to third).forEach { (startFrac, col) ->
                val vPath = Path().apply {
                    moveTo(left, top + h * startFrac)
                    lineTo(left + w * 0.5f, top + h * (startFrac + 0.16f))
                    lineTo(left + w, top + h * startFrac)
                    lineTo(left + w, top + h * (startFrac + 0.09f))
                    lineTo(left + w * 0.5f, top + h * (startFrac + 0.25f))
                    lineTo(left, top + h * (startFrac + 0.09f))
                    close()
                }
                drawPath(vPath, color = col.copy(alpha = 0.9f))
            }
        }
        JerseyPattern.GRADIENT_HOOPS -> {
            val rows = 8
            val rowH = h / rows
            for (i in 1 until rows) {
                val frac = i / rows.toFloat()
                val bandH = rowH * (0.20f + frac * 0.65f)
                drawRect(
                    color = secondary.copy(alpha = 0.35f + frac * 0.55f),
                    topLeft = Offset(left, top + i * rowH),
                    size = Size(w, bandH)
                )
            }
        }
        JerseyPattern.CROSS_ST_GEORGE -> {
            val crossW = w * 0.18f
            val crossH = h * 0.14f
            drawRect(color = secondary, topLeft = Offset(left + (w - crossW) / 2f, top), size = Size(crossW, h))
            drawRect(color = secondary, topLeft = Offset(left, top + h * 0.35f), size = Size(w, crossH))
            drawRect(color = third.copy(alpha = 0.5f), topLeft = Offset(left + (w - crossW * 0.22f) / 2f, top), size = Size(crossW * 0.22f, h))
        }
        JerseyPattern.DIAMOND_ARGYLE -> {
            val cols = 4
            val cw = w / cols
            val ch = cw * 1.15f
            val rows = (h / ch).toInt() + 1
            for (r in 0..rows) {
                for (c in 0 until cols) {
                    val cx = left + c * cw + cw * 0.5f
                    val cy = top + r * ch + ch * 0.5f
                    val d = Path().apply {
                        moveTo(cx, cy - ch * 0.46f)
                        lineTo(cx + cw * 0.46f, cy)
                        lineTo(cx, cy + ch * 0.46f)
                        lineTo(cx - cw * 0.46f, cy)
                        close()
                    }
                    drawPath(d, color = if ((r + c) % 2 == 0) secondary.copy(alpha = 0.62f) else third.copy(alpha = 0.28f))
                }
            }
        }
        JerseyPattern.LIGHTNING_BOLT -> {
            listOf(0.18f, 0.48f, 0.76f).forEachIndexed { idx, xFrac ->
                val x0 = left + w * xFrac
                val bolt = Path().apply {
                    moveTo(x0 + w * 0.08f, top + h * 0.05f)
                    lineTo(x0 - w * 0.06f, top + h * 0.44f)
                    lineTo(x0 + w * 0.05f, top + h * 0.44f)
                    lineTo(x0 - w * 0.09f, top + h * 0.92f)
                    lineTo(x0 + w * 0.14f, top + h * 0.36f)
                    lineTo(x0 + w * 0.03f, top + h * 0.36f)
                    close()
                }
                drawPath(bolt, color = if (idx == 1) third.copy(alpha = 0.65f) else secondary.copy(alpha = 0.55f))
            }
        }
        JerseyPattern.BRUSH_SLASH -> {
            for (i in 0..4) {
                val yOff = top + h * (0.14f + i * 0.16f)
                val slash = Path().apply {
                    moveTo(left - w * 0.05f, yOff + h * 0.12f)
                    lineTo(left + w * 1.05f, yOff - h * 0.08f)
                    lineTo(left + w * 1.05f, yOff - h * 0.01f)
                    lineTo(left - w * 0.05f, yOff + h * 0.19f)
                    close()
                }
                drawPath(slash, color = if (i % 2 == 0) secondary.copy(alpha = 0.48f) else third.copy(alpha = 0.28f))
            }
        }
        JerseyPattern.WAVE_RIPPLE -> {
            val bands = 7
            val bh = h / bands
            for (i in 0 until bands) {
                val baseY = top + i * bh
                val wave = Path().apply {
                    moveTo(left, baseY + bh * 0.3f)
                    cubicTo(left + w * 0.33f, baseY - bh * 0.25f, left + w * 0.66f, baseY + bh * 0.85f, left + w, baseY + bh * 0.3f)
                    lineTo(left + w, baseY + bh * 0.65f)
                    cubicTo(left + w * 0.66f, baseY + bh * 1.2f, left + w * 0.33f, baseY + bh * 0.1f, left, baseY + bh * 0.65f)
                    close()
                }
                drawPath(wave, color = if (i % 2 == 0) secondary.copy(alpha = 0.42f) else third.copy(alpha = 0.20f))
            }
        }
        JerseyPattern.HEX_HONEYCOMB -> {
            val step = w * 0.18f
            var y = top
            var row = 0
            while (y < top + h + step) {
                var x = left + if (row % 2 == 0) 0f else step * 0.5f
                while (x < left + w + step) {
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
                    drawPath(hex, color = secondary.copy(alpha = 0.18f))
                    drawPath(hex, color = third.copy(alpha = 0.22f), style = Stroke(width = 1.4f))
                    x += step
                }
                y += step * 0.82f
                row++
            }
        }
        JerseyPattern.GK_SPLATTER,
        JerseyPattern.GK_SHATTER,
        JerseyPattern.GK_CAMO_GRID,
        JerseyPattern.GK_SONIC_BURST -> {
            KitBadgeRenderer.run {
                drawGoalkeeperPatternCompose(pattern, secondary, third, left, top, w, h)
            }
        }
    }
}

private fun DrawScope.drawShoulderPattern(
    pattern: ShoulderPattern,
    accent: Color,
    third: Color,
    cx: Float,
    neckY: Float,
    shoulderY: Float,
    sleeveTipXLeft: Float,
    sleeveTipXRight: Float,
    sleeveTipY: Float,
    width: Float,
    height: Float
) {
    when (pattern) {
        ShoulderPattern.NONE -> Unit
        ShoulderPattern.THREE_STRIPES -> {
            for (i in 0..2) {
                val offset = i * width * 0.013f
                drawLine(accent, Offset(cx - width * 0.13f, neckY + height * 0.02f + offset), Offset(sleeveTipXLeft + width * 0.04f, sleeveTipY - width * 0.05f + offset), width * 0.007f)
                drawLine(accent, Offset(cx + width * 0.13f, neckY + height * 0.02f + offset), Offset(sleeveTipXRight - width * 0.04f, sleeveTipY - width * 0.05f + offset), width * 0.007f)
            }
        }
        ShoulderPattern.CHEVRON_BAND -> {
            drawLine(accent, Offset(cx - width * 0.22f, shoulderY + width * 0.02f), Offset(sleeveTipXLeft + width * 0.03f, sleeveTipY - width * 0.03f), width * 0.022f)
            drawLine(third, Offset(cx + width * 0.22f, shoulderY + width * 0.02f), Offset(sleeveTipXRight - width * 0.03f, sleeveTipY - width * 0.03f), width * 0.022f)
        }
        ShoulderPattern.PIPING -> {
            drawLine(accent, Offset(cx - width * 0.11f, neckY + width * 0.01f), Offset(sleeveTipXLeft + width * 0.02f, sleeveTipY - width * 0.02f), width * 0.007f)
            drawLine(accent, Offset(cx + width * 0.11f, neckY + width * 0.01f), Offset(sleeveTipXRight - width * 0.02f, sleeveTipY - width * 0.02f), width * 0.007f)
        }
    }
}

private fun DrawScope.drawSidePanels(
    sidePattern: SidePattern,
    accent: Color,
    third: Color,
    torsoLeft: Float,
    torsoRight: Float,
    armholeBottomY: Float,
    hemY: Float,
    width: Float
) {
    val panelW = width * 0.022f
    when (sidePattern) {
        SidePattern.NONE -> Unit
        SidePattern.MESH_STRIPE -> {
            drawRect(accent.copy(alpha = 0.85f), Offset(torsoLeft, armholeBottomY), Size(panelW, hemY - armholeBottomY))
            drawRect(accent.copy(alpha = 0.85f), Offset(torsoRight - panelW, armholeBottomY), Size(panelW, hemY - armholeBottomY))
        }
        SidePattern.VENT_PANEL -> {
            drawRect(third.copy(alpha = 0.55f), Offset(torsoLeft, armholeBottomY + (hemY - armholeBottomY) * 0.2f), Size(panelW * 1.3f, (hemY - armholeBottomY) * 0.8f))
            drawRect(third.copy(alpha = 0.55f), Offset(torsoRight - panelW * 1.3f, armholeBottomY + (hemY - armholeBottomY) * 0.2f), Size(panelW * 1.3f, (hemY - armholeBottomY) * 0.8f))
        }
        SidePattern.DIAGONAL_TRIM -> {
            val leftTrim = Path().apply {
                moveTo(torsoLeft, hemY - (hemY - armholeBottomY) * 0.35f)
                lineTo(torsoLeft + panelW * 2.2f, hemY)
                lineTo(torsoLeft, hemY)
                close()
            }
            val rightTrim = Path().apply {
                moveTo(torsoRight, hemY - (hemY - armholeBottomY) * 0.35f)
                lineTo(torsoRight - panelW * 2.2f, hemY)
                lineTo(torsoRight, hemY)
                close()
            }
            drawPath(leftTrim, color = accent)
            drawPath(rightTrim, color = accent)
        }
    }
}

private fun DrawScope.drawCollar(
    collarColor: Color,
    accent: Color,
    cx: Float,
    neckY: Float,
    width: Float,
    height: Float,
    isBackView: Boolean
) {
    val neckW = width * 0.112f
    val collarStroke = width * 0.024f
    val neckDepth = if (isBackView) height * 0.042f else height * 0.072f

    // Realistic inside back-of-neck shadow + subtle neck tape
    val neckHole = Path().apply {
        moveTo(cx - neckW, neckY)
        quadraticTo(cx, neckY + neckDepth, cx + neckW, neckY)
        quadraticTo(cx, neckY - height * 0.015f, cx - neckW, neckY)
        close()
    }
    drawPath(neckHole, color = Color(0xFF0A0E17))
    drawLine(
        color = accent.copy(alpha = 0.65f),
        start = Offset(cx - neckW * 0.55f, neckY + height * 0.015f),
        end = Offset(cx + neckW * 0.55f, neckY + height * 0.015f),
        strokeWidth = width * 0.008f
    )

    // Authentic DLS 26 Crew Neck Collar Rim
    val rim = Path().apply {
        moveTo(cx - neckW, neckY)
        quadraticTo(cx, neckY + neckDepth + height * 0.015f, cx + neckW, neckY)
    }
    drawPath(rim, color = collarColor, style = Stroke(width = collarStroke, cap = StrokeCap.Round))
    drawPath(rim, color = accent.copy(alpha = 0.45f), style = Stroke(width = collarStroke * 0.24f, cap = StrokeCap.Round))
}

private fun DrawScope.drawFrontBranding(
    spec: JerseyDesignSpec,
    cx: Float,
    top: Float,
    width: Float,
    height: Float,
    primary: Color,
    secondary: Color,
    third: Color,
    customCrestBitmap: ImageBitmap?,
    customSponsorBitmap: ImageBitmap?,
    customStickerBitmap: ImageBitmap?,
    athleticTypeface: android.graphics.Typeface?
) {
    val autoContrast = computeReadableTextColor(primary, secondary, third)
    val brandColor = spec.manufacturerColorHex?.let { parseColorHex(it, autoContrast) } ?: autoContrast
    val sponsorColor = spec.sponsorColorHex?.let { parseColorHex(it, autoContrast) } ?: autoContrast
    val crestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it, primary) } ?: primary
    val crestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it, secondary) } ?: secondary
    val crestAccent = spec.crestAccentColorHex?.let { parseColorHex(it, third) } ?: third

    // Competition Sleeve Patch (Right Sleeve) & Captain Armband (Left Sleeve)
    if (spec.sleevePatch != SleevePatchStyle.NONE) {
        KitBadgeRenderer.run {
            drawSleevePatchCompose(
                patch = spec.sleevePatch,
                center = Offset(width * 0.165f, top + height * 0.34f),
                radius = width * 0.032f
            )
        }
    }
    if (spec.captainArmbandEnabled) {
        val bandCol = parseColorHex(spec.captainArmbandColorHex).toAndroidColorInt()
        KitBadgeRenderer.drawCaptainArmbandAndroid(
            canvas = drawContext.canvas.nativeCanvas,
            rect = android.graphics.RectF(
                width * 0.785f,
                top + height * 0.32f,
                width * 0.902f,
                top + height * 0.39f
            ),
            bandColor = bandCol,
            verticalC = false
        )
    }

    val chestY = top + height * 0.262f
    val brandX = when (spec.crestPlacement) {
        CrestPlacement.LEFT_CHEST -> cx - width * 0.118f
        CrestPlacement.CENTER_CHEST -> cx - width * 0.145f
        CrestPlacement.RIGHT_CHEST -> cx + width * 0.118f
    }
    ProCrestRenderer.run {
        drawCrispManufacturerCompose(spec.manufacturer, brandColor, Offset(brandX, chestY), width * 0.056f)
    }

    val crestX = when (spec.crestPlacement) {
        CrestPlacement.LEFT_CHEST -> cx + width * 0.118f
        CrestPlacement.CENTER_CHEST -> cx
        CrestPlacement.RIGHT_CHEST -> cx - width * 0.118f
    }
    val crestSize = width * 0.068f
    if (customCrestBitmap != null) {
        drawImage(
            image = customCrestBitmap,
            dstOffset = IntOffset((crestX - crestSize / 2f).toInt(), (chestY - crestSize / 2f).toInt()),
            dstSize = IntSize(crestSize.toInt(), crestSize.toInt())
        )
    } else {
        ProCrestRenderer.run {
            drawProCrestCompose(
                center = Offset(crestX, chestY),
                radius = crestSize * 0.48f,
                primary = crestPrimary,
                secondary = crestSecondary,
                accent = crestAccent,
                clubName = spec.clubName,
                crestStyleId = spec.crestStyleId
            )
        }
    }

    // World Champion / Trophy Chest Patch
    if (spec.chestBadge != ChestBadgeStyle.NONE) {
        val badgeX = if (spec.crestPlacement == CrestPlacement.CENTER_CHEST) cx + width * 0.135f else cx
        KitBadgeRenderer.run {
            drawChestBadgeCompose(
                badge = spec.chestBadge,
                center = Offset(badgeX, chestY - height * 0.006f),
                radius = width * 0.028f
            )
        }
    }

    val sponsorY = top + height * 0.465f
    if (customSponsorBitmap != null) {
        val scale = spec.customSponsorScale.coerceIn(0.4f, 1.8f)
        val spW = width * 0.30f * scale
        val spH = height * 0.12f * scale
        val sx = cx + spec.customSponsorOffsetX * width * 0.22f
        val sy = sponsorY + spec.customSponsorOffsetY * height * 0.22f
        drawImage(
            image = customSponsorBitmap,
            dstOffset = IntOffset((sx - spW / 2f).toInt(), (sy - spH / 2f).toInt()),
            dstSize = IntSize(spW.toInt().coerceAtLeast(1), spH.toInt().coerceAtLeast(1))
        )
    } else if (spec.sponsorText.isNotBlank()) {
        KitBadgeRenderer.drawStyledSponsorAndroid(
            canvas = drawContext.canvas.nativeCanvas,
            text = spec.sponsorText,
            style = spec.sponsorStyle,
            cx = cx,
            cy = sponsorY - height * 0.014f,
            textSizePx = width * 0.050f,
            fillColor = sponsorColor.toAndroidColorInt(),
            accentColor = third.toAndroidColorInt(),
            typeface = athleticTypeface
        )
    }

    if (spec.customText.isNotBlank()) {
        ProCrestRenderer.drawCrispOutlinedText(
            canvas = drawContext.canvas.nativeCanvas,
            text = spec.customText.uppercase().take(20),
            cx = cx,
            baselineY = top + height * 0.562f,
            textSizePx = width * 0.029f,
            fillColor = sponsorColor.toAndroidColorInt(),
            typeface = athleticTypeface,
            letterSpacingEm = 0.10f
        )
    }

    // Custom PNG Sticker Overlay (with live X, Y, Scale controls)
    if (customStickerBitmap != null) {
        val stScale = spec.customStickerScale.coerceIn(0.4f, 1.8f)
        val stSize = width * 0.16f * stScale
        val stX = cx + spec.customStickerOffsetX * width * 0.22f
        val stY = top + height * (0.48f + spec.customStickerOffsetY * 0.34f)
        drawImage(
            image = customStickerBitmap,
            dstOffset = IntOffset((stX - stSize / 2f).toInt(), (stY - stSize / 2f).toInt()),
            dstSize = IntSize(stSize.toInt().coerceAtLeast(1), stSize.toInt().coerceAtLeast(1))
        )
    }
}

private fun DrawScope.drawBackPrint(
    spec: JerseyDesignSpec,
    cx: Float,
    top: Float,
    width: Float,
    height: Float,
    primary: Color,
    secondary: Color,
    third: Color,
    athleticTypeface: android.graphics.Typeface?
) {
    if (spec.cleanBackNumberBox) {
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx - width * 0.17f, top + height * 0.16f),
            size = Size(width * 0.34f, height * 0.62f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }

    val autoContrast = computeReadableTextColor(primary, secondary, third)
    val numColor = spec.playerNumberColorHex?.let { parseColorHex(it, autoContrast) } ?: autoContrast
    val numColorInt = numColor.toAndroidColorInt()

    if (spec.playerName.isNotBlank()) {
        ProCrestRenderer.drawCrispOutlinedText(
            canvas = drawContext.canvas.nativeCanvas,
            text = spec.playerName.uppercase().take(14),
            cx = cx,
            baselineY = top + height * 0.25f,
            textSizePx = width * 0.066f,
            fillColor = numColorInt,
            typeface = athleticTypeface,
            letterSpacingEm = 0.08f
        )
    }

    if (spec.playerNumber.isNotBlank()) {
        ProCrestRenderer.drawCrispOutlinedText(
            canvas = drawContext.canvas.nativeCanvas,
            text = spec.playerNumber.take(2),
            cx = cx,
            baselineY = top + height * 0.62f,
            textSizePx = width * 0.25f,
            fillColor = numColorInt,
            typeface = athleticTypeface,
            letterSpacingEm = 0.02f
        )
    }
}

/**
 * Tailored Side-Profile Kit View (Left Side 270° & Right Side 90°)
 * Showcases shoulder 3-stripes/chevron/piping, draped sleeve & cuff, side torso panels
 * (mesh stripe, vent panel, diagonal trim), outer shorts hip stripe, and knee sock profile.
 */
private fun DrawScope.drawProSideProfileKit(
    spec: JerseyDesignSpec,
    isLeftSide: Boolean,
    tiltFrac: Float,
    top: Float,
    width: Float,
    jerseyHeight: Float,
    fullHeight: Float,
    showShortsPreview: Boolean
) {
    val primary = parseColorHex(spec.primaryColorHex)
    val secondary = parseColorHex(spec.secondaryColorHex)
    val third = parseColorHex(spec.thirdColorHex)
    val gradEnd = parseColorHex(spec.gradientEndHex, primary)
    val collarColor = parseColorHex(spec.collarColorHex)
    val sleeveColor = parseColorHex(spec.sleeveColorHex, primary)
    val cuffColor = parseColorHex(spec.cuffColorHex, secondary)
    val shortsColor = parseColorHex(spec.shortsColorHex, primary)
    val socksColor = parseColorHex(spec.socksColorHex, primary)
    val shortsTrim = spec.shortsTrimColorHex?.let { parseColorHex(it, secondary) } ?: secondary
    val socksTrim = spec.socksTrimColorHex?.let { parseColorHex(it, secondary) } ?: secondary
    val autoContrast = computeReadableTextColor(socksColor, socksTrim, third)
    val brandColor = spec.manufacturerColorHex?.let { parseColorHex(it, autoContrast) } ?: autoContrast

    val cx = width / 2f + tiltFrac * width * 0.03f
    val dir = if (isLeftSide) -1f else 1f // Direction front chest faces
    val neckY = top + jerseyHeight * 0.06f
    val shoulderTopY = top + jerseyHeight * 0.11f
    val hemY = top + jerseyHeight * 0.94f
    val torsoHalfW = width * 0.205f

    // Side-profile athletic torso silhouette (contoured chest forward, curved athletic back)
    val sideTorsoPath = Path().apply {
        moveTo(cx - dir * width * 0.06f, neckY)
        cubicTo(
            cx + dir * torsoHalfW * 0.72f, neckY + jerseyHeight * 0.04f,
            cx + dir * torsoHalfW * 1.12f, top + jerseyHeight * 0.28f,
            cx + dir * torsoHalfW * 0.96f, top + jerseyHeight * 0.56f
        )
        quadraticTo(
            cx + dir * torsoHalfW * 0.88f, top + jerseyHeight * 0.78f,
            cx + dir * torsoHalfW * 0.98f, hemY
        )
        quadraticTo(cx, hemY + jerseyHeight * 0.03f, cx - dir * torsoHalfW * 0.95f, hemY - jerseyHeight * 0.01f)
        cubicTo(
            cx - dir * torsoHalfW * 0.84f, top + jerseyHeight * 0.72f,
            cx - dir * torsoHalfW * 1.05f, top + jerseyHeight * 0.36f,
            cx - dir * width * 0.08f, neckY + jerseyHeight * 0.02f
        )
        close()
    }

    // Soft studio shadow
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.translate(0f, jerseyHeight * 0.018f)
    drawPath(sideTorsoPath, Color.Black.copy(alpha = 0.20f))
    drawContext.canvas.nativeCanvas.restore()

    clipPath(sideTorsoPath) {
        val baseBrush = if (spec.useGradient) {
            Brush.verticalGradient(listOf(primary, gradEnd), startY = top, endY = hemY)
        } else {
            Brush.verticalGradient(listOf(primary, primary))
        }
        drawRect(baseBrush, Offset(0f, top), Size(width, jerseyHeight))

        drawJerseyPattern(
            pattern = spec.pattern,
            secondary = secondary,
            third = third,
            left = cx - torsoHalfW * 1.2f,
            top = top,
            w = torsoHalfW * 2.4f,
            h = jerseyHeight
        )

        // Prominent Side Panel running right down the center of the side profile!
        val panelTopY = top + jerseyHeight * 0.42f
        val panelW = width * 0.088f
        when (spec.sidePattern) {
            SidePattern.NONE -> {
                // Subtle side stitch seam line
                drawLine(
                    Color.Black.copy(alpha = 0.22f),
                    Offset(cx, panelTopY),
                    Offset(cx, hemY),
                    strokeWidth = width * 0.005f
                )
            }
            SidePattern.MESH_STRIPE -> {
                drawRect(secondary, Offset(cx - panelW / 2f, panelTopY), Size(panelW, hemY - panelTopY))
                drawRect(third.copy(alpha = 0.45f), Offset(cx - panelW * 0.14f, panelTopY), Size(panelW * 0.28f, hemY - panelTopY))
                // Breathable mesh micro-dots
                var py = panelTopY + 8f
                while (py < hemY - 6f) {
                    drawCircle(Color.Black.copy(alpha = 0.16f), radius = 1.6f, center = Offset(cx - panelW * 0.22f, py))
                    drawCircle(Color.Black.copy(alpha = 0.16f), radius = 1.6f, center = Offset(cx + panelW * 0.22f, py))
                    py += 9f
                }
            }
            SidePattern.VENT_PANEL -> {
                val ventW = panelW * 1.35f
                drawRect(third.copy(alpha = 0.80f), Offset(cx - ventW / 2f, panelTopY + jerseyHeight * 0.06f), Size(ventW, (hemY - panelTopY) * 0.94f))
                drawRect(secondary.copy(alpha = 0.75f), Offset(cx - ventW / 2f, panelTopY + jerseyHeight * 0.06f), Size(ventW * 0.18f, (hemY - panelTopY) * 0.94f))
                drawRect(secondary.copy(alpha = 0.75f), Offset(cx + ventW * 0.32f, panelTopY + jerseyHeight * 0.06f), Size(ventW * 0.18f, (hemY - panelTopY) * 0.94f))
            }
            SidePattern.DIAGONAL_TRIM -> {
                val slash = Path().apply {
                    moveTo(cx - panelW * 0.95f, hemY)
                    lineTo(cx + panelW * 0.95f, hemY)
                    lineTo(cx + dir * panelW * 0.65f, panelTopY + (hemY - panelTopY) * 0.32f)
                    lineTo(cx - dir * panelW * 0.35f, panelTopY + (hemY - panelTopY) * 0.56f)
                    close()
                }
                drawPath(slash, secondary)
                drawLine(third, Offset(cx - panelW * 0.6f, hemY), Offset(cx + dir * panelW * 0.5f, panelTopY + (hemY - panelTopY) * 0.38f), width * 0.009f)
            }
        }

        // Subtle curved volume lighting
        drawRect(
            brush = Brush.horizontalGradient(
                0.0f to Color.Black.copy(alpha = 0.14f),
                0.5f to Color.White.copy(alpha = 0.06f),
                1.0f to Color.Black.copy(alpha = 0.14f)
            ),
            topLeft = Offset(cx - torsoHalfW * 1.2f, top),
            size = Size(torsoHalfW * 2.4f, jerseyHeight)
        )
    }
    drawPath(sideTorsoPath, Color.Black.copy(alpha = 0.35f), style = Stroke(width * 0.0045f, join = StrokeJoin.Round))

    // Draped Side-Profile Sleeve overlaying upper torso (clearly showing Shoulder Pattern + Cuff!)
    val sleeveBottomY = top + jerseyHeight * 0.495f
    val sleeveHalfW = width * 0.135f
    val sideSleevePath = Path().apply {
        moveTo(cx - sleeveHalfW * 0.75f, shoulderTopY)
        quadraticTo(cx, neckY + jerseyHeight * 0.02f, cx + sleeveHalfW * 0.75f, shoulderTopY)
        cubicTo(
            cx + sleeveHalfW * 1.08f, top + jerseyHeight * 0.24f,
            cx + sleeveHalfW * 1.02f, top + jerseyHeight * 0.38f,
            cx + sleeveHalfW * 0.88f, sleeveBottomY
        )
        quadraticTo(cx, sleeveBottomY + jerseyHeight * 0.032f, cx - sleeveHalfW * 0.88f, sleeveBottomY)
        cubicTo(
            cx - sleeveHalfW * 1.02f, top + jerseyHeight * 0.38f,
            cx - sleeveHalfW * 1.08f, top + jerseyHeight * 0.24f,
            cx - sleeveHalfW * 0.75f, shoulderTopY
        )
        close()
    }

    clipPath(sideSleevePath) {
        drawRect(sleeveColor, Offset(cx - sleeveHalfW * 1.2f, neckY), Size(sleeveHalfW * 2.4f, sleeveBottomY - neckY + 20f))

        // Vivid Shoulder Pattern running continuously from collar down the sleeve to cuff!
        when (spec.shoulderPattern) {
            ShoulderPattern.NONE -> Unit
            ShoulderPattern.THREE_STRIPES -> {
                val stripeW = width * 0.017f
                val gap = width * 0.028f
                for (i in -1..1) {
                    drawRect(
                        color = secondary,
                        topLeft = Offset(cx + i * gap - stripeW / 2f, neckY),
                        size = Size(stripeW, sleeveBottomY - neckY - jerseyHeight * 0.03f)
                    )
                }
            }
            ShoulderPattern.CHEVRON_BAND -> {
                val band = Path().apply {
                    moveTo(cx - sleeveHalfW * 1.1f, top + jerseyHeight * 0.24f)
                    lineTo(cx, top + jerseyHeight * 0.33f)
                    lineTo(cx + sleeveHalfW * 1.1f, top + jerseyHeight * 0.24f)
                    lineTo(cx + sleeveHalfW * 1.1f, top + jerseyHeight * 0.31f)
                    lineTo(cx, top + jerseyHeight * 0.40f)
                    lineTo(cx - sleeveHalfW * 1.1f, top + jerseyHeight * 0.31f)
                    close()
                }
                drawPath(band, third)
                drawRect(secondary, Offset(cx - width * 0.025f, neckY), Size(width * 0.05f, jerseyHeight * 0.25f))
            }
            ShoulderPattern.PIPING -> {
                drawRect(secondary, Offset(cx - width * 0.012f, neckY), Size(width * 0.024f, sleeveBottomY - neckY))
            }
        }

        // Knitted sleeve cuff band at bottom of draped side sleeve
        val cuffH = jerseyHeight * 0.058f
        drawRect(cuffColor, Offset(cx - sleeveHalfW * 1.2f, sleeveBottomY - cuffH * 0.75f), Size(sleeveHalfW * 2.4f, cuffH * 1.4f))
        drawLine(secondary, Offset(cx - sleeveHalfW, sleeveBottomY - cuffH * 0.78f), Offset(cx + sleeveHalfW, sleeveBottomY - cuffH * 0.78f), width * 0.007f)

        // Side-profile Sleeve Competition Patch (Right side) & Captain Armband (Left side)
        if (!isLeftSide && spec.sleevePatch != SleevePatchStyle.NONE) {
            KitBadgeRenderer.run {
                drawSleevePatchCompose(
                    patch = spec.sleevePatch,
                    center = Offset(cx, top + jerseyHeight * 0.34f),
                    radius = width * 0.044f
                )
            }
        }
        if (isLeftSide && spec.captainArmbandEnabled) {
            val bandCol = parseColorHex(spec.captainArmbandColorHex).toAndroidColorInt()
            KitBadgeRenderer.drawCaptainArmbandAndroid(
                canvas = drawContext.canvas.nativeCanvas,
                rect = android.graphics.RectF(
                    cx - sleeveHalfW * 1.05f,
                    top + jerseyHeight * 0.31f,
                    cx + sleeveHalfW * 1.05f,
                    top + jerseyHeight * 0.39f
                ),
                bandColor = bandCol,
                verticalC = false
            )
        }

        // Sleeve cylindrical highlight
        drawRect(
            brush = Brush.horizontalGradient(
                0.0f to Color.Black.copy(alpha = 0.14f),
                0.5f to Color.White.copy(alpha = 0.07f),
                1.0f to Color.Black.copy(alpha = 0.14f)
            ),
            topLeft = Offset(cx - sleeveHalfW * 1.2f, neckY),
            size = Size(sleeveHalfW * 2.4f, sleeveBottomY - neckY + 20f)
        )
    }
    drawPath(sideSleevePath, Color.Black.copy(alpha = 0.34f), style = Stroke(width * 0.0045f, join = StrokeJoin.Round))

    // Side-profile Crew Collar Ring
    val sideCollar = Path().apply {
        moveTo(cx - dir * width * 0.075f, neckY + jerseyHeight * 0.01f)
        quadraticTo(cx + dir * width * 0.01f, neckY + jerseyHeight * 0.045f, cx + dir * width * 0.085f, neckY + jerseyHeight * 0.025f)
    }
    drawPath(sideCollar, collarColor, style = Stroke(width * 0.026f, cap = StrokeCap.Round))

    // Side-Profile Shorts & Knee Sock (if Full Kit enabled)
    if (showShortsPreview) {
        val shortsTop = top + jerseyHeight + fullHeight * 0.02f
        val lowerH = fullHeight * 0.30f
        val shortsH = lowerH * 0.56f
        val shortsHalfW = width * 0.155f
        val sideShorts = Path().apply {
            moveTo(cx - shortsHalfW * 0.85f, shortsTop)
            lineTo(cx + shortsHalfW * 0.85f, shortsTop)
            quadraticTo(cx + shortsHalfW * 1.05f, shortsTop + shortsH * 0.5f, cx + shortsHalfW * 0.96f, shortsTop + shortsH)
            quadraticTo(cx, shortsTop + shortsH * 1.05f, cx - shortsHalfW * 0.96f, shortsTop + shortsH)
            quadraticTo(cx - shortsHalfW * 1.05f, shortsTop + shortsH * 0.5f, cx - shortsHalfW * 0.85f, shortsTop)
            close()
        }
        clipPath(sideShorts) {
            drawRect(shortsColor, Offset(cx - shortsHalfW * 1.2f, shortsTop), Size(shortsHalfW * 2.4f, shortsH * 1.1f))
            // Prominent Outer-Hip Vertical Stripe right down the middle of the side shorts!
            drawRect(shortsTrim, Offset(cx - width * 0.026f, shortsTop), Size(width * 0.052f, shortsH * 1.1f))
            drawRect(Color.Black.copy(alpha = 0.16f), Offset(cx - shortsHalfW * 1.2f, shortsTop), Size(shortsHalfW * 2.4f, shortsH * 0.15f))
        }
        drawPath(sideShorts, Color.Black.copy(alpha = 0.32f), style = Stroke(1.8f, join = StrokeJoin.Round))

        // Side-Profile Knee Sock with turnover cuff + shin brand peek
        val sockTop = shortsTop + shortsH + lowerH * 0.065f
        val sockH = lowerH * 0.37f
        val sockW = width * 0.088f
        val sideSock = Path().apply {
            moveTo(cx - sockW * 0.5f, sockTop)
            lineTo(cx + sockW * 0.5f, sockTop)
            quadraticTo(cx + sockW * 0.52f, sockTop + sockH * 0.55f, cx + dir * sockW * 0.95f, sockTop + sockH * 0.92f)
            quadraticTo(cx + dir * sockW * 0.2f, sockTop + sockH * 1.06f, cx - dir * sockW * 0.55f, sockTop + sockH * 0.92f)
            quadraticTo(cx - sockW * 0.62f, sockTop + sockH * 0.45f, cx - sockW * 0.5f, sockTop)
            close()
        }
        clipPath(sideSock) {
            drawRect(socksColor, Offset(cx - sockW * 1.2f, sockTop), Size(sockW * 2.4f, sockH * 1.1f))
            drawRect(Color.Black.copy(alpha = 0.14f), Offset(cx - sockW * 1.2f, sockTop), Size(sockW * 2.4f, sockH * 0.22f))
            drawRect(socksTrim, Offset(cx - sockW * 1.2f, sockTop + sockH * 0.22f), Size(sockW * 2.4f, sockH * 0.11f))
        }
        drawPath(sideSock, Color.Black.copy(alpha = 0.28f), style = Stroke(1.6f, join = StrokeJoin.Round))
        ProCrestRenderer.run {
            drawCrispManufacturerCompose(spec.manufacturer, brandColor, Offset(cx + dir * sockW * 0.18f, sockTop + sockH * 0.56f), sockW * 0.42f)
        }
    }
}

private fun DrawScope.drawProShortsAndSocks(
    spec: JerseyDesignSpec,
    isBackView: Boolean = false,
    top: Float,
    width: Float,
    height: Float
) {
    val primary = parseColorHex(spec.primaryColorHex)
    val secondary = parseColorHex(spec.secondaryColorHex)
    val third = parseColorHex(spec.thirdColorHex)
    val shortsColor = parseColorHex(spec.shortsColorHex, primary)
    val socksColor = parseColorHex(spec.socksColorHex, primary)
    val shortsTrim = spec.shortsTrimColorHex?.let { parseColorHex(it, secondary) } ?: secondary
    val socksTrim = spec.socksTrimColorHex?.let { parseColorHex(it, secondary) } ?: secondary
    val crestPrimary = spec.crestPrimaryColorHex?.let { parseColorHex(it, primary) } ?: primary
    val crestSecondary = spec.crestSecondaryColorHex?.let { parseColorHex(it, shortsTrim) } ?: shortsTrim
    val crestAccent = spec.crestAccentColorHex?.let { parseColorHex(it, third) } ?: third
    val autoShortsInk = computeReadableTextColor(shortsColor, shortsTrim, third)
    val brandColor = spec.manufacturerColorHex?.let { parseColorHex(it, autoShortsInk) } ?: autoShortsInk
    val shortsNumColor = spec.shortsNumberColorHex?.let { parseColorHex(it, autoShortsInk) }
        ?: spec.playerNumberColorHex?.let { parseColorHex(it, autoShortsInk) }
        ?: autoShortsInk

    val cx = width / 2f
    val shortsH = height * 0.56f

    // Tailored shorts with subtle curved outer hips & contoured leg hems
    val shortsPath = Path().apply {
        moveTo(cx - width * 0.175f, top)
        lineTo(cx + width * 0.175f, top)
        quadraticTo(cx + width * 0.210f, top + shortsH * 0.45f, cx + width * 0.228f, top + shortsH)
        quadraticTo(cx + width * 0.130f, top + shortsH * 1.04f, cx + width * 0.032f, top + shortsH * 0.96f)
        lineTo(cx, top + shortsH * 0.44f)
        lineTo(cx - width * 0.032f, top + shortsH * 0.96f)
        quadraticTo(cx - width * 0.130f, top + shortsH * 1.04f, cx - width * 0.228f, top + shortsH)
        quadraticTo(cx - width * 0.210f, top + shortsH * 0.45f, cx - width * 0.175f, top)
        close()
    }

    drawPath(shortsPath, color = Color.Black.copy(alpha = 0.20f))
    clipPath(shortsPath) {
        drawRect(color = shortsColor, topLeft = Offset(cx - width * 0.25f, top), size = Size(width * 0.50f, shortsH * 1.1f))
        drawRect(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(cx - width * 0.25f, top),
            size = Size(width * 0.50f, shortsH * 0.15f)
        )
        // Outer hip stripes curved cleanly along outer thighs
        val leftHip = Path().apply {
            moveTo(cx - width * 0.175f, top)
            quadraticTo(cx - width * 0.208f, top + shortsH * 0.45f, cx - width * 0.225f, top + shortsH)
        }
        val rightHip = Path().apply {
            moveTo(cx + width * 0.175f, top)
            quadraticTo(cx + width * 0.208f, top + shortsH * 0.45f, cx + width * 0.225f, top + shortsH)
        }
        drawPath(leftHip, color = shortsTrim, style = Stroke(width * 0.018f))
        drawPath(rightHip, color = shortsTrim, style = Stroke(width * 0.018f))

        drawRect(
            brush = Brush.horizontalGradient(
                0.0f to Color.Black.copy(alpha = 0.12f),
                0.35f to Color.White.copy(alpha = 0.05f),
                0.50f to Color.Black.copy(alpha = 0.08f),
                0.65f to Color.White.copy(alpha = 0.05f),
                1.0f to Color.Black.copy(alpha = 0.12f)
            ),
            topLeft = Offset(cx - width * 0.24f, top),
            size = Size(width * 0.48f, shortsH)
        )

        if (!isBackView) {
            drawLine(
                Color.Black.copy(alpha = 0.22f),
                Offset(cx, top + shortsH * 0.15f),
                Offset(cx, top + shortsH * 0.42f),
                width * 0.004f
            )
            drawLine(Color.White.copy(alpha = 0.60f), Offset(cx - width * 0.01f, top + shortsH * 0.12f), Offset(cx - width * 0.02f, top + shortsH * 0.23f), 1.8f)
            drawLine(Color.White.copy(alpha = 0.60f), Offset(cx + width * 0.01f, top + shortsH * 0.12f), Offset(cx + width * 0.02f, top + shortsH * 0.23f), 1.8f)
        }
    }
    drawPath(shortsPath, color = Color.Black.copy(alpha = 0.32f), style = Stroke(width = 2f, join = StrokeJoin.Round))

    if (!isBackView) {
        // Official DLS 26 Front Shorts Layout (matches Barca reference 1:1):
        // Viewer's LEFT (Player's RIGHT Leg): Manufacturer Mark cleanly on lower-front thigh!
        ProCrestRenderer.run {
            drawCrispManufacturerCompose(
                mark = spec.manufacturer,
                color = brandColor,
                center = Offset(cx - width * 0.132f, top + shortsH * 0.78f),
                size = shortsH * 0.20f
            )
        }

        // Viewer's RIGHT (Player's LEFT Leg): Club Crest at lower hem + Shorts Squad Number above!
        if (spec.playerNumber.isNotBlank()) {
            ProCrestRenderer.drawCrispOutlinedText(
                canvas = drawContext.canvas.nativeCanvas,
                text = spec.playerNumber.take(2),
                cx = cx + width * 0.118f,
                baselineY = top + shortsH * 0.56f,
                textSizePx = shortsH * 0.27f,
                fillColor = shortsNumColor.toAndroidColorInt(),
                typeface = null,
                letterSpacingEm = 0f
            )
        }
        ProCrestRenderer.run {
            drawProCrestCompose(
                center = Offset(cx + width * 0.132f, top + shortsH * 0.78f),
                radius = shortsH * 0.145f,
                primary = crestPrimary,
                secondary = crestSecondary,
                accent = crestAccent,
                clubName = spec.clubName,
                crestStyleId = spec.crestStyleId,
                shadowAlpha = 0.14f
            )
        }
    }

    // Anatomically Contoured Knee-High Football Socks underneath
    val sockTop = top + shortsH + height * 0.065f
    val sockH = height * 0.37f
    val sockW = width * 0.072f
    val autoSockInk = computeReadableTextColor(socksColor, socksTrim, third)
    val sockBrandColor = spec.manufacturerColorHex?.let { parseColorHex(it, autoSockInk) } ?: autoSockInk

    listOf(cx - width * 0.125f, cx + width * 0.053f).forEachIndexed { idx, sx ->
        val sCx = sx + sockW / 2f
        val sockPath = Path().apply {
            moveTo(sx, sockTop)
            lineTo(sx + sockW, sockTop)
            quadraticTo(sx + sockW * 1.06f, sockTop + sockH * 0.36f, sx + sockW * 0.82f, sockTop + sockH * 0.78f)
            val footOut = if (idx == 0) sx - sockW * 0.18f else sx + sockW * 1.18f
            val footIn = if (idx == 0) sx + sockW * 0.75f else sx + sockW * 0.25f
            lineTo(footIn, sockTop + sockH * 0.96f)
            quadraticTo(sCx, sockTop + sockH * 1.04f, footOut, sockTop + sockH * 0.94f)
            lineTo(sx + sockW * 0.18f, sockTop + sockH * 0.78f)
            quadraticTo(sx - sockW * 0.06f, sockTop + sockH * 0.36f, sx, sockTop)
            close()
        }
        clipPath(sockPath) {
            drawRect(color = socksColor, topLeft = Offset(sx - sockW * 0.3f, sockTop), size = Size(sockW * 1.6f, sockH * 1.1f))
            drawRect(
                color = Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(sx - sockW * 0.3f, sockTop),
                size = Size(sockW * 1.6f, sockH * 0.22f)
            )
            drawRect(
                color = socksTrim,
                topLeft = Offset(sx - sockW * 0.3f, sockTop + sockH * 0.22f),
                size = Size(sockW * 1.6f, sockH * 0.11f)
            )
            drawRect(
                color = socksTrim.copy(alpha = 0.75f),
                topLeft = Offset(sx - sockW * 0.3f, sockTop + sockH * 0.37f),
                size = Size(sockW * 1.6f, sockH * 0.045f)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(sx - sockW * 0.3f, sockTop + sockH * 0.85f),
                size = Size(sockW * 1.6f, sockH * 0.20f)
            )
        }
        drawPath(sockPath, color = Color.Black.copy(alpha = 0.28f), style = Stroke(width = 1.6f, join = StrokeJoin.Round))
        if (!isBackView) {
            ProCrestRenderer.run {
                drawCrispManufacturerCompose(spec.manufacturer, sockBrandColor, Offset(sCx, sockTop + sockH * 0.58f), sockW * 0.42f)
            }
        }
    }
}

fun computeReadableTextColor(primary: Color, secondary: Color, third: Color): Color {
    val luminance = 0.299f * primary.red + 0.587f * primary.green + 0.114f * primary.blue
    return if (luminance > 0.62f) {
        if (0.299f * secondary.red + 0.587f * secondary.green + 0.114f * secondary.blue < 0.45f) {
            secondary
        } else {
            Color(0xFF0F172A)
        }
    } else {
        if (0.299f * third.red + 0.587f * third.green + 0.114f * third.blue > 0.65f) {
            third
        } else {
            Color.White
        }
    }
}

@Composable
fun ClubCrestBadge(
    primaryHex: String,
    secondaryHex: String,
    shortCode: String,
    modifier: Modifier = Modifier,
    clubName: String = "",
    accentHex: String? = null,
    crestStyleId: String? = null
) {
    val primary = parseColorHex(primaryHex)
    val secondary = parseColorHex(secondaryHex)
    val accent = accentHex?.let { parseColorHex(it) } ?: Color(0xFFFFD54F)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        ProCrestRenderer.run {
            drawProCrestCompose(
                center = Offset(w / 2f, h / 2f),
                radius = minOf(w, h) * 0.42f,
                primary = primary,
                secondary = secondary,
                accent = accent,
                clubName = clubName.ifBlank { shortCode },
                shortCode = shortCode,
                crestStyleId = crestStyleId,
                shadowAlpha = 0.18f
            )
        }
    }
}
