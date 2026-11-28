package com.example.rendering

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Pure-Kotlin ISO/IEC 18004 Byte-Mode QR Code encoder (Versions 1–10, Error Correction Level L).
 * Produces genuine, camera-scannable QR codes for DLS 26 custom kit URLs (up to 271 bytes)
 * with zero external dependencies.
 */
object QrCodeEncoder {

    data class QrMatrix(
        val version: Int,
        val size: Int,
        val modules: BooleanArray
    ) {
        fun isDark(x: Int, y: Int): Boolean = modules[y * size + x]
    }

    // Total data codewords, EC codewords per block, and block count for EC Level L (Versions 1..10)
    private data class VersionSpec(
        val version: Int,
        val totalCodewords: Int,
        val ecCodewordsPerBlock: Int,
        val numBlocksGroup1: Int,
        val dataCodewordsGroup1: Int,
        val numBlocksGroup2: Int = 0,
        val dataCodewordsGroup2: Int = 0,
        val alignmentCenters: IntArray = intArrayOf()
    ) {
        val totalDataCodewords: Int
            get() = numBlocksGroup1 * dataCodewordsGroup1 + numBlocksGroup2 * dataCodewordsGroup2
        val byteCapacity: Int
            get() = totalDataCodewords - if (version < 10) 2 else 3
    }

    private val VERSION_SPECS = listOf(
        VersionSpec(1, 26, 7, 1, 19, 0, 0, intArrayOf()),
        VersionSpec(2, 44, 10, 1, 34, 0, 0, intArrayOf(6, 18)),
        VersionSpec(3, 70, 15, 1, 55, 0, 0, intArrayOf(6, 22)),
        VersionSpec(4, 100, 20, 1, 80, 0, 0, intArrayOf(6, 26)),
        VersionSpec(5, 134, 26, 1, 108, 0, 0, intArrayOf(6, 30)),
        VersionSpec(6, 172, 18, 2, 68, 0, 0, intArrayOf(6, 34)),
        VersionSpec(7, 196, 20, 2, 78, 0, 0, intArrayOf(6, 22, 38)),
        VersionSpec(8, 242, 24, 2, 97, 0, 0, intArrayOf(6, 24, 42)),
        VersionSpec(9, 292, 30, 2, 116, 0, 0, intArrayOf(6, 26, 46)),
        VersionSpec(10, 346, 18, 2, 68, 2, 69, intArrayOf(6, 28, 50))
    )

    // Precomputed 15-bit format info strings for EC Level L (01) and masks 0..7 (with BCH + XOR 0x5412)
    private val FORMAT_INFO_L = intArrayOf(
        0x77C4, 0x72F3, 0x7DAA, 0x789D, 0x662F, 0x6318, 0x6C41, 0x6976
    )

    // 18-bit version info strings for Versions 7..10
    private val VERSION_INFO = mapOf(
        7 to 0x07C94,
        8 to 0x085BC,
        9 to 0x09A99,
        10 to 0x0A4D3
    )

    // GF(256) exp & log tables for polynomial x^8 + x^4 + x^3 + x^2 + 1 (0x11D)
    private val GF_EXP = IntArray(512)
    private val GF_LOG = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            GF_EXP[i] = x
            GF_LOG[x] = i
            x = x shl 1
            if (x and 0x100 != 0) x = x xor 0x11D
        }
        for (i in 255 until 512) {
            GF_EXP[i] = GF_EXP[i - 255]
        }
    }

    fun encode(text: String): QrMatrix {
        val payload = text.ifBlank { "https://dlskitstudio.app" }
        val rawBytes = payload.toByteArray(Charsets.UTF_8)
        val truncated = if (rawBytes.size > 270) rawBytes.copyOf(270) else rawBytes
        val spec = VERSION_SPECS.firstOrNull { truncated.size <= it.byteCapacity } ?: VERSION_SPECS.last()
        val dataBytes = if (truncated.size > spec.byteCapacity) truncated.copyOf(spec.byteCapacity) else truncated

        val dataCodewords = buildDataCodewords(dataBytes, spec)
        val interleaved = interleaveWithErrorCorrection(dataCodewords, spec)
        val size = 17 + spec.version * 4

        val baseModules = BooleanArray(size * size)
        val isFunction = BooleanArray(size * size)
        placeFunctionPatterns(spec, size, baseModules, isFunction)
        placeDataBits(size, baseModules, isFunction, interleaved)

        // Pick the mask (0..7) with the lowest ISO/IEC 18004 penalty score
        var bestMask = 0
        var bestPenalty = Int.MAX_VALUE
        var bestModules = baseModules
        for (mask in 0..7) {
            val candidate = baseModules.copyOf()
            applyMask(size, candidate, isFunction, mask)
            writeFormatInfo(size, candidate, FORMAT_INFO_L[mask])
            val penalty = computePenalty(size, candidate)
            if (penalty < bestPenalty) {
                bestPenalty = penalty
                bestMask = mask
                bestModules = candidate
            }
        }
        writeFormatInfo(size, bestModules, FORMAT_INFO_L[bestMask])
        return QrMatrix(spec.version, size, bestModules)
    }

    fun renderQrBitmap(
        content: String,
        pixelSize: Int = 320,
        darkColor: Int = Color.parseColor("#0B0D12"),
        lightColor: Int = Color.WHITE,
        quietZoneModules: Int = 3
    ): Bitmap {
        val qr = encode(content)
        val bmp = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(lightColor)

        val totalModules = qr.size + quietZoneModules * 2
        val cell = pixelSize.toFloat() / totalModules.toFloat()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkColor
            style = Paint.Style.FILL
        }

        for (y in 0 until qr.size) {
            for (x in 0 until qr.size) {
                if (qr.isDark(x, y)) {
                    val left = (x + quietZoneModules) * cell
                    val top = (y + quietZoneModules) * cell
                    canvas.drawRect(RectF(left, top, left + cell + 0.35f, top + cell + 0.35f), paint)
                }
            }
        }
        return bmp
    }

    private fun buildDataCodewords(bytes: ByteArray, spec: VersionSpec): IntArray {
        val bits = ArrayList<Int>(spec.totalDataCodewords * 8)
        fun appendBits(value: Int, count: Int) {
            for (i in (count - 1) downTo 0) {
                bits.add((value ushr i) and 1)
            }
        }

        // Byte mode indicator (0100)
        appendBits(0b0100, 4)
        // Character count indicator (8 bits for V1-V9, 16 bits for V10+)
        val countBits = if (spec.version < 10) 8 else 16
        appendBits(bytes.size, countBits)
        for (b in bytes) {
            appendBits(b.toInt() and 0xFF, 8)
        }

        val maxBits = spec.totalDataCodewords * 8
        // Terminator up to 4 zero bits
        val termLen = minOf(4, maxBits - bits.size)
        repeat(termLen) { bits.add(0) }
        // Pad to byte boundary
        while (bits.size % 8 != 0) bits.add(0)

        val out = IntArray(spec.totalDataCodewords)
        var idx = 0
        var i = 0
        while (i < bits.size && idx < out.size) {
            var v = 0
            for (k in 0 until 8) v = (v shl 1) or bits[i + k]
            out[idx++] = v
            i += 8
        }
        // Pad alternating 0xEC, 0x11
        var padToggle = true
        while (idx < out.size) {
            out[idx++] = if (padToggle) 0xEC else 0x11
            padToggle = !padToggle
        }
        return out
    }

    private fun interleaveWithErrorCorrection(data: IntArray, spec: VersionSpec): IntArray {
        val gen = rsGeneratorPoly(spec.ecCodewordsPerBlock)
        val dataBlocks = ArrayList<IntArray>()
        val ecBlocks = ArrayList<IntArray>()
        var offset = 0

        repeat(spec.numBlocksGroup1) {
            val block = data.copyOfRange(offset, offset + spec.dataCodewordsGroup1)
            offset += spec.dataCodewordsGroup1
            dataBlocks.add(block)
            ecBlocks.add(rsRemainder(block, gen))
        }
        repeat(spec.numBlocksGroup2) {
            val block = data.copyOfRange(offset, offset + spec.dataCodewordsGroup2)
            offset += spec.dataCodewordsGroup2
            dataBlocks.add(block)
            ecBlocks.add(rsRemainder(block, gen))
        }

        val result = ArrayList<Int>(spec.totalCodewords)
        val maxDataLen = maxOf(spec.dataCodewordsGroup1, spec.dataCodewordsGroup2)
        for (i in 0 until maxDataLen) {
            for (blk in dataBlocks) {
                if (i < blk.size) result.add(blk[i])
            }
        }
        for (i in 0 until spec.ecCodewordsPerBlock) {
            for (ec in ecBlocks) {
                result.add(ec[i])
            }
        }
        return result.toIntArray()
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return GF_EXP[GF_LOG[a] + GF_LOG[b]]
    }

    private fun rsGeneratorPoly(degree: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until degree) {
            val next = IntArray(poly.size + 1)
            val root = GF_EXP[i]
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor gfMul(poly[j], root)
            }
            poly = next
        }
        return poly
    }

    private fun rsRemainder(data: IntArray, gen: IntArray): IntArray {
        val ecLen = gen.size - 1
        val rem = IntArray(ecLen)
        for (b in data) {
            val factor = b xor rem[0]
            System.arraycopy(rem, 1, rem, 0, ecLen - 1)
            rem[ecLen - 1] = 0
            if (factor != 0) {
                for (j in 0 until ecLen) {
                    rem[j] = rem[j] xor gfMul(gen[j + 1], factor)
                }
            }
        }
        return rem
    }

    private fun placeFunctionPatterns(
        spec: VersionSpec,
        size: Int,
        modules: BooleanArray,
        isFunction: BooleanArray
    ) {
        fun setFn(x: Int, y: Int, dark: Boolean) {
            if (x in 0 until size && y in 0 until size) {
                modules[y * size + x] = dark
                isFunction[y * size + x] = true
            }
        }

        // Finder patterns + 1-module white separator
        fun drawFinder(ox: Int, oy: Int) {
            for (dy in -1..7) {
                for (dx in -1..7) {
                    val x = ox + dx
                    val y = oy + dy
                    if (x !in 0 until size || y !in 0 until size) continue
                    val dark = dx in 0..6 && dy in 0..6 && (
                        dx == 0 || dx == 6 || dy == 0 || dy == 6 ||
                            (dx in 2..4 && dy in 2..4)
                        )
                    setFn(x, y, dark)
                }
            }
        }
        drawFinder(0, 0)
        drawFinder(size - 7, 0)
        drawFinder(0, size - 7)

        // Timing patterns
        for (i in 8 until size - 8) {
            setFn(i, 6, i % 2 == 0)
            setFn(6, i, i % 2 == 0)
        }

        // Alignment patterns (V2+)
        val centers = spec.alignmentCenters
        for (cy in centers) {
            for (cx in centers) {
                val overlapsFinder =
                    (cx <= 8 && cy <= 8) ||
                        (cx >= size - 9 && cy <= 8) ||
                        (cx <= 8 && cy >= size - 9)
                if (overlapsFinder) continue
                for (dy in -2..2) {
                    for (dx in -2..2) {
                        val dist = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                        setFn(cx + dx, cy + dy, dist != 1)
                    }
                }
            }
        }

        // Dark module
        setFn(8, size - 8, true)

        // Reserve format information areas
        for (i in 0..8) {
            if (i != 6) {
                setFn(8, i, false)
                setFn(i, 8, false)
            }
        }
        for (i in 0 until 8) {
            setFn(size - 1 - i, 8, false)
        }
        for (i in 0 until 7) {
            setFn(8, size - 7 + i, false)
        }

        // Version information areas (V7+)
        val vBits = VERSION_INFO[spec.version]
        if (vBits != null) {
            for (i in 0 until 18) {
                val bit = ((vBits ushr i) and 1) != 0
                val a = size - 11 + (i % 3)
                val b = i / 3
                setFn(a, b, bit)
                setFn(b, a, bit)
            }
        }
    }

    private fun placeDataBits(
        size: Int,
        modules: BooleanArray,
        isFunction: BooleanArray,
        codewords: IntArray
    ) {
        var bitIndex = 0
        val totalBits = codewords.size * 8
        var upward = true
        var col = size - 1
        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing column
            for (step in 0 until size) {
                val y = if (upward) size - 1 - step else step
                for (dx in 0..1) {
                    val x = col - dx
                    val idx = y * size + x
                    if (!isFunction[idx]) {
                        val dark = if (bitIndex < totalBits) {
                            ((codewords[bitIndex ushr 3] ushr (7 - (bitIndex and 7))) and 1) != 0
                        } else false
                        modules[idx] = dark
                        bitIndex++
                    }
                }
            }
            upward = !upward
            col -= 2
        }
    }

    private fun applyMask(size: Int, modules: BooleanArray, isFunction: BooleanArray, mask: Int) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val idx = y * size + x
                if (isFunction[idx]) continue
                val invert = when (mask) {
                    0 -> (x + y) % 2 == 0
                    1 -> y % 2 == 0
                    2 -> x % 3 == 0
                    3 -> (x + y) % 3 == 0
                    4 -> ((y / 2) + (x / 3)) % 2 == 0
                    5 -> (x * y) % 2 + (x * y) % 3 == 0
                    6 -> ((x * y) % 2 + (x * y) % 3) % 2 == 0
                    else -> ((x + y) % 2 + (x * y) % 3) % 2 == 0
                }
                if (invert) modules[idx] = !modules[idx]
            }
        }
    }

    private fun writeFormatInfo(size: Int, modules: BooleanArray, formatBits: Int) {
        // Bit 14 (MSB) to bit 0 (LSB)
        fun bit(pos: Int): Boolean = ((formatBits ushr pos) and 1) != 0

        // Around top-left finder
        val topLeftCoords = arrayOf(
            0 to 8, 1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8, 7 to 8, 8 to 8,
            8 to 7, 8 to 5, 8 to 4, 8 to 3, 8 to 2, 8 to 1, 8 to 0
        )
        for (i in 0 until 15) {
            val (x, y) = topLeftCoords[i]
            modules[y * size + x] = bit(14 - i)
        }
        // Split across bottom-left and top-right finders
        for (i in 0 until 7) {
            modules[(size - 1 - i) * size + 8] = bit(14 - i)
        }
        for (i in 7 until 15) {
            modules[8 * size + (size - 15 + i)] = bit(14 - i)
        }
    }

    private fun computePenalty(size: Int, modules: BooleanArray): Int {
        var penalty = 0
        // Rule 1: Runs of 5+ identical modules in rows/columns
        for (y in 0 until size) {
            var runColor = modules[y * size]
            var runLen = 1
            for (x in 1 until size) {
                val c = modules[y * size + x]
                if (c == runColor) runLen++ else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runColor = c
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }
        for (x in 0 until size) {
            var runColor = modules[x]
            var runLen = 1
            for (y in 1 until size) {
                val c = modules[y * size + x]
                if (c == runColor) runLen++ else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runColor = c
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }
        // Rule 2: 2x2 blocks of identical modules
        for (y in 0 until size - 1) {
            for (x in 0 until size - 1) {
                val c = modules[y * size + x]
                if (c == modules[y * size + x + 1] &&
                    c == modules[(y + 1) * size + x] &&
                    c == modules[(y + 1) * size + x + 1]
                ) {
                    penalty += 3
                }
            }
        }
        return penalty
    }
}
