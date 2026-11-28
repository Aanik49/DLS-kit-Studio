package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.LogoDesignSpec
import com.example.data.model.toHexString
import com.example.data.remote.SampleKitCatalog
import com.example.rendering.DlsUvTextureExporter
import com.example.rendering.LogoBitmapExporter
import com.example.rendering.StudioPreviewExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class DlsKitStudioRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesDlsKitStudio() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("DLS Kit Studio", context.getString(R.string.app_name))
    }

    @Test
    fun dlsUvExporterGeneratesExact512x512Texture() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, JerseyDesignSpec(), includeUvGuideOverlay = false)
        assertEquals(512, bmp.width)
        assertEquals(512, bmp.height)
        // Transparent background outside UV islands
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(0, 0)))
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(511, 0)))
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(511, 511)))
        // Neck cutout (326, 193) & empty mid-right gap (480, 310) must be transparent in official DLS 26 mask
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(326, 193)))
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(480, 310)))
        // Opaque official DLS 26 UV islands: outer sleeves, socks, inner sleeves, torso, mid-row sleeves, shorts, dual # squares, primary circle
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(120, 60)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(200, 60)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(300, 50)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(60, 190)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(60, 300)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(200, 194)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(400, 194)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(200, 330)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(370, 330)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(111, 472)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(382, 469)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(478, 355)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(478, 435)) > 200)
        assertTrue(android.graphics.Color.alpha(bmp.getPixel(458, 491)) > 200)
        // Clean transparent moat between Right Shorts and Bottom-Right Primary Color Circle
        assertEquals(0, android.graphics.Color.alpha(bmp.getPixel(436, 485)))
    }

    @Test
    fun dlsUvHasZeroIslandBleedAndCalibratedColorsAndUncutShortsCrest() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Verify DLS 26 Unity shader color calibration preserves pure white & black and prevents dark/red washout
        assertEquals(android.graphics.Color.WHITE, DlsUvTextureExporter.calibrateColorForDls26(android.graphics.Color.WHITE))
        assertEquals(android.graphics.Color.BLACK, DlsUvTextureExporter.calibrateColorForDls26(android.graphics.Color.BLACK))
        val calRed = DlsUvTextureExporter.calibrateColorForDls26(android.graphics.Color.parseColor("#FB090B"))
        assertTrue("calibrated red stays vivid R>240", android.graphics.Color.red(calRed) > 240)
        assertEquals("calibrated red suppresses green washout", 0, android.graphics.Color.green(calRed))
        val calSlate = DlsUvTextureExporter.calibrateColorForDls26(android.graphics.Color.parseColor("#111827"))
        assertTrue("calibrated dark suppresses slate blue lift", android.graphics.Color.blue(calSlate) <= 8)

        // Render AC Milan Home style: red/black stripes, white shorts, black socks
        val milanSpec = JerseyDesignSpec(
            clubName = "AC Milan",
            pattern = com.example.data.model.JerseyPattern.VERTICAL_STRIPES,
            primaryColorHex = "#FB090B",
            secondaryColorHex = "#0B0D12",
            thirdColorHex = "#FFFFFF",
            collarColorHex = "#FFFFFF",
            sleeveColorHex = "#0B0D12",
            cuffColorHex = "#FFFFFF",
            shortsColorHex = "#FFFFFF",
            shortsTrimColorHex = "#0B0D12",
            socksColorHex = "#0B0D12",
            socksTrimColorHex = "#0B0D12",
            playerNumberColorHex = "#FFFFFF",
            shortsNumberColorHex = "#111827",
            crestStyleId = com.example.rendering.ClubCrestStyle.MILAN_OVAL_STRIPES.name,
            crestPrimaryColorHex = "#FB090B",
            crestSecondaryColorHex = "#0B0D12",
            crestAccentColorHex = "#FFFFFF"
        )
        val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, milanSpec, includeUvGuideOverlay = false)

        // 1. ZERO red torso bleed onto bottom edge of socks (previously turned into vertical red stripes on socks!)
        val sockBottomLeft = bmp.getPixel(300, 113)
        val sockBottomRight = bmp.getPixel(400, 112)
        assertTrue("sockBottomLeft opaque", android.graphics.Color.alpha(sockBottomLeft) > 200)
        assertTrue("sockBottomLeft must NOT have red torso bleed", android.graphics.Color.red(sockBottomLeft) < 20)
        assertTrue("sockBottomRight opaque", android.graphics.Color.alpha(sockBottomRight) > 200)
        assertTrue("sockBottomRight must NOT have red torso bleed", android.graphics.Color.red(sockBottomRight) < 20)

        // 2. ZERO red torso bleed onto bottom edge of outer sleeves
        val outerSleeveBottom = bmp.getPixel(180, 118)
        assertTrue("outerSleeveBottom opaque", android.graphics.Color.alpha(outerSleeveBottom) > 200)
        assertTrue("outerSleeveBottom must NOT have red torso bleed", android.graphics.Color.red(outerSleeveBottom) < 20)

        // 3. ZERO primary-circle red bleed onto Right Shorts outer corner (432, 475) & clean transparent moat at (436, 485)
        val shortsOuterCorner = bmp.getPixel(432, 475)
        assertTrue("shortsOuterCorner opaque", android.graphics.Color.alpha(shortsOuterCorner) > 200)
        assertTrue("shortsOuterCorner stays pure white R>240", android.graphics.Color.red(shortsOuterCorner) > 240)
        assertTrue("shortsOuterCorner stays pure white G>240", android.graphics.Color.green(shortsOuterCorner) > 240)
        assertTrue("shortsOuterCorner stays pure white B>240", android.graphics.Color.blue(shortsOuterCorner) > 240)
        assertEquals("transparent moat between shorts & primary circle", 0, android.graphics.Color.alpha(bmp.getPixel(436, 485)))

        // 4. Primary Color Circle (458, 491) is pure solid calibrated primary red
        val priCircle = bmp.getPixel(458, 491)
        assertEquals(255, android.graphics.Color.alpha(priCircle))
        assertEquals(calRed, priCircle)

        // 5. Raised Shorts Club Crest at (382, 469) has clean white shorts margin underneath at (382, 494) so it is never cut off
        val belowCrestHem = bmp.getPixel(382, 494)
        assertTrue("belowCrestHem opaque", android.graphics.Color.alpha(belowCrestHem) > 200)
        assertTrue("belowCrestHem is clean white shorts G>235", android.graphics.Color.green(belowCrestHem) > 235)
        assertTrue("belowCrestHem is clean white shorts B>235", android.graphics.Color.blue(belowCrestHem) > 235)
    }

    @Test
    fun dlsColorProfileSyncMapsJerseyAndShortsAccuratelyAndReducesFrontCrestSize() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val spec = JerseyDesignSpec(
            clubName = "Sync FC",
            pattern = com.example.data.model.JerseyPattern.SOLID,
            primaryColorHex = "#00529F",
            secondaryColorHex = "#FFFFFF",
            thirdColorHex = "#FFDF00",
            shortsColorHex = "#111827",
            shortsTrimColorHex = "#FFDF00",
            socksColorHex = "#00529F",
            playerNumberColorHex = "#FFDF00",
            shortsNumberColorHex = "#FFFFFF",
            crestStyleId = com.example.rendering.ClubCrestStyle.ROYAL_CROWN_RING.name,
            crestPrimaryColorHex = "#FFDF00",
            crestSecondaryColorHex = "#D70022",
            crestAccentColorHex = "#FFFFFF"
        )
        val profile = com.example.rendering.DlsColorProfileSync.buildOfficialProfile(spec)

        // Jersey & shorts fabric slots calibrated for DLS 26 Unity shader; number decals preserved in exact sRGB
        assertEquals("#00529F", profile.jerseyPrimary.sourceHex)
        assertEquals("#001A63", profile.jerseyPrimary.dlsTextureHex)
        assertEquals(profile.jerseyPrimary.dlsTextureColorInt, profile.primaryIndicatorCircle.dlsTextureColorInt)
        assertEquals("#111827", profile.shortsPrimary.sourceHex)
        assertEquals("#010206", profile.shortsPrimary.dlsTextureHex)
        assertEquals("#FFDF00", profile.jerseyNumber.dlsTextureHex)
        assertEquals("#FFFFFF", profile.shortsNumber.dlsTextureHex)
        assertTrue(profile.formatShareableProfileText().contains("Jersey Primary"))
        assertTrue(profile.formatShareableProfileText().contains("Shorts Primary"))

        // Forward-simulated in-game dark shorts stay deep dark (< 60) instead of washing out to slate (> 95)
        val predictedShorts = com.example.rendering.DlsColorProfileSync.predictInGameRenderedColor(
            profile.shortsPrimary.dlsTextureColorInt
        )
        assertTrue("synced shorts stay deep dark in-game", android.graphics.Color.blue(predictedShorts) < 60)

        // Exported UV texture uses exact synced jersey & shorts profile colors AND smaller front chest crest (radius 9.8 vs old 13.5)
        val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, spec, includeUvGuideOverlay = false)
        assertEquals(profile.primaryIndicatorCircle.dlsTextureColorInt, bmp.getPixel(458, 491))
        assertEquals(profile.jerseyNumber.dlsTextureColorInt, bmp.getPixel(478, 355))
        assertEquals(profile.shortsNumber.dlsTextureColorInt, bmp.getPixel(478, 435))

        // Front chest crest center in UV is at x = 325 + 0.22 * 185 = 365.7, y = 194 - 0.16 * 168 = 167.1
        // At 12px away from crest center (366, 155), the old 13.5px crest overlapped, whereas the reduced 9.8px crest leaves clean jersey blue!
        val outsideReducedCrest = bmp.getPixel(366, 155)
        assertTrue("pixel 12px from crest center is clean jersey blue, not yellow/red crest", android.graphics.Color.red(outsideReducedCrest) < 20)
        assertTrue("pixel 12px from crest center has jersey blue channel", android.graphics.Color.blue(outsideReducedCrest) > 70)
    }

    @Test
    fun showcaseAndLogoExportersGenerateExpectedResolutions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val showcase = StudioPreviewExporter.generateJerseyShowcaseBitmap(context, JerseyDesignSpec())
        assertEquals(1024, showcase.width)
        assertEquals(1024, showcase.height)

        val logo512 = LogoBitmapExporter.renderLogoBitmap(context, LogoDesignSpec(), pixelSize = 512, transparentBg = true)
        assertEquals(512, logo512.width)
        assertEquals(512, logo512.height)
    }

    @Test
    fun dlsUvExporterSupportsAll24PatternsAndCustomShortsAndSocksColors() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(com.example.data.model.JerseyPattern.entries.size >= 20)
        assertTrue(com.example.rendering.ClubCrestStyle.entries.size >= 20)

        val expectedCalPrimary = DlsUvTextureExporter.calibrateColorForDls26(android.graphics.Color.parseColor("#0A2463"))
        com.example.data.model.JerseyPattern.entries.forEach { pattern ->
            val spec = JerseyDesignSpec(
                pattern = pattern,
                primaryColorHex = "#0A2463",
                secondaryColorHex = "#00E5FF",
                thirdColorHex = "#FFFFFF",
                sleeveColorHex = "#7C3AED",
                cuffColorHex = "#FFC400",
                shortsColorHex = "#D70022",
                shortsTrimColorHex = "#FF00AA",
                socksColorHex = "#00E676",
                playerNumberColorHex = "#00E5FF",
                shortsNumberColorHex = "#FFDF00",
                crestStyleId = com.example.rendering.ClubCrestStyle.ROYAL_CROWN_RING.name,
                crestPrimaryColorHex = "#FFDF00",
                crestSecondaryColorHex = "#004D98",
                crestAccentColorHex = "#FFFFFF",
                manufacturerColorHex = "#FFDF00",
                sponsorColorHex = "#00E676"
            )
            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, spec, includeUvGuideOverlay = false)
            assertEquals(512, bmp.width)
            assertEquals(512, bmp.height)

            // 1. Bottom-Right Circular Primary Color Indicator (458, 491) MUST match calibrated primaryColorHex exactly!
            val primaryCirclePx = bmp.getPixel(458, 491)
            assertEquals(255, android.graphics.Color.alpha(primaryCirclePx))
            assertEquals(expectedCalPrimary, primaryCirclePx)

            // 2. Dual Official DLS 26 Number Color Squares (independent exact sRGB colors!):
            //    - Top Square (478, 355) = Jersey # Color (#00E5FF)
            val jerseyNumSquarePx = bmp.getPixel(478, 355)
            assertEquals(255, android.graphics.Color.alpha(jerseyNumSquarePx))
            assertEquals(0x00, android.graphics.Color.red(jerseyNumSquarePx))
            assertEquals(0xE5, android.graphics.Color.green(jerseyNumSquarePx))
            assertEquals(0xFF, android.graphics.Color.blue(jerseyNumSquarePx))
            //    - Bottom Square (478, 435) = Shorts # Color (#FFDF00)
            val shortsNumSquarePx = bmp.getPixel(478, 435)
            assertEquals(255, android.graphics.Color.alpha(shortsNumSquarePx))
            assertEquals(0xFF, android.graphics.Color.red(shortsNumSquarePx))
            assertEquals(0xDF, android.graphics.Color.green(shortsNumSquarePx))
            assertEquals(0x00, android.graphics.Color.blue(shortsNumSquarePx))

            // 3. Official DLS 26 Sleeve & Inner-Sleeve Mapping:
            //    - Inner sleeves mid-left (60, 190) & (60, 300) match calibrated sleeveColorHex (#7C3AED -> blue dominant)
            val innerSlv1 = bmp.getPixel(60, 190)
            val innerSlv2 = bmp.getPixel(60, 300)
            assertTrue("innerSlv1 purple blue>180", android.graphics.Color.blue(innerSlv1) > 180)
            assertTrue("innerSlv2 purple blue>180", android.graphics.Color.blue(innerSlv2) > 180)
            //    - Combined Left Sleeve cuffs on RIGHT edge: Top-Mid Left (148, 60) & Mid-Row Left (258, 330) = calibrated #FFC400 gold
            val topLeftCuff = bmp.getPixel(148, 60)
            val midLeftCuff = bmp.getPixel(258, 330)
            assertTrue("topLeftCuff gold red>200", android.graphics.Color.red(topLeftCuff) > 200)
            assertTrue("topLeftCuff gold green>100", android.graphics.Color.green(topLeftCuff) > 100)
            assertTrue("midLeftCuff gold red>200", android.graphics.Color.red(midLeftCuff) > 200)
            assertTrue("midLeftCuff gold green>100", android.graphics.Color.green(midLeftCuff) > 100)
            //    - Combined Right Sleeve cuffs on LEFT edge: Top-Mid Right (170, 60) & Mid-Row Right (302, 330) = calibrated #FFC400 gold
            val topRightCuff = bmp.getPixel(170, 60)
            val midRightCuff = bmp.getPixel(302, 330)
            assertTrue("topRightCuff gold red>200", android.graphics.Color.red(topRightCuff) > 200)
            assertTrue("midRightCuff gold green>100", android.graphics.Color.green(midRightCuff) > 100)

            // 4. Official DLS 26 Shorts Alignment:
            //    - Upper Front Right Island (400, 444) stays pure calibrated shorts red (#D70022 -> R=181, G=0)
            val upperThighPx = bmp.getPixel(400, 444)
            assertTrue("upperThigh red>150", android.graphics.Color.red(upperThighPx) > 150)
            assertTrue("upperThigh green<25", android.graphics.Color.green(upperThighPx) < 25)
            //    - Back of left island (206, 468) stays 100% pure calibrated shorts red
            val backRightLegPx = bmp.getPixel(206, 468)
            assertTrue("backRightLeg red>150", android.graphics.Color.red(backRightLegPx) > 150)
            assertTrue("backRightLeg green<25", android.graphics.Color.green(backRightLegPx) < 25)
            //    - Slanted outer-hip shorts stripes (calibrated #FF00AA -> R=255, B=113) at (146, 450) and (352, 450)
            val leftHipStripePx = bmp.getPixel(146, 450)
            val rightHipStripePx = bmp.getPixel(352, 450)
            assertTrue("leftHip red>200", android.graphics.Color.red(leftHipStripePx) > 200)
            assertTrue("leftHip blue>90", android.graphics.Color.blue(leftHipStripePx) > 90)
            assertTrue("rightHip blue>90", android.graphics.Color.blue(rightHipStripePx) > 90)

            // 5. Custom socksColorHex (calibrated #00E676 -> green dominant ~207) inside top-right socks
            val topSocksPx = bmp.getPixel(430, 60)
            assertTrue("topSocks alpha", android.graphics.Color.alpha(topSocksPx) > 200)
            assertTrue("topSocks green=${Integer.toHexString(topSocksPx)}", android.graphics.Color.green(topSocksPx) > 160)
        }
    }

    @Test
    fun colorHexAndHslConversionsAreExactWithoutDrift() {
        val samples = listOf("#FFFFFF", "#000000", "#D70022", "#004D98", "#00E5FF", "#FFDF00", "#0A2463", "#018749")
        samples.forEach { hex ->
            val parsed = com.example.data.model.parseColorHex(hex)
            assertEquals(hex, parsed.toHexString())
            val (h, s, l) = com.example.data.model.hexToHsl(hex)
            val roundTrip = com.example.data.model.hslToHex(h, s, l)
            assertEquals("HSL round-trip for $hex", hex, roundTrip)
        }
    }

    @Test
    fun uploadedDls26KitAnalyzerValidatesOfficial512UvLayout() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val officialBmp = DlsUvTextureExporter.generateDlsUvBitmap(context, JerseyDesignSpec(), includeUvGuideOverlay = false)
        val tmpFile = java.io.File(context.cacheDir, "test_dls26_upload.png")
        java.io.FileOutputStream(tmpFile).use { out ->
            officialBmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        val analysis = DlsUvTextureExporter.analyzeUploadedDlsKit(context, android.net.Uri.fromFile(tmpFile))
        assertTrue(analysis != null)
        assertEquals(512, analysis!!.originalWidth)
        assertEquals(512, analysis.originalHeight)
        assertTrue("expected aligned maskCoverage=${analysis.maskCoveragePercent}", analysis.isOfficialDls26Aligned)
        assertTrue(analysis.hasTransparentBackground)
        tmpFile.delete()
    }

    @Test
    fun catalogCoversAllRequestedCategories() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clubs = SampleKitCatalog.clubs
        assertTrue(clubs.size >= 30)
        assertTrue(clubs.all { it.variants.size == 4 })

        val templates = SampleKitCatalog.featuredTemplates
        assertTrue("Expected at least 24 featured templates, got ${templates.size}", templates.size >= 24)
        assertEquals("All featured template IDs must be unique", templates.size, templates.map { it.id }.toSet().size)
        templates.forEach { tpl ->
            assertTrue("Template title must not be blank", tpl.title.isNotBlank())
            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, tpl.designSpec, includeUvGuideOverlay = false)
            assertEquals(512, bmp.width)
            assertEquals(512, bmp.height)
            assertEquals(255, android.graphics.Color.alpha(bmp.getPixel(458, 491)))
        }
    }

    @Test
    fun bottomNavigationAndCreatorsRenderCleanly() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_kits").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_create").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_logos").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_profile").assertIsDisplayed()

        // Navigate to Official DLS 26 Kit Uploader & Link Generator from Home
        composeRule.onNodeWithTag("home_upload_kit_link_card").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("upload_pick_gallery_button").assertIsDisplayed()
        composeRule.onNodeWithTag("upload_pick_file_button").assertIsDisplayed()
        composeRule.onNodeWithTag("uploader_back_button").performClick()
        composeRule.waitForIdle()

        // Navigate to Kits and search
        composeRule.onNodeWithTag("nav_tab_kits").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("kits_upload_link_button").assertIsDisplayed()
        composeRule.onNodeWithTag("kits_search_input").performTextInput("Brazil")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Brazil National Team").assertIsDisplayed()

        // Navigate to Kit Creator Studio & check 360° Controls, GK Studio, Visual Pattern Grid, Details (no Collar/Sleeve Style), Branding (24 Crests + Sponsor/Badge Gallery + Separate Jersey/Shorts # Colors), and Export (Matchday Poster)
        composeRule.onNodeWithTag("nav_tab_create").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stage_right_chip").performScrollTo().performClick()
        composeRule.onNodeWithTag("stage_left_chip").performScrollTo().performClick()
        composeRule.onNodeWithTag("stage_front_chip").performScrollTo().performClick()
        composeRule.onNodeWithTag("stage_full_kit_chip").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("kit_tab_GK_STUDIO").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gk_studio_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("gk_gloves_preview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("kit_tab_PATTERN").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("visual_pattern_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("kit_tab_DETAILS").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Collar Style").assertDoesNotExist()
        composeRule.onNodeWithText("Sleeve Style").assertDoesNotExist()
        composeRule.onNodeWithTag("kit_tab_BRANDING").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("crest_picker_row").assertIsDisplayed()
        composeRule.onNodeWithTag("manufacturer_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("sponsor_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("sponsor_gallery_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("badges_and_patches_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("custom_sticker_overlay_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("player_number_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("shorts_number_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("kit_tab_EXPORT").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("export_matchday_poster_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("kit_tab_COLORS").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shorts_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("socks_color_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("dls_color_profile_sync_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stage_back_chip").performScrollTo().performClick()
        composeRule.onNodeWithTag("stage_uv_chip").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Navigate to Logo Creator
        composeRule.onNodeWithTag("nav_tab_logos").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("interactive_logo_canvas").assertIsDisplayed()

        // Navigate to Profile
        composeRule.onNodeWithTag("nav_tab_profile").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_my_designs_card").assertIsDisplayed()
    }

    @Test
    fun qrCodeAndMatchdayPosterExporterGenerateValidVerticalPoster() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testUrl = "https://files.catbox.moe/dls26kit.png"
        val matrix = com.example.rendering.QrCodeEncoder.encode(testUrl)
        assertTrue("QR matrix size >= 25 for URL", matrix.size >= 25)
        // Top-left finder pattern center (3, 3) and border (0, 0) must be dark
        assertTrue("Finder (0,0) dark", matrix.isDark(0, 0))
        assertTrue("Finder (3,3) dark", matrix.isDark(3, 3))
        assertTrue("Finder separator (1,1) light", !matrix.isDark(1, 1))

        val qrBmp = com.example.rendering.QrCodeEncoder.renderQrBitmap(testUrl, pixelSize = 240)
        assertEquals(240, qrBmp.width)
        assertEquals(240, qrBmp.height)

        val posterSpec = JerseyDesignSpec(
            clubName = "Miami Stars",
            playerName = "MESSI",
            playerNumber = "10",
            sponsorText = "APEX ENERGY",
            sponsorStyle = com.example.data.model.SponsorStyle.AERO_WINGS,
            chestBadge = com.example.data.model.ChestBadgeStyle.WORLD_CHAMPION_GOLD,
            sleevePatch = com.example.data.model.SleevePatchStyle.UCL_STARBALL,
            captainArmbandEnabled = true
        )
        val poster = com.example.rendering.MatchdayPosterExporter.generateMatchdayPosterBitmap(
            context = context,
            spec = posterSpec,
            dlsKitUrl = testUrl,
            seasonLabel = "2025/26"
        )
        assertEquals(1080, poster.width)
        assertEquals(1920, poster.height)
        assertEquals(255, android.graphics.Color.alpha(poster.getPixel(540, 960)))
    }

    @Test
    fun goalkeeperStudioCatalogAndPatternsRenderClean512Uv() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gkTemplates = com.example.data.remote.GoalkeeperStudioCatalog.gkTemplates
        val neonPalettes = com.example.data.remote.GoalkeeperStudioCatalog.neonPalettes
        assertTrue("Expected at least 8 GK templates", gkTemplates.size >= 8)
        assertTrue("Expected at least 7 neon GK palettes", neonPalettes.size >= 7)
        val gkPatterns = com.example.data.model.JerseyPattern.entries.filter { it.isGoalkeeper }
        assertTrue("Expected 4 GK-specific patterns", gkPatterns.size >= 4)

        gkTemplates.forEach { tpl ->
            assertEquals(com.example.data.model.KitSlotType.GOALKEEPER, tpl.designSpec.slotType)
            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, tpl.designSpec, includeUvGuideOverlay = false)
            assertEquals(512, bmp.width)
            assertEquals(512, bmp.height)
            assertEquals(255, android.graphics.Color.alpha(bmp.getPixel(458, 491)))
        }
    }

    @Test
    fun sponsorStylesPatchesAndCaptainArmbandRenderOntoDls26Uv() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val spec = JerseyDesignSpec(
            clubName = "Europa Champions",
            pattern = com.example.data.model.JerseyPattern.SOLID,
            primaryColorHex = "#0B1F3A",
            secondaryColorHex = "#FFFFFF",
            sleeveColorHex = "#0B1F3A",
            cuffColorHex = "#0B1F3A",
            sponsorText = "SKYLINE",
            sponsorStyle = com.example.data.model.SponsorStyle.TECH_PILL,
            sponsorColorHex = "#FFFFFF",
            chestBadge = com.example.data.model.ChestBadgeStyle.WORLD_CHAMPION_GOLD,
            sleevePatch = com.example.data.model.SleevePatchStyle.UCL_STARBALL,
            captainArmbandEnabled = true,
            captainArmbandColorHex = "#FFDF00"
        )
        val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, spec, includeUvGuideOverlay = false)
        assertEquals(512, bmp.width)
        assertEquals(512, bmp.height)

        // Left sleeve mid-row UV (220, 326) should contain calibrated yellow captain armband pixels!
        var foundYellowArmband = false
        for (x in 190..248) {
            for (y in 310..346) {
                val px = bmp.getPixel(x, y)
                if (android.graphics.Color.alpha(px) > 200 &&
                    android.graphics.Color.red(px) > 200 &&
                    android.graphics.Color.green(px) > 140 &&
                    android.graphics.Color.blue(px) < 60
                ) {
                    foundYellowArmband = true
                    break
                }
            }
            if (foundYellowArmband) break
        }
        assertTrue("Captain armband yellow band should render in Left Sleeve UV", foundYellowArmband)
    }

    @Test
    fun dlsUvReverseEditorExtractsPaletteAndAppliesRetintAndOverlays() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sourceSpec = JerseyDesignSpec(
            clubName = "Origin FC",
            pattern = com.example.data.model.JerseyPattern.SOLID,
            primaryColorHex = "#D70022",
            secondaryColorHex = "#FFFFFF",
            sleeveColorHex = "#D70022",
            collarColorHex = "#FFFFFF",
            shortsColorHex = "#111827",
            socksColorHex = "#00529F",
            playerNumberColorHex = "#FFDF00",
            shortsNumberColorHex = "#00E5FF"
        )
        val sourceBmp = DlsUvTextureExporter.generateDlsUvBitmap(context, sourceSpec, includeUvGuideOverlay = false)
        val palette = com.example.rendering.DlsUvReverseEditor.extractPaletteFromDlsUv(context, sourceBmp)

        // Exact number squares extracted cleanly
        assertEquals("#FFDF00", palette.jerseyNumberHex)
        assertEquals("#00E5FF", palette.shortsNumberHex)
        // Extracted jersey primary is red-dominant
        val extractedJerseyInt = android.graphics.Color.parseColor(palette.jerseyPrimaryHex)
        assertTrue("Extracted jersey primary should be red-dominant", android.graphics.Color.red(extractedJerseyInt) > 150)

        // Editable spec preserves extracted colors
        val editableSpec = com.example.rendering.DlsUvReverseEditor.toEditableJerseySpec(palette, "Remixed FC")
        assertEquals("Remixed FC", editableSpec.clubName)
        assertEquals("#FFDF00", editableSpec.playerNumberColorHex)
        assertEquals("#00E5FF", editableSpec.shortsNumberColorHex)

        // Re-tint jersey to emerald green (#00B86B) + stamp vector crest & sponsor
        val editedBmp = com.example.rendering.DlsUvReverseEditor.applyReverseEdits(
            context = context,
            source512 = sourceBmp,
            edits = com.example.rendering.DlsUvReverseEditConfig(
                retintJerseyHex = "#00B86B",
                retintJerseyStrength = 0.85f,
                retintShortsHex = "#FFFFFF",
                overlayCrestEnabled = true,
                overlayCrestStyleId = com.example.rendering.ClubCrestStyle.ROYAL_CROWN_RING.name,
                overlaySponsorEnabled = true,
                overlaySponsorText = "NOVA TECH"
            ),
            clubName = "Remixed FC"
        )
        assertEquals(512, editedBmp.width)
        assertEquals(512, editedBmp.height)
        // Primary color indicator circle (458, 491) updated to calibrated #00B86B green
        val updatedCircle = editedBmp.getPixel(458, 491)
        assertTrue("Updated primary circle should be green-dominant", android.graphics.Color.green(updatedCircle) > android.graphics.Color.red(updatedCircle) + 60)
    }
}
