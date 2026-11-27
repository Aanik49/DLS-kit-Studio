package com.example.rendering

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.ManufacturerMark
import com.example.data.model.toAndroidColorInt
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 24 Pro Football Club Crest Styles selectable during custom kit creation
 * and auto-mapped to catalog clubs. Rendered identically on Compose DrawScope
 * and Android Canvas (512x512 DLS 26 UV & 1024px Showcase).
 */
enum class ClubCrestStyle(val displayName: String) {
    ROYAL_CROWN_RING("Royal Crown"),
    CATALAN_HERITAGE("Striped Heritage"),
    SKY_ANCHOR_ROUNDEL("City Roundel"),
    RED_FLAME_SHIELD("Anfield Shield"),
    DEVIL_TRIDENT_CREST("Trident Crest"),
    CANNON_SHIELD("Armory Shield"),
    LION_RING_ROYAL("Royal Ring"),
    EIFFEL_ROUNDEL("Paris Tower"),
    BAVARIAN_LOZENGE("Bavarian Grid"),
    BLACK_YELLOW_CIRCLE("Borussia Ring"),
    MILAN_OVAL_STRIPES("Milan Oval"),
    INTER_MONOGRAM_RING("Interlock Ring"),
    TURIN_MODERN_J("Modern Split"),
    MADRID_STRIPED_SHIELD("Corner Stripes"),
    MIAMI_HERON_RING("Sunburst Ring"),
    LAUREL_EAGLE_CREST("Imperial Wings"),
    NORDIC_CROSS_SHIELD("Templar Cross"),
    CHECKERED_ROYAL("Checkered Crest"),
    SOUTHERN_STARS_FED("3-Star Fed"),
    SUNBURST_DIAGONAL("Diagonal Sash"),
    WINGS_VICTORY("Victory Star"),
    HEX_CYBER_BADGE("Aero Hex"),
    DIAMOND_CLASSIC("Rhombus Crest"),
    CROWN_FIVE_STAR("5-Star Champ");

    companion object {
        fun fromIdOrNull(id: String?): ClubCrestStyle? {
            if (id.isNullOrBlank()) return null
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
        }
    }
}

object ProCrestRenderer {

    fun resolveCrestStyle(
        clubName: String,
        shortCode: String = "",
        explicitStyleId: String? = null
    ): ClubCrestStyle {
        ClubCrestStyle.fromIdOrNull(explicitStyleId)?.let { return it }
        val key = "${clubName.uppercase()} ${shortCode.uppercase()}"
        return when {
            key.contains("REAL MADRID") || key.contains("RMA") -> ClubCrestStyle.ROYAL_CROWN_RING
            key.contains("BARCELONA") || key.contains("FCB") -> ClubCrestStyle.CATALAN_HERITAGE
            key.contains("MANCHESTER CITY") || key.contains("MCI") -> ClubCrestStyle.SKY_ANCHOR_ROUNDEL
            key.contains("LIVERPOOL") || key.contains("LIV") -> ClubCrestStyle.RED_FLAME_SHIELD
            key.contains("MANCHESTER UNITED") || key.contains("MUN") -> ClubCrestStyle.DEVIL_TRIDENT_CREST
            key.contains("ARSENAL") || key.contains("ARS") -> ClubCrestStyle.CANNON_SHIELD
            key.contains("CHELSEA") || key.contains("CHE") -> ClubCrestStyle.LION_RING_ROYAL
            key.contains("PARIS") || key.contains("PSG") -> ClubCrestStyle.EIFFEL_ROUNDEL
            key.contains("BAYERN") || key.contains("FCBAY") -> ClubCrestStyle.BAVARIAN_LOZENGE
            key.contains("DORTMUND") || key.contains("BVB") -> ClubCrestStyle.BLACK_YELLOW_CIRCLE
            key.contains("AC MILAN") || key.contains("ACM") -> ClubCrestStyle.MILAN_OVAL_STRIPES
            key.contains("INTER MIAMI") || key.contains("MIAMI") -> ClubCrestStyle.MIAMI_HERON_RING
            key.contains("INTER MILAN") || key.contains("INT") -> ClubCrestStyle.INTER_MONOGRAM_RING
            key.contains("JUVENTUS") || key.contains("JUV") -> ClubCrestStyle.TURIN_MODERN_J
            key.contains("ATLETICO") || key.contains("ATM") -> ClubCrestStyle.MADRID_STRIPED_SHIELD
            key.contains("BENFICA") || key.contains("LAZIO") || key.contains("EAGLE") -> ClubCrestStyle.LAUREL_EAGLE_CREST
            key.contains("CROATIA") || key.contains("BOAVISTA") -> ClubCrestStyle.CHECKERED_ROYAL
            key.contains("RIVER") || key.contains("PERU") || key.contains("GALAXY") -> ClubCrestStyle.SUNBURST_DIAGONAL
            key.contains("AJAX") || key.contains("TOTTENHAM") || key.contains("TOT") -> ClubCrestStyle.WINGS_VICTORY
            key.contains("BRAZIL") || key.contains("ARGENTINA") || key.contains("FRANCE") ||
                key.contains("SPAIN") || key.contains("ENGLAND") || key.contains("GERMANY") ||
                key.contains("PORTUGAL") || key.contains("ITALY") || key.contains("NETHERLANDS") ||
                key.contains("NATIONAL") -> ClubCrestStyle.SOUTHERN_STARS_FED
            else -> {
                val idx = abs(key.hashCode()) % ClubCrestStyle.entries.size
                ClubCrestStyle.entries[idx]
            }
        }
    }

    fun deriveShortMonogram(clubName: String, explicitShortCode: String = ""): String {
        if (explicitShortCode.isNotBlank()) return explicitShortCode.uppercase().take(3)
        val clean = clubName.trim()
        if (clean.isBlank()) return "DLS"
        val words = clean.split(Regex("\\s+")).filter {
            it.isNotBlank() && it.uppercase() !in listOf("FC", "CF", "SC", "AC", "SK", "UNITED", "TEAM")
        }
        return when {
            words.size >= 2 -> words.take(3).joinToString("") { it.first().uppercase() }
            words.size == 1 -> words.first().uppercase().take(3)
            else -> clean.replace(" ", "").uppercase().take(3)
        }
    }

    // =========================================================================
    // Compose DrawScope Crest Renderer (Live Stage, Crest Picker, Cards)
    // =========================================================================

    fun DrawScope.drawProCrestCompose(
        center: Offset,
        radius: Float,
        primary: Color,
        secondary: Color,
        accent: Color = Color(0xFFFFD54F),
        clubName: String = "",
        shortCode: String = "",
        crestStyleId: String? = null,
        shadowAlpha: Float = 0.22f
    ) {
        val style = resolveCrestStyle(clubName, shortCode, crestStyleId)
        val monogram = deriveShortMonogram(clubName, shortCode)
        val inkColor = contrastTextFor(primary)
        val secInk = contrastTextFor(secondary)

        when (style) {
            ClubCrestStyle.ROYAL_CROWN_RING -> {
                drawCircle(Color.Black.copy(alpha = shadowAlpha), radius * 1.04f, center + Offset(0f, radius * 0.06f))
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.72f, center)
                drawCircle(primary, radius * 0.54f, center)
                val sash = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x - radius * 0.45f, center.y + radius * 0.30f)
                    lineTo(center.x + radius * 0.30f, center.y - radius * 0.45f)
                    lineTo(center.x + radius * 0.45f, center.y - radius * 0.30f)
                    lineTo(center.x - radius * 0.30f, center.y + radius * 0.45f)
                    close()
                }
                drawPath(sash, color = accent)
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.13f))
                drawCrownCompose(center + Offset(0f, -radius * 1.02f), radius * 0.48f, accent)
                drawMonogramNative(center.x, center.y + radius * 0.18f, radius * 0.42f, monogram, inkColor)
            }

            ClubCrestStyle.CATALAN_HERITAGE -> {
                val shield = buildScallopedShieldCompose(center, radius)
                drawPath(shield, Color.Black.copy(alpha = shadowAlpha))
                drawPath(shield, primary)
                clipPath(shield) {
                    val colW = (radius * 2f) / 5f
                    for (i in 0 until 5) {
                        drawRect(
                            color = if (i % 2 == 0) secondary else primary,
                            topLeft = Offset(center.x - radius + i * colW, center.y + radius * 0.02f),
                            size = Size(colW + 0.5f, radius * 1.15f)
                        )
                    }
                    drawRect(accent, Offset(center.x - radius, center.y - radius * 0.22f), Size(radius * 2f, radius * 0.28f))
                    drawRect(Color.White, Offset(center.x - radius, center.y - radius), Size(radius, radius * 0.78f))
                    drawRect(secondary, Offset(center.x - radius * 0.58f, center.y - radius), Size(radius * 0.18f, radius * 0.78f))
                    drawRect(secondary, Offset(center.x - radius, center.y - radius * 0.65f), Size(radius, radius * 0.18f))
                }
                drawPath(shield, color = accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawMonogramNative(center.x, center.y + radius * 0.02f, radius * 0.25f, monogram, contrastTextFor(accent))
            }

            ClubCrestStyle.SKY_ANCHOR_ROUNDEL -> {
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.76f, center, style = Stroke(width = radius * 0.20f))
                val innerShield = buildClassicCrestCompose(center + Offset(0f, radius * 0.06f), radius * 0.56f)
                drawPath(innerShield, primary)
                clipPath(innerShield) {
                    drawRect(secondary, Offset(center.x - radius * 0.6f, center.y + radius * 0.05f), Size(radius * 1.2f, radius * 0.6f))
                    for (i in -1..1) {
                        drawLine(
                            accent,
                            Offset(center.x - radius * 0.35f + i * radius * 0.18f, center.y + radius * 0.12f),
                            Offset(center.x - radius * 0.12f + i * radius * 0.18f, center.y + radius * 0.65f),
                            strokeWidth = radius * 0.08f
                        )
                    }
                }
                drawPath(innerShield, accent, style = Stroke(width = radius * 0.09f))
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.10f))
                drawMonogramNative(center.x, center.y - radius * 0.04f, radius * 0.32f, monogram, inkColor)
            }

            ClubCrestStyle.RED_FLAME_SHIELD -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x - radius, center.y - radius), Size(radius * 2f, radius * 0.45f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                // Twin flame torches beside top
                drawStarCompose(center + Offset(-radius * 0.76f, -radius * 0.92f), radius * 0.16f, accent)
                drawStarCompose(center + Offset(radius * 0.76f, -radius * 0.92f), radius * 0.16f, accent)
                // Central chevron flame
                val flame = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - radius * 0.32f)
                    lineTo(center.x + radius * 0.28f, center.y + radius * 0.10f)
                    lineTo(center.x, center.y + radius * 0.44f)
                    lineTo(center.x - radius * 0.28f, center.y + radius * 0.10f)
                    close()
                }
                drawPath(flame, accent)
                drawMonogramNative(center.x, center.y - radius * 0.56f, radius * 0.28f, monogram, secInk)
            }

            ClubCrestStyle.DEVIL_TRIDENT_CREST -> {
                val shield = buildScallopedShieldCompose(center, radius * 0.92f)
                drawPath(shield, primary)
                drawPath(shield, accent, style = Stroke(width = radius * 0.14f, join = StrokeJoin.Round))
                // Top & bottom ribbon bars
                drawRoundRect(
                    secondary,
                    Offset(center.x - radius * 0.92f, center.y - radius * 0.76f),
                    Size(radius * 1.84f, radius * 0.34f),
                    CornerRadius(radius * 0.12f)
                )
                // Stylized trident in center
                val sw = radius * 0.10f
                drawLine(accent, Offset(center.x, center.y - radius * 0.25f), Offset(center.x, center.y + radius * 0.62f), sw)
                drawLine(accent, Offset(center.x - radius * 0.32f, center.y - radius * 0.15f), Offset(center.x - radius * 0.32f, center.y + radius * 0.18f), sw)
                drawLine(accent, Offset(center.x + radius * 0.32f, center.y - radius * 0.15f), Offset(center.x + radius * 0.32f, center.y + radius * 0.18f), sw)
                drawLine(accent, Offset(center.x - radius * 0.32f, center.y + radius * 0.18f), Offset(center.x + radius * 0.32f, center.y + radius * 0.18f), sw)
                drawMonogramNative(center.x, center.y - radius * 0.49f, radius * 0.28f, monogram, secInk)
            }

            ClubCrestStyle.CANNON_SHIELD -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x, center.y - radius), Size(radius, radius * 2.2f))
                    // Bold barrel & wheel motif
                    drawLine(
                        accent,
                        Offset(center.x - radius * 0.55f, center.y + radius * 0.12f),
                        Offset(center.x + radius * 0.58f, center.y - radius * 0.12f),
                        strokeWidth = radius * 0.22f
                    )
                    drawCircle(accent, radius * 0.24f, center + Offset(0f, radius * 0.24f), style = Stroke(radius * 0.09f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.14f, join = StrokeJoin.Round))
                drawMonogramNative(center.x, center.y - radius * 0.40f, radius * 0.32f, monogram, inkColor)
            }

            ClubCrestStyle.LION_RING_ROYAL -> {
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.78f, center, style = Stroke(width = radius * 0.18f))
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.10f))
                drawCircle(accent, radius * 0.64f, center, style = Stroke(width = radius * 0.06f))
                drawStarCompose(center + Offset(-radius * 0.78f, 0f), radius * 0.12f, accent)
                drawStarCompose(center + Offset(radius * 0.78f, 0f), radius * 0.12f, accent)
                drawStarCompose(center + Offset(0f, -radius * 0.26f), radius * 0.22f, accent)
                drawMonogramNative(center.x, center.y + radius * 0.34f, radius * 0.38f, monogram, inkColor)
            }

            ClubCrestStyle.EIFFEL_ROUNDEL -> {
                drawCircle(primary, radius, center)
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.12f))
                drawCircle(Color.White.copy(alpha = 0.85f), radius * 0.72f, center, style = Stroke(width = radius * 0.06f))
                // Stylized Paris tower A-frame
                val tower = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - radius * 0.62f)
                    lineTo(center.x + radius * 0.44f, center.y + radius * 0.56f)
                    lineTo(center.x + radius * 0.22f, center.y + radius * 0.56f)
                    lineTo(center.x, center.y - radius * 0.06f)
                    lineTo(center.x - radius * 0.22f, center.y + radius * 0.56f)
                    lineTo(center.x - radius * 0.44f, center.y + radius * 0.56f)
                    close()
                }
                drawPath(tower, secondary)
                drawLine(secondary, Offset(center.x - radius * 0.32f, center.y + radius * 0.18f), Offset(center.x + radius * 0.32f, center.y + radius * 0.18f), radius * 0.12f)
                drawStarCompose(center + Offset(0f, radius * 0.42f), radius * 0.16f, accent)
            }

            ClubCrestStyle.BAVARIAN_LOZENGE -> {
                drawCircle(primary, radius, center)
                val innerCircle = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(center.x - radius * 0.70f, center.y - radius * 0.70f, center.x + radius * 0.70f, center.y + radius * 0.70f))
                }
                clipPath(innerCircle) {
                    drawRect(Color.White, Offset(center.x - radius, center.y - radius), Size(radius * 2f, radius * 2f))
                    val step = radius * 0.44f
                    for (r in -2..2) {
                        for (c in -2..2) {
                            if ((r + c) % 2 == 0) {
                                val dx = center.x + c * step
                                val dy = center.y + r * step
                                val loz = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(dx, dy - step * 0.52f)
                                    lineTo(dx + step * 0.52f, dy)
                                    lineTo(dx, dy + step * 0.52f)
                                    lineTo(dx - step * 0.52f, dy)
                                    close()
                                }
                                drawPath(loz, secondary)
                            }
                        }
                    }
                }
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.12f))
                drawCircle(accent, radius * 0.70f, center, style = Stroke(width = radius * 0.08f))
            }

            ClubCrestStyle.BLACK_YELLOW_CIRCLE -> {
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.84f, center, style = Stroke(width = radius * 0.14f))
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.08f))
                drawLine(secondary, Offset(center.x - radius * 0.65f, center.y + radius * 0.25f), Offset(center.x + radius * 0.65f, center.y + radius * 0.25f), radius * 0.08f)
                drawMonogramNative(center.x, center.y + radius * 0.14f, radius * 0.56f, monogram, inkColor)
            }

            ClubCrestStyle.MILAN_OVAL_STRIPES -> {
                val oval = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(center.x - radius * 0.76f, center.y - radius * 1.04f, center.x + radius * 0.76f, center.y + radius * 1.04f))
                }
                drawPath(oval, primary)
                clipPath(oval) {
                    // Left stripes
                    val sw = radius * 0.24f
                    for (i in 0..2) {
                        drawRect(if (i % 2 == 0) primary else secondary, Offset(center.x - radius * 0.76f + i * sw, center.y - radius * 1.1f), Size(sw, radius * 2.2f))
                    }
                    // Right cross
                    drawRect(Color.White, Offset(center.x, center.y - radius * 1.1f), Size(radius, radius * 2.2f))
                    drawRect(secondary, Offset(center.x + radius * 0.26f, center.y - radius * 1.1f), Size(radius * 0.18f, radius * 2.2f))
                    drawRect(secondary, Offset(center.x, center.y - radius * 0.10f), Size(radius, radius * 0.20f))
                }
                drawPath(oval, accent, style = Stroke(width = radius * 0.13f))
                drawStarCompose(center + Offset(0f, -radius * 1.18f), radius * 0.17f, accent)
            }

            ClubCrestStyle.INTER_MONOGRAM_RING -> {
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.78f, center, style = Stroke(width = radius * 0.15f))
                drawCircle(accent, radius * 0.58f, center, style = Stroke(width = radius * 0.08f))
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.09f))
                drawMonogramNative(center.x, center.y + radius * 0.20f, radius * 0.46f, monogram, accent)
            }

            ClubCrestStyle.TURIN_MODERN_J -> {
                val badge = buildClassicCrestCompose(center, radius)
                drawPath(badge, primary)
                clipPath(badge) {
                    // Bold vertical twin stripes
                    drawRect(secondary, Offset(center.x - radius * 0.46f, center.y - radius), Size(radius * 0.26f, radius * 1.65f))
                    drawRect(secondary, Offset(center.x + radius * 0.06f, center.y - radius), Size(radius * 0.26f, radius * 1.85f))
                    drawRect(accent, Offset(center.x - radius * 0.46f, center.y + radius * 0.45f), Size(radius * 0.78f, radius * 0.20f))
                }
                drawPath(badge, accent, style = Stroke(width = radius * 0.12f, join = StrokeJoin.Round))
            }

            ClubCrestStyle.MADRID_STRIPED_SHIELD -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    val colW = (radius * 2f) / 5f
                    for (i in 0 until 5) {
                        if (i % 2 == 1) drawRect(secondary, Offset(center.x - radius + i * colW, center.y - radius), Size(colW, radius * 2.2f))
                    }
                    // Diagonal upper-left navy/primary triangle
                    val corner = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x - radius, center.y - radius)
                        lineTo(center.x + radius * 0.35f, center.y - radius)
                        lineTo(center.x - radius, center.y + radius * 0.25f)
                        close()
                    }
                    drawPath(corner, primary)
                    drawStarCompose(center + Offset(-radius * 0.42f, -radius * 0.48f), radius * 0.18f, accent)
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
            }

            ClubCrestStyle.MIAMI_HERON_RING -> {
                drawCircle(primary, radius, center)
                drawCircle(secondary, radius * 0.72f, center)
                // Art Deco rays from center
                for (a in 0 until 12) {
                    val rad = Math.toRadians(a * 30.0)
                    drawLine(
                        accent.copy(alpha = 0.75f),
                        center,
                        Offset(center.x + (radius * 0.66f * cos(rad)).toFloat(), center.y + (radius * 0.66f * sin(rad)).toFloat()),
                        strokeWidth = radius * 0.06f
                    )
                }
                drawCircle(primary, radius * 0.42f, center)
                drawCircle(accent, radius, center, style = Stroke(width = radius * 0.12f))
                drawMonogramNative(center.x, center.y + radius * 0.16f, radius * 0.38f, monogram, inkColor)
            }

            ClubCrestStyle.LAUREL_EAGLE_CREST -> {
                // Outstretched eagle wings behind shield
                val wings = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x - radius * 1.15f, center.y - radius * 0.45f)
                    lineTo(center.x, center.y - radius * 0.10f)
                    lineTo(center.x + radius * 1.15f, center.y - radius * 0.45f)
                    lineTo(center.x + radius * 0.90f, center.y + radius * 0.18f)
                    lineTo(center.x, center.y + radius * 0.28f)
                    lineTo(center.x - radius * 0.90f, center.y + radius * 0.18f)
                    close()
                }
                drawPath(wings, accent)
                val shield = buildClassicCrestCompose(center + Offset(0f, radius * 0.10f), radius * 0.78f)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x - radius, center.y - radius * 0.15f), Size(radius * 2f, radius * 0.32f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.11f))
                drawMonogramNative(center.x, center.y + radius * 0.14f, radius * 0.32f, monogram, secInk)
            }

            ClubCrestStyle.NORDIC_CROSS_SHIELD -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x - radius * 0.20f, center.y - radius), Size(radius * 0.40f, radius * 2.2f))
                    drawRect(secondary, Offset(center.x - radius, center.y - radius * 0.24f), Size(radius * 2f, radius * 0.40f))
                    drawRect(accent, Offset(center.x - radius * 0.08f, center.y - radius), Size(radius * 0.16f, radius * 2.2f))
                    drawRect(accent, Offset(center.x - radius, center.y - radius * 0.12f), Size(radius * 2f, radius * 0.16f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
            }

            ClubCrestStyle.CHECKERED_ROYAL -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    val cell = radius * 0.50f
                    for (r in -2..2) {
                        for (c in -2..2) {
                            if ((r + c) % 2 == 0) {
                                drawRect(secondary, Offset(center.x + c * cell, center.y + r * cell), Size(cell, cell))
                            }
                        }
                    }
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawCrownCompose(center + Offset(0f, -radius * 0.96f), radius * 0.46f, accent)
            }

            ClubCrestStyle.SOUTHERN_STARS_FED -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x - radius * 0.26f, center.y - radius), Size(radius * 0.52f, radius * 2.2f))
                    val diamond = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x, center.y - radius * 0.52f)
                        lineTo(center.x + radius * 0.52f, center.y)
                        lineTo(center.x, center.y + radius * 0.52f)
                        lineTo(center.x - radius * 0.52f, center.y)
                        close()
                    }
                    drawPath(diamond, primary)
                    drawPath(diamond, accent, style = Stroke(width = radius * 0.08f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawStarCompose(center + Offset(0f, -radius * 1.12f), radius * 0.17f, accent)
                drawStarCompose(center + Offset(-radius * 0.36f, -radius * 1.02f), radius * 0.12f, accent)
                drawStarCompose(center + Offset(radius * 0.36f, -radius * 1.02f), radius * 0.12f, accent)
                drawMonogramNative(center.x, center.y + radius * 0.14f, radius * 0.36f, monogram, inkColor)
            }

            ClubCrestStyle.SUNBURST_DIAGONAL -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    val sash = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x - radius, center.y - radius * 0.95f)
                        lineTo(center.x - radius * 0.45f, center.y - radius * 0.95f)
                        lineTo(center.x + radius, center.y + radius * 0.65f)
                        lineTo(center.x + radius, center.y + radius * 1.15f)
                        lineTo(center.x + radius * 0.45f, center.y + radius * 1.15f)
                        lineTo(center.x - radius, center.y - radius * 0.45f)
                        close()
                    }
                    drawPath(sash, secondary)
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawMonogramNative(center.x, center.y + radius * 0.18f, radius * 0.38f, monogram, inkColor)
            }

            ClubCrestStyle.WINGS_VICTORY -> {
                drawCircle(primary, radius * 0.92f, center)
                drawCircle(secondary, radius * 0.92f, center, style = Stroke(width = radius * 0.18f))
                drawCircle(accent, radius * 0.72f, center, style = Stroke(width = radius * 0.08f))
                drawStarCompose(center + Offset(0f, -radius * 0.16f), radius * 0.42f, accent)
                drawMonogramNative(center.x, center.y + radius * 0.56f, radius * 0.30f, monogram, inkColor)
            }

            ClubCrestStyle.HEX_CYBER_BADGE -> {
                val hex = buildHexCompose(center, radius)
                drawPath(hex, primary)
                clipPath(hex) {
                    val chevron = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x - radius, center.y - radius * 0.15f)
                        lineTo(center.x, center.y + radius * 0.32f)
                        lineTo(center.x + radius, center.y - radius * 0.15f)
                        lineTo(center.x + radius, center.y + radius * 0.25f)
                        lineTo(center.x, center.y + radius * 0.72f)
                        lineTo(center.x - radius, center.y + radius * 0.25f)
                        close()
                    }
                    drawPath(chevron, secondary)
                }
                drawPath(hex, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawMonogramNative(center.x, center.y - radius * 0.10f, radius * 0.38f, monogram, inkColor)
            }

            ClubCrestStyle.DIAMOND_CLASSIC -> {
                val diamond = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - radius * 1.04f)
                    lineTo(center.x + radius * 0.92f, center.y)
                    lineTo(center.x, center.y + radius * 1.04f)
                    lineTo(center.x - radius * 0.92f, center.y)
                    close()
                }
                drawPath(diamond, primary)
                clipPath(diamond) {
                    drawRect(secondary, Offset(center.x - radius, center.y - radius * 0.22f), Size(radius * 2f, radius * 0.44f))
                }
                drawPath(diamond, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                drawMonogramNative(center.x, center.y + radius * 0.14f, radius * 0.38f, monogram, secInk)
            }

            ClubCrestStyle.CROWN_FIVE_STAR -> {
                val shield = buildClassicCrestCompose(center, radius)
                drawPath(shield, primary)
                clipPath(shield) {
                    drawRect(secondary, Offset(center.x - radius, center.y + radius * 0.05f), Size(radius * 2f, radius * 1.15f))
                }
                drawPath(shield, accent, style = Stroke(width = radius * 0.13f, join = StrokeJoin.Round))
                // 5-star arc across upper crest
                listOf(-0.56f, -0.28f, 0f, 0.28f, 0.56f).forEachIndexed { idx, xOff ->
                    val yOff = -0.34f - (2 - abs(idx - 2)) * 0.08f
                    drawStarCompose(center + Offset(radius * xOff, radius * yOff), radius * 0.11f, accent)
                }
                drawMonogramNative(center.x, center.y + radius * 0.44f, radius * 0.38f, monogram, secInk)
            }
        }
    }

    // =========================================================================
    // Android Canvas Crest Renderer (512x512 DLS 26 UV & 1024px Showcase)
    // =========================================================================

    fun drawProCrestAndroid(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        primary: Int,
        secondary: Int,
        accent: Int = android.graphics.Color.rgb(255, 213, 79),
        clubName: String = "",
        shortCode: String = "",
        crestStyleId: String? = null
    ) {
        val crestStyle = resolveCrestStyle(clubName, shortCode, crestStyleId)
        val monogram = deriveShortMonogram(clubName, shortCode)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
        }
        val inkColor = if (luminance(primary) > 0.62) android.graphics.Color.rgb(15, 23, 42) else android.graphics.Color.WHITE
        val secInk = if (luminance(secondary) > 0.62) android.graphics.Color.rgb(15, 23, 42) else android.graphics.Color.WHITE
        val accInk = if (luminance(accent) > 0.62) android.graphics.Color.rgb(15, 23, 42) else android.graphics.Color.WHITE

        when (crestStyle) {
            ClubCrestStyle.ROYAL_CROWN_RING -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.72f, fillPaint.apply { color = secondary })
                canvas.drawCircle(cx, cy, radius * 0.54f, fillPaint.apply { color = primary })
                val sash = Path().apply {
                    moveTo(cx - radius * 0.45f, cy + radius * 0.30f)
                    lineTo(cx + radius * 0.30f, cy - radius * 0.45f)
                    lineTo(cx + radius * 0.45f, cy - radius * 0.30f)
                    lineTo(cx - radius * 0.30f, cy + radius * 0.45f)
                    close()
                }
                canvas.drawPath(sash, fillPaint.apply { color = accent })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.14f })
                drawCrownAndroid(canvas, cx, cy - radius * 1.02f, radius * 0.48f, accent)
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.18f, radius * 0.42f, inkColor)
            }

            ClubCrestStyle.CATALAN_HERITAGE -> {
                val shield = buildScallopedShieldAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                val colW = (radius * 2f) / 5f
                for (i in 0 until 5) {
                    fillPaint.color = if (i % 2 == 0) secondary else primary
                    canvas.drawRect(cx - radius + i * colW, cy + radius * 0.02f, cx - radius + (i + 1) * colW + 0.5f, cy + radius * 1.2f, fillPaint)
                }
                canvas.drawRect(cx - radius, cy - radius * 0.22f, cx + radius, cy + radius * 0.06f, fillPaint.apply { color = accent })
                canvas.drawRect(cx - radius, cy - radius, cx, cy - radius * 0.22f, fillPaint.apply { color = android.graphics.Color.WHITE })
                canvas.drawRect(cx - radius * 0.58f, cy - radius, cx - radius * 0.40f, cy - radius * 0.22f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx - radius, cy - radius * 0.65f, cx, cy - radius * 0.47f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.14f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.02f, radius * 0.25f, accInk)
            }

            ClubCrestStyle.SKY_ANCHOR_ROUNDEL -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.76f, strokePaint.apply { color = secondary; strokeWidth = radius * 0.20f })
                val inner = buildClassicCrestAndroid(cx, cy + radius * 0.06f, radius * 0.56f)
                canvas.save()
                canvas.clipPath(inner)
                canvas.drawPath(inner, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius * 0.6f, cy + radius * 0.05f, cx + radius * 0.6f, cy + radius * 0.7f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(inner, strokePaint.apply { color = accent; strokeWidth = radius * 0.09f })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.11f })
                drawAndroidText(canvas, monogram, cx, cy - radius * 0.04f, radius * 0.32f, inkColor)
            }

            ClubCrestStyle.RED_FLAME_SHIELD -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius, cy - radius, cx + radius, cy - radius * 0.55f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawStarAndroid(canvas, cx - radius * 0.76f, cy - radius * 0.92f, radius * 0.16f, accent)
                drawStarAndroid(canvas, cx + radius * 0.76f, cy - radius * 0.92f, radius * 0.16f, accent)
                val flame = Path().apply {
                    moveTo(cx, cy - radius * 0.32f)
                    lineTo(cx + radius * 0.28f, cy + radius * 0.10f)
                    lineTo(cx, cy + radius * 0.44f)
                    lineTo(cx - radius * 0.28f, cy + radius * 0.10f)
                    close()
                }
                canvas.drawPath(flame, fillPaint.apply { color = accent })
                drawAndroidText(canvas, monogram, cx, cy - radius * 0.56f, radius * 0.28f, secInk)
            }

            ClubCrestStyle.DEVIL_TRIDENT_CREST -> {
                val shield = buildScallopedShieldAndroid(cx, cy, radius * 0.92f)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.14f })
                canvas.drawRoundRect(
                    RectF(cx - radius * 0.92f, cy - radius * 0.76f, cx + radius * 0.92f, cy - radius * 0.42f),
                    radius * 0.12f, radius * 0.12f, fillPaint.apply { color = secondary }
                )
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = radius * 0.10f; strokeCap = Paint.Cap.ROUND }
                canvas.drawLine(cx, cy - radius * 0.25f, cx, cy + radius * 0.62f, lineP)
                canvas.drawLine(cx - radius * 0.32f, cy - radius * 0.15f, cx - radius * 0.32f, cy + radius * 0.18f, lineP)
                canvas.drawLine(cx + radius * 0.32f, cy - radius * 0.15f, cx + radius * 0.32f, cy + radius * 0.18f, lineP)
                canvas.drawLine(cx - radius * 0.32f, cy + radius * 0.18f, cx + radius * 0.32f, cy + radius * 0.18f, lineP)
                drawAndroidText(canvas, monogram, cx, cy - radius * 0.49f, radius * 0.28f, secInk)
            }

            ClubCrestStyle.CANNON_SHIELD -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx, cy - radius, cx + radius, cy + radius * 1.2f, fillPaint.apply { color = secondary })
                val barP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = radius * 0.22f }
                canvas.drawLine(cx - radius * 0.55f, cy + radius * 0.12f, cx + radius * 0.58f, cy - radius * 0.12f, barP)
                canvas.drawCircle(cx, cy + radius * 0.24f, radius * 0.24f, strokePaint.apply { color = accent; strokeWidth = radius * 0.09f })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.14f })
                drawAndroidText(canvas, monogram, cx, cy - radius * 0.40f, radius * 0.32f, inkColor)
            }

            ClubCrestStyle.LION_RING_ROYAL -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.78f, strokePaint.apply { color = secondary; strokeWidth = radius * 0.18f })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.11f })
                canvas.drawCircle(cx, cy, radius * 0.64f, strokePaint.apply { color = accent; strokeWidth = radius * 0.06f })
                drawStarAndroid(canvas, cx - radius * 0.78f, cy, radius * 0.12f, accent)
                drawStarAndroid(canvas, cx + radius * 0.78f, cy, radius * 0.12f, accent)
                drawStarAndroid(canvas, cx, cy - radius * 0.26f, radius * 0.22f, accent)
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.34f, radius * 0.38f, inkColor)
            }

            ClubCrestStyle.EIFFEL_ROUNDEL -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.12f })
                val tower = Path().apply {
                    moveTo(cx, cy - radius * 0.62f)
                    lineTo(cx + radius * 0.44f, cy + radius * 0.56f)
                    lineTo(cx + radius * 0.22f, cy + radius * 0.56f)
                    lineTo(cx, cy - radius * 0.06f)
                    lineTo(cx - radius * 0.22f, cy + radius * 0.56f)
                    lineTo(cx - radius * 0.44f, cy + radius * 0.56f)
                    close()
                }
                canvas.drawPath(tower, fillPaint.apply { color = secondary })
                canvas.drawLine(cx - radius * 0.32f, cy + radius * 0.18f, cx + radius * 0.32f, cy + radius * 0.18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; strokeWidth = radius * 0.12f })
                drawStarAndroid(canvas, cx, cy + radius * 0.42f, radius * 0.16f, accent)
            }

            ClubCrestStyle.BAVARIAN_LOZENGE -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                val inner = Path().apply { addCircle(cx, cy, radius * 0.70f, Path.Direction.CW) }
                canvas.save()
                canvas.clipPath(inner)
                canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, fillPaint.apply { color = android.graphics.Color.WHITE })
                val step = radius * 0.44f
                fillPaint.color = secondary
                for (r in -2..2) {
                    for (c in -2..2) {
                        if ((r + c) % 2 == 0) {
                            val dx = cx + c * step
                            val dy = cy + r * step
                            val loz = Path().apply {
                                moveTo(dx, dy - step * 0.52f)
                                lineTo(dx + step * 0.52f, dy)
                                lineTo(dx, dy + step * 0.52f)
                                lineTo(dx - step * 0.52f, dy)
                                close()
                            }
                            canvas.drawPath(loz, fillPaint)
                        }
                    }
                }
                canvas.restore()
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.12f })
                canvas.drawCircle(cx, cy, radius * 0.70f, strokePaint.apply { color = accent; strokeWidth = radius * 0.08f })
            }

            ClubCrestStyle.BLACK_YELLOW_CIRCLE -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.84f, strokePaint.apply { color = secondary; strokeWidth = radius * 0.14f })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.09f })
                canvas.drawLine(cx - radius * 0.65f, cy + radius * 0.25f, cx + radius * 0.65f, cy + radius * 0.25f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; strokeWidth = radius * 0.08f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.14f, radius * 0.54f, inkColor)
            }

            ClubCrestStyle.MILAN_OVAL_STRIPES -> {
                val oval = Path().apply {
                    addOval(RectF(cx - radius * 0.76f, cy - radius * 1.04f, cx + radius * 0.76f, cy + radius * 1.04f), Path.Direction.CW)
                }
                canvas.save()
                canvas.clipPath(oval)
                val sw = radius * 0.25f
                for (i in 0..2) {
                    fillPaint.color = if (i % 2 == 0) primary else secondary
                    canvas.drawRect(cx - radius * 0.76f + i * sw, cy - radius * 1.1f, cx - radius * 0.76f + (i + 1) * sw, cy + radius * 1.1f, fillPaint)
                }
                canvas.drawRect(cx, cy - radius * 1.1f, cx + radius, cy + radius * 1.1f, fillPaint.apply { color = android.graphics.Color.WHITE })
                canvas.drawRect(cx + radius * 0.26f, cy - radius * 1.1f, cx + radius * 0.44f, cy + radius * 1.1f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx, cy - radius * 0.10f, cx + radius, cy + radius * 0.10f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(oval, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawStarAndroid(canvas, cx, cy - radius * 1.18f, radius * 0.17f, accent)
            }

            ClubCrestStyle.INTER_MONOGRAM_RING -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.78f, strokePaint.apply { color = secondary; strokeWidth = radius * 0.15f })
                canvas.drawCircle(cx, cy, radius * 0.58f, strokePaint.apply { color = accent; strokeWidth = radius * 0.08f })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.09f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.20f, radius * 0.46f, accent)
            }

            ClubCrestStyle.TURIN_MODERN_J -> {
                val badge = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(badge)
                canvas.drawPath(badge, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius * 0.46f, cy - radius, cx - radius * 0.20f, cy + radius * 0.65f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx + radius * 0.06f, cy - radius, cx + radius * 0.32f, cy + radius * 0.85f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx - radius * 0.46f, cy + radius * 0.45f, cx + radius * 0.32f, cy + radius * 0.65f, fillPaint.apply { color = accent })
                canvas.restore()
                canvas.drawPath(badge, strokePaint.apply { color = accent; strokeWidth = radius * 0.12f })
            }

            ClubCrestStyle.MADRID_STRIPED_SHIELD -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                val colW = (radius * 2f) / 5f
                fillPaint.color = secondary
                for (i in 0 until 5) {
                    if (i % 2 == 1) canvas.drawRect(cx - radius + i * colW, cy - radius, cx - radius + (i + 1) * colW, cy + radius * 1.2f, fillPaint)
                }
                val corner = Path().apply {
                    moveTo(cx - radius, cy - radius)
                    lineTo(cx + radius * 0.35f, cy - radius)
                    lineTo(cx - radius, cy + radius * 0.25f)
                    close()
                }
                canvas.drawPath(corner, fillPaint.apply { color = primary })
                drawStarAndroid(canvas, cx - radius * 0.42f, cy - radius * 0.48f, radius * 0.18f, accent)
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
            }

            ClubCrestStyle.MIAMI_HERON_RING -> {
                canvas.drawCircle(cx, cy, radius, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.72f, fillPaint.apply { color = secondary })
                val rayP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = radius * 0.06f }
                for (a in 0 until 12) {
                    val rad = Math.toRadians(a * 30.0)
                    canvas.drawLine(cx, cy, cx + (radius * 0.66f * cos(rad)).toFloat(), cy + (radius * 0.66f * sin(rad)).toFloat(), rayP)
                }
                canvas.drawCircle(cx, cy, radius * 0.42f, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius, strokePaint.apply { color = accent; strokeWidth = radius * 0.12f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.16f, radius * 0.38f, inkColor)
            }

            ClubCrestStyle.LAUREL_EAGLE_CREST -> {
                val wings = Path().apply {
                    moveTo(cx - radius * 1.15f, cy - radius * 0.45f)
                    lineTo(cx, cy - radius * 0.10f)
                    lineTo(cx + radius * 1.15f, cy - radius * 0.45f)
                    lineTo(cx + radius * 0.90f, cy + radius * 0.18f)
                    lineTo(cx, cy + radius * 0.28f)
                    lineTo(cx - radius * 0.90f, cy + radius * 0.18f)
                    close()
                }
                canvas.drawPath(wings, fillPaint.apply { color = accent })
                val shield = buildClassicCrestAndroid(cx, cy + radius * 0.10f, radius * 0.78f)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius, cy - radius * 0.15f, cx + radius, cy + radius * 0.17f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.11f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.14f, radius * 0.32f, secInk)
            }

            ClubCrestStyle.NORDIC_CROSS_SHIELD -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius * 0.20f, cy - radius, cx + radius * 0.20f, cy + radius * 1.2f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx - radius, cy - radius * 0.24f, cx + radius, cy + radius * 0.16f, fillPaint.apply { color = secondary })
                canvas.drawRect(cx - radius * 0.08f, cy - radius, cx + radius * 0.08f, cy + radius * 1.2f, fillPaint.apply { color = accent })
                canvas.drawRect(cx - radius, cy - radius * 0.12f, cx + radius, cy + radius * 0.04f, fillPaint.apply { color = accent })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
            }

            ClubCrestStyle.CHECKERED_ROYAL -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                val cell = radius * 0.50f
                fillPaint.color = secondary
                for (r in -2..2) {
                    for (c in -2..2) {
                        if ((r + c) % 2 == 0) {
                            canvas.drawRect(cx + c * cell, cy + r * cell, cx + (c + 1) * cell, cy + (r + 1) * cell, fillPaint)
                        }
                    }
                }
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawCrownAndroid(canvas, cx, cy - radius * 0.96f, radius * 0.46f, accent)
            }

            ClubCrestStyle.SOUTHERN_STARS_FED -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius * 0.26f, cy - radius, cx + radius * 0.26f, cy + radius * 1.2f, fillPaint.apply { color = secondary })
                val diamond = Path().apply {
                    moveTo(cx, cy - radius * 0.52f)
                    lineTo(cx + radius * 0.52f, cy)
                    lineTo(cx, cy + radius * 0.52f)
                    lineTo(cx - radius * 0.52f, cy)
                    close()
                }
                canvas.drawPath(diamond, fillPaint.apply { color = primary })
                canvas.drawPath(diamond, strokePaint.apply { color = accent; strokeWidth = radius * 0.09f })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.14f })
                drawStarAndroid(canvas, cx, cy - radius * 1.12f, radius * 0.17f, accent)
                drawStarAndroid(canvas, cx - radius * 0.36f, cy - radius * 1.02f, radius * 0.12f, accent)
                drawStarAndroid(canvas, cx + radius * 0.36f, cy - radius * 1.02f, radius * 0.12f, accent)
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.14f, radius * 0.36f, inkColor)
            }

            ClubCrestStyle.SUNBURST_DIAGONAL -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                val sash = Path().apply {
                    moveTo(cx - radius, cy - radius * 0.95f)
                    lineTo(cx - radius * 0.45f, cy - radius * 0.95f)
                    lineTo(cx + radius, cy + radius * 0.65f)
                    lineTo(cx + radius, cy + radius * 1.15f)
                    lineTo(cx + radius * 0.45f, cy + radius * 1.15f)
                    lineTo(cx - radius, cy - radius * 0.45f)
                    close()
                }
                canvas.drawPath(sash, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.18f, radius * 0.38f, inkColor)
            }

            ClubCrestStyle.WINGS_VICTORY -> {
                canvas.drawCircle(cx, cy, radius * 0.92f, fillPaint.apply { color = primary })
                canvas.drawCircle(cx, cy, radius * 0.92f, strokePaint.apply { color = secondary; strokeWidth = radius * 0.18f })
                canvas.drawCircle(cx, cy, radius * 0.72f, strokePaint.apply { color = accent; strokeWidth = radius * 0.08f })
                drawStarAndroid(canvas, cx, cy - radius * 0.16f, radius * 0.42f, accent)
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.56f, radius * 0.30f, inkColor)
            }

            ClubCrestStyle.HEX_CYBER_BADGE -> {
                val hex = buildHexAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(hex)
                canvas.drawPath(hex, fillPaint.apply { color = primary })
                val chevron = Path().apply {
                    moveTo(cx - radius, cy - radius * 0.15f)
                    lineTo(cx, cy + radius * 0.32f)
                    lineTo(cx + radius, cy - radius * 0.15f)
                    lineTo(cx + radius, cy + radius * 0.25f)
                    lineTo(cx, cy + radius * 0.72f)
                    lineTo(cx - radius, cy + radius * 0.25f)
                    close()
                }
                canvas.drawPath(chevron, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(hex, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawAndroidText(canvas, monogram, cx, cy - radius * 0.10f, radius * 0.38f, inkColor)
            }

            ClubCrestStyle.DIAMOND_CLASSIC -> {
                val diamond = Path().apply {
                    moveTo(cx, cy - radius * 1.04f)
                    lineTo(cx + radius * 0.92f, cy)
                    lineTo(cx, cy + radius * 1.04f)
                    lineTo(cx - radius * 0.92f, cy)
                    close()
                }
                canvas.save()
                canvas.clipPath(diamond)
                canvas.drawPath(diamond, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius, cy - radius * 0.22f, cx + radius, cy + radius * 0.22f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(diamond, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.14f, radius * 0.38f, secInk)
            }

            ClubCrestStyle.CROWN_FIVE_STAR -> {
                val shield = buildClassicCrestAndroid(cx, cy, radius)
                canvas.save()
                canvas.clipPath(shield)
                canvas.drawPath(shield, fillPaint.apply { color = primary })
                canvas.drawRect(cx - radius, cy + radius * 0.05f, cx + radius, cy + radius * 1.2f, fillPaint.apply { color = secondary })
                canvas.restore()
                canvas.drawPath(shield, strokePaint.apply { color = accent; strokeWidth = radius * 0.13f })
                listOf(-0.56f, -0.28f, 0f, 0.28f, 0.56f).forEachIndexed { idx, xOff ->
                    val yOff = -0.34f - (2 - abs(idx - 2)) * 0.08f
                    drawStarAndroid(canvas, cx + radius * xOff, cy + radius * yOff, radius * 0.11f, accent)
                }
                drawAndroidText(canvas, monogram, cx, cy + radius * 0.44f, radius * 0.38f, secInk)
            }
        }
    }

    // =========================================================================
    // Manufacturer Marks & Crisp Outlined Text
    // =========================================================================

    fun DrawScope.drawCrispManufacturerCompose(
        mark: ManufacturerMark,
        color: Color,
        center: Offset,
        size: Float
    ) {
        val outlineCol = if (luminance(color.toAndroidColorInt()) > 0.55) {
            Color.Black.copy(alpha = 0.24f)
        } else {
            Color.White.copy(alpha = 0.18f)
        }
        when (mark) {
            ManufacturerMark.NONE -> Unit
            ManufacturerMark.STRIKER_SWOOSH -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x - size * 0.65f, center.y - size * 0.08f)
                    cubicTo(
                        center.x - size * 0.28f, center.y + size * 0.52f,
                        center.x + size * 0.12f, center.y + size * 0.24f,
                        center.x + size * 0.72f, center.y - size * 0.38f
                    )
                    cubicTo(
                        center.x + size * 0.08f, center.y + size * 0.06f,
                        center.x - size * 0.30f, center.y + size * 0.18f,
                        center.x - size * 0.65f, center.y - size * 0.08f
                    )
                    close()
                }
                drawPath(path, color = outlineCol, style = Stroke(width = size * 0.07f, join = StrokeJoin.Round))
                drawPath(path, color = color)
            }
            ManufacturerMark.TRIPLE_PEAK -> {
                for (i in 0..2) {
                    val barH = size * (0.34f + i * 0.26f)
                    val bx = center.x - size * 0.52f + i * size * 0.34f
                    val base = center.y + size * 0.38f
                    val slant = androidx.compose.ui.graphics.Path().apply {
                        moveTo(bx, base)
                        lineTo(bx + size * 0.22f, base)
                        lineTo(bx + size * 0.32f, base - barH)
                        lineTo(bx + size * 0.10f, base - barH)
                        close()
                    }
                    drawPath(slant, color = outlineCol, style = Stroke(width = size * 0.05f))
                    drawPath(slant, color = color)
                }
            }
            ManufacturerMark.LEAP_CAT -> {
                val pounce = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x - size * 0.58f, center.y + size * 0.25f)
                    quadraticTo(center.x - size * 0.05f, center.y - size * 0.52f, center.x + size * 0.58f, center.y - size * 0.16f)
                    quadraticTo(center.x + size * 0.15f, center.y - size * 0.04f, center.x - size * 0.25f, center.y + size * 0.28f)
                    close()
                }
                drawPath(pounce, color = outlineCol, style = Stroke(width = size * 0.06f))
                drawPath(pounce, color = color)
            }
            ManufacturerMark.DIAMOND_CREST -> {
                val outer = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - size * 0.44f)
                    lineTo(center.x + size * 0.62f, center.y)
                    lineTo(center.x, center.y + size * 0.44f)
                    lineTo(center.x - size * 0.62f, center.y)
                    close()
                }
                val inner = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - size * 0.22f)
                    lineTo(center.x + size * 0.32f, center.y)
                    lineTo(center.x, center.y + size * 0.22f)
                    lineTo(center.x - size * 0.32f, center.y)
                    close()
                }
                drawPath(outer, color = color, style = Stroke(width = size * 0.13f, join = StrokeJoin.Miter))
                drawPath(inner, color = color, style = Stroke(width = size * 0.08f, join = StrokeJoin.Miter))
            }
        }
    }

    fun drawCrispManufacturerAndroid(
        canvas: Canvas,
        mark: ManufacturerMark,
        color: Int,
        cx: Float,
        cy: Float,
        size: Float
    ) {
        val fillP = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.MITER
        }
        when (mark) {
            ManufacturerMark.NONE -> Unit
            ManufacturerMark.STRIKER_SWOOSH -> {
                val path = Path().apply {
                    moveTo(cx - size * 0.65f, cy - size * 0.08f)
                    cubicTo(
                        cx - size * 0.28f, cy + size * 0.52f,
                        cx + size * 0.12f, cy + size * 0.24f,
                        cx + size * 0.72f, cy - size * 0.38f
                    )
                    cubicTo(
                        cx + size * 0.08f, cy + size * 0.06f,
                        cx - size * 0.30f, cy + size * 0.18f,
                        cx - size * 0.65f, cy - size * 0.08f
                    )
                    close()
                }
                canvas.drawPath(path, fillP)
            }
            ManufacturerMark.TRIPLE_PEAK -> {
                val base = cy + size * 0.38f
                for (i in 0..2) {
                    val barH = size * (0.34f + i * 0.26f)
                    val bx = cx - size * 0.52f + i * size * 0.34f
                    val slant = Path().apply {
                        moveTo(bx, base)
                        lineTo(bx + size * 0.22f, base)
                        lineTo(bx + size * 0.32f, base - barH)
                        lineTo(bx + size * 0.10f, base - barH)
                        close()
                    }
                    canvas.drawPath(slant, fillP)
                }
            }
            ManufacturerMark.LEAP_CAT -> {
                val arc = Path().apply {
                    moveTo(cx - size * 0.58f, cy + size * 0.25f)
                    quadTo(cx - size * 0.05f, cy - size * 0.52f, cx + size * 0.58f, cy - size * 0.16f)
                    quadTo(cx + size * 0.15f, cy - size * 0.04f, cx - size * 0.25f, cy + size * 0.28f)
                    close()
                }
                canvas.drawPath(arc, fillP)
            }
            ManufacturerMark.DIAMOND_CREST -> {
                val outer = Path().apply {
                    moveTo(cx, cy - size * 0.44f)
                    lineTo(cx + size * 0.62f, cy)
                    lineTo(cx, cy + size * 0.44f)
                    lineTo(cx - size * 0.62f, cy)
                    close()
                }
                val inner = Path().apply {
                    moveTo(cx, cy - size * 0.22f)
                    lineTo(cx + size * 0.32f, cy)
                    lineTo(cx, cy + size * 0.22f)
                    lineTo(cx - size * 0.32f, cy)
                    close()
                }
                canvas.drawPath(outer, strokeP.apply { strokeWidth = size * 0.14f })
                canvas.drawPath(inner, strokeP.apply { strokeWidth = size * 0.09f })
            }
        }
    }

    fun drawCrispOutlinedText(
        canvas: Canvas,
        text: String,
        cx: Float,
        baselineY: Float,
        textSizePx: Float,
        fillColor: Int,
        typeface: Typeface?,
        letterSpacingEm: Float = 0.06f
    ) {
        val outlineColor = if (luminance(fillColor) > 0.55) {
            android.graphics.Color.argb(210, 10, 14, 24)
        } else {
            android.graphics.Color.argb(180, 255, 255, 255)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = textSizePx
            this.typeface = typeface ?: Typeface.DEFAULT_BOLD
            isFakeBoldText = true
            letterSpacing = letterSpacingEm
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (textSizePx * 0.13f).coerceAtLeast(1.6f)
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = outlineColor
        canvas.drawText(text, cx, baselineY, paint)

        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawText(text, cx, baselineY, paint)
    }

    // =========================================================================
    // Geometry & Shape Helpers
    // =========================================================================

    private fun buildClassicCrestCompose(center: Offset, r: Float): androidx.compose.ui.graphics.Path {
        return androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x - r * 0.88f, center.y - r * 0.86f)
            quadraticTo(center.x, center.y - r * 0.98f, center.x + r * 0.88f, center.y - r * 0.86f)
            lineTo(center.x + r * 0.84f, center.y + r * 0.15f)
            cubicTo(center.x + r * 0.78f, center.y + r * 0.68f, center.x + r * 0.30f, center.y + r * 0.96f, center.x, center.y + r * 1.12f)
            cubicTo(center.x - r * 0.30f, center.y + r * 0.96f, center.x - r * 0.78f, center.y + r * 0.68f, center.x - r * 0.84f, center.y + r * 0.15f)
            close()
        }
    }

    private fun buildScallopedShieldCompose(center: Offset, r: Float): androidx.compose.ui.graphics.Path {
        return androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x - r * 0.86f, center.y - r * 0.82f)
            quadraticTo(center.x - r * 0.42f, center.y - r * 0.68f, center.x, center.y - r * 0.96f)
            quadraticTo(center.x + r * 0.42f, center.y - r * 0.68f, center.x + r * 0.86f, center.y - r * 0.82f)
            lineTo(center.x + r * 0.94f, center.y - r * 0.25f)
            quadraticTo(center.x + r * 0.82f, center.y + r * 0.66f, center.x, center.y + r * 1.10f)
            quadraticTo(center.x - r * 0.82f, center.y + r * 0.66f, center.x - r * 0.94f, center.y - r * 0.25f)
            close()
        }
    }

    private fun buildHexCompose(center: Offset, r: Float): androidx.compose.ui.graphics.Path {
        return androidx.compose.ui.graphics.Path().apply {
            for (i in 0 until 6) {
                val angle = Math.toRadians(-90.0 + i * 60.0)
                val px = center.x + (r * cos(angle)).toFloat()
                val py = center.y + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
    }

    private fun DrawScope.drawCrownCompose(center: Offset, w: Float, color: Color) {
        val h = w * 0.62f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x - w * 0.55f, center.y + h * 0.35f)
            lineTo(center.x - w * 0.62f, center.y - h * 0.35f)
            lineTo(center.x - w * 0.22f, center.y + h * 0.02f)
            lineTo(center.x, center.y - h * 0.55f)
            lineTo(center.x + w * 0.22f, center.y + h * 0.02f)
            lineTo(center.x + w * 0.62f, center.y - h * 0.35f)
            lineTo(center.x + w * 0.55f, center.y + h * 0.35f)
            close()
        }
        drawPath(path, color = color)
    }

    private fun buildClassicCrestAndroid(cx: Float, cy: Float, r: Float): Path {
        return Path().apply {
            moveTo(cx - r * 0.88f, cy - r * 0.86f)
            quadTo(cx, cy - r * 0.98f, cx + r * 0.88f, cy - r * 0.86f)
            lineTo(cx + r * 0.84f, cy + r * 0.15f)
            cubicTo(cx + r * 0.78f, cy + r * 0.68f, cx + r * 0.30f, cy + r * 0.96f, cx, cy + r * 1.12f)
            cubicTo(cx - r * 0.30f, cy + r * 0.96f, cx - r * 0.78f, cy + r * 0.68f, cx - r * 0.84f, cy + r * 0.15f)
            close()
        }
    }

    private fun buildScallopedShieldAndroid(cx: Float, cy: Float, r: Float): Path {
        return Path().apply {
            moveTo(cx - r * 0.86f, cy - r * 0.82f)
            quadTo(cx - r * 0.42f, cy - r * 0.68f, cx, cy - r * 0.96f)
            quadTo(cx + r * 0.42f, cy - r * 0.68f, cx + r * 0.86f, cy - r * 0.82f)
            lineTo(cx + r * 0.94f, cy - r * 0.25f)
            quadTo(cx + r * 0.82f, cy + r * 0.66f, cx, cy + r * 1.10f)
            quadTo(cx - r * 0.82f, cy + r * 0.66f, cx - r * 0.94f, cy - r * 0.25f)
            close()
        }
    }

    private fun buildHexAndroid(cx: Float, cy: Float, r: Float): Path {
        return Path().apply {
            for (i in 0 until 6) {
                val angle = Math.toRadians(-90.0 + i * 60.0)
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
    }

    private fun drawCrownAndroid(canvas: Canvas, cx: Float, cy: Float, w: Float, color: Int) {
        val h = w * 0.62f
        val path = Path().apply {
            moveTo(cx - w * 0.55f, cy + h * 0.35f)
            lineTo(cx - w * 0.62f, cy - h * 0.35f)
            lineTo(cx - w * 0.22f, cy + h * 0.02f)
            lineTo(cx, cy - h * 0.55f)
            lineTo(cx + w * 0.22f, cy + h * 0.02f)
            lineTo(cx + w * 0.62f, cy - h * 0.35f)
            lineTo(cx + w * 0.55f, cy + h * 0.35f)
            close()
        }
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }

    private fun DrawScope.drawStarCompose(center: Offset, r: Float, color: Color) {
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0 until 10) {
            val rad = if (i % 2 == 0) r else r * 0.45f
            val angle = Math.toRadians(-90.0 + i * 36.0)
            val px = center.x + (rad * cos(angle)).toFloat()
            val py = center.y + (rad * sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, color = color)
    }

    private fun drawStarAndroid(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val path = Path()
        for (i in 0 until 10) {
            val rad = if (i % 2 == 0) r else r * 0.45f
            val angle = Math.toRadians(-90.0 + i * 36.0)
            val px = cx + (rad * cos(angle)).toFloat()
            val py = cy + (rad * sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }

    private fun DrawScope.drawMonogramNative(
        cx: Float,
        baselineY: Float,
        textSizePx: Float,
        text: String,
        color: Color
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toAndroidColorInt()
            textAlign = Paint.Align.CENTER
            textSize = textSizePx
            isFakeBoldText = true
            letterSpacing = 0.04f
        }
        drawContext.canvas.nativeCanvas.drawText(text, cx, baselineY, p)
    }

    private fun drawAndroidText(
        canvas: Canvas,
        text: String,
        cx: Float,
        baselineY: Float,
        textSizePx: Float,
        color: Int
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textAlign = Paint.Align.CENTER
            textSize = textSizePx
            isFakeBoldText = true
        }
        canvas.drawText(text, cx, baselineY, p)
    }

    private fun contrastTextFor(bg: Color): Color {
        val lum = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
        return if (lum > 0.60f) Color(0xFF0F172A) else Color.White
    }

    private fun luminance(colorInt: Int): Double {
        return (0.299 * android.graphics.Color.red(colorInt) +
            0.587 * android.graphics.Color.green(colorInt) +
            0.114 * android.graphics.Color.blue(colorInt)) / 255.0
    }
}
