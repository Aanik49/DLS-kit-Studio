package com.example.data.remote

import com.example.data.model.ClubKitBundle
import com.example.data.model.CollarStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.CustomizationTemplate
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.JerseyTemplateFit
import com.example.data.model.KitCategory
import com.example.data.model.KitSlotType
import com.example.data.model.KitVariant
import com.example.data.model.LogoBorderStyle
import com.example.data.model.LogoDesignSpec
import com.example.data.model.LogoElementType
import com.example.data.model.LogoFontStyle
import com.example.data.model.LogoLayer
import com.example.data.model.LogoShape
import com.example.data.model.LogoTemplatePreset
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.SleeveStyle

object SampleKitCatalog {

    private fun buildFourSlotVariants(
        slug: String,
        clubName: String,
        season: String,
        home: JerseyDesignSpec,
        away: JerseyDesignSpec,
        third: JerseyDesignSpec,
        gk: JerseyDesignSpec
    ): List<KitVariant> {
        val cleanSeason = season.replace("/", "")
        return listOf(
            KitVariant(
                slotType = KitSlotType.HOME,
                label = "Home Kit $season",
                kitUrl = "",
                importCode = "DLS26-$cleanSeason-${slug.uppercase()}-HOME",
                designSpec = home.copy(clubName = clubName, slotType = KitSlotType.HOME)
            ),
            KitVariant(
                slotType = KitSlotType.AWAY,
                label = "Away Kit $season",
                kitUrl = "",
                importCode = "DLS26-$cleanSeason-${slug.uppercase()}-AWAY",
                designSpec = away.copy(clubName = clubName, slotType = KitSlotType.AWAY)
            ),
            KitVariant(
                slotType = KitSlotType.THIRD,
                label = "Third Kit $season",
                kitUrl = "",
                importCode = "DLS26-$cleanSeason-${slug.uppercase()}-THIRD",
                designSpec = third.copy(clubName = clubName, slotType = KitSlotType.THIRD)
            ),
            KitVariant(
                slotType = KitSlotType.GOALKEEPER,
                label = "Goalkeeper Kit $season",
                kitUrl = "",
                importCode = "DLS26-$cleanSeason-${slug.uppercase()}-GK",
                designSpec = gk.copy(clubName = clubName, slotType = KitSlotType.GOALKEEPER)
            )
        )
    }

    val clubs: List<ClubKitBundle> = listOf(
        // PREMIER LEAGUE
        ClubKitBundle(
            id = "epl_arsenal",
            clubName = "Arsenal FC",
            shortCode = "ARS",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "Official 2025/26 Gunners match pack: crimson body with crisp white raglan sleeves, dark obsidian away, and cyan/navy third.",
            downloads = 168200,
            popularityRank = 2,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#EF0107",
            crestSecondaryHex = "#9C824A",
            crestEmblem = LogoElementType.CROWN,
            variants = buildFourSlotVariants(
                slug = "arsenal",
                clubName = "Arsenal FC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#EF0107", secondaryColorHex = "#FFFFFF", thirdColorHex = "#063672",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#EF0107",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "SAKA", playerNumber = "7", shortsColorHex = "#FFFFFF", socksColorHex = "#EF0107"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#00E676", thirdColorHex = "#EF0107",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0F172A", cuffColorHex = "#EF0107",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "ODEGAARD", playerNumber = "8", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#6EC1E4", secondaryColorHex = "#1E293B", thirdColorHex = "#F472B6",
                    useGradient = true, gradientEndHex = "#818CF8",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1E293B",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#6EC1E4", cuffColorHex = "#1E293B",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "RICE", playerNumber = "41", shortsColorHex = "#1E293B", socksColorHex = "#6EC1E4"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#10B981", secondaryColorHex = "#064E3B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#064E3B",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#10B981", cuffColorHex = "#064E3B",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "RAYA", playerNumber = "22", shortsColorHex = "#10B981", socksColorHex = "#10B981"
                )
            )
        ),
        ClubKitBundle(
            id = "epl_man_city",
            clubName = "Manchester City",
            shortCode = "MCI",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "Signature Cityzens sky blue home kit with white sash accent, deep navy away, and maroon third kit.",
            downloads = 179400,
            popularityRank = 3,
            isTrending = true,
            isRecentlyAdded = false,
            crestPrimaryHex = "#6CABDD",
            crestSecondaryHex = "#1C2C5B",
            crestEmblem = LogoElementType.EAGLE,
            variants = buildFourSlotVariants(
                slug = "man-city",
                clubName = "Manchester City",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#6CABDD", secondaryColorHex = "#FFFFFF", thirdColorHex = "#1C2C5B",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.RIBBED_TRIM, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#6CABDD", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "ETIHAD AIRWAYS",
                    playerName = "HAALAND", playerNumber = "9", shortsColorHex = "#FFFFFF", socksColorHex = "#6CABDD"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#141B38", secondaryColorHex = "#FFE600", thirdColorHex = "#6CABDD",
                    pattern = JerseyPattern.HORIZONTAL_STRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FFE600",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#141B38", cuffColorHex = "#FFE600",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "ETIHAD AIRWAYS",
                    playerName = "FODEN", playerNumber = "47", shortsColorHex = "#141B38", socksColorHex = "#141B38"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#6D1B3B", secondaryColorHex = "#F59E0B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#F59E0B",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#6D1B3B", cuffColorHex = "#F59E0B",
                    shoulderPattern = ShoulderPattern.CHEVRON_BAND, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "ETIHAD AIRWAYS",
                    playerName = "RODRI", playerNumber = "16", shortsColorHex = "#6D1B3B", socksColorHex = "#6D1B3B"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#FF6D00", secondaryColorHex = "#1E1E24", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1E1E24",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#FF6D00", cuffColorHex = "#1E1E24",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "ETIHAD AIRWAYS",
                    playerName = "EDERSON", playerNumber = "31", shortsColorHex = "#FF6D00", socksColorHex = "#FF6D00"
                )
            )
        ),
        ClubKitBundle(
            id = "epl_liverpool",
            clubName = "Liverpool FC",
            shortCode = "LIV",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "Anfield scarlet home jersey with clean white trim, off-white away kit, and vibrant sea-green third kit.",
            downloads = 161500,
            popularityRank = 4,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#C8102E",
            crestSecondaryHex = "#F6EB61",
            crestEmblem = LogoElementType.WINGS,
            variants = buildFourSlotVariants(
                slug = "liverpool",
                clubName = "Liverpool FC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#C8102E", secondaryColorHex = "#FFFFFF", thirdColorHex = "#F6EB61",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#C8102E", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "STANDARD CHARTERED",
                    playerName = "SALAH", playerNumber = "11", shortsColorHex = "#C8102E", socksColorHex = "#C8102E"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#1E293B", thirdColorHex = "#C8102E",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#C8102E",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F8FAFC", cuffColorHex = "#1E293B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "STANDARD CHARTERED",
                    playerName = "VAN DIJK", playerNumber = "4", shortsColorHex = "#1E293B", socksColorHex = "#F8FAFC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0D9488", secondaryColorHex = "#FFFFFF", thirdColorHex = "#111827",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0D9488", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "STANDARD CHARTERED",
                    playerName = "MAC ALLISTER", playerNumber = "10", shortsColorHex = "#0D9488", socksColorHex = "#0D9488"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#22C55E", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#22C55E",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#111827", cuffColorHex = "#22C55E",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "STANDARD CHARTERED",
                    playerName = "ALISSON", playerNumber = "1", shortsColorHex = "#111827", socksColorHex = "#111827"
                )
            )
        ),

        // LA LIGA
        ClubKitBundle(
            id = "laliga_real_madrid",
            clubName = "Real Madrid",
            shortCode = "RMA",
            country = "Spain",
            league = "La Liga",
            category = KitCategory.LA_LIGA,
            season = "2025/26",
            info = "Santiago Bernabéu iconic pure white home kit with gold & obsidian piping, midnight blue away, and royal blue third.",
            downloads = 248400,
            popularityRank = 1,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#FEBE10",
            crestEmblem = LogoElementType.CROWN,
            variants = buildFourSlotVariants(
                slug = "real-madrid",
                clubName = "Real Madrid",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#111827", thirdColorHex = "#FEBE10",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#FEBE10",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "MBAPPE", playerNumber = "9", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0B193D", secondaryColorHex = "#93C5FD", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#93C5FD",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0B193D", cuffColorHex = "#93C5FD",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "BELLINGHAM", playerNumber = "5", shortsColorHex = "#0B193D", socksColorHex = "#0B193D"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#1D4ED8", secondaryColorHex = "#FFFFFF", thirdColorHex = "#FEBE10",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#1D4ED8", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "VINICIUS JR", playerNumber = "7", shortsColorHex = "#1D4ED8", socksColorHex = "#1D4ED8"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#0EA5E9", secondaryColorHex = "#0C4A6E", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0C4A6E",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#0EA5E9", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "COURTOIS", playerNumber = "1", shortsColorHex = "#0EA5E9", socksColorHex = "#0EA5E9"
                )
            )
        ),
        ClubKitBundle(
            id = "laliga_barcelona",
            clubName = "FC Barcelona",
            shortCode = "FCB",
            country = "Spain",
            league = "La Liga",
            category = KitCategory.LA_LIGA,
            season = "2025/26",
            info = "Classic Blaugrana royal blue & garnet vertical stripes with gold crest accents and obsidian away jersey.",
            downloads = 219700,
            popularityRank = 2,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#004D98",
            crestSecondaryHex = "#A50044",
            crestEmblem = LogoElementType.FOOTBALL,
            variants = buildFourSlotVariants(
                slug = "fc-barcelona",
                clubName = "FC Barcelona",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#004D98", secondaryColorHex = "#A50044", thirdColorHex = "#EDBB00",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.RIBBED_TRIM, collarColorHex = "#004D98",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#004D98", cuffColorHex = "#A50044",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "SPOTIFY",
                    playerName = "YAMAL", playerNumber = "19", shortsColorHex = "#004D98", socksColorHex = "#004D98"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FDE68A", secondaryColorHex = "#1E1B4B", thirdColorHex = "#A50044",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#1E1B4B",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FDE68A", cuffColorHex = "#1E1B4B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "SPOTIFY",
                    playerName = "PEDRI", playerNumber = "8", shortsColorHex = "#1E1B4B", socksColorHex = "#FDE68A"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#FF6D00", secondaryColorHex = "#111827", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FF6D00", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "SPOTIFY",
                    playerName = "RAPHINHA", playerNumber = "11", shortsColorHex = "#FF6D00", socksColorHex = "#FF6D00"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#22C55E", secondaryColorHex = "#052E16", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#052E16",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#22C55E", cuffColorHex = "#052E16",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "SPOTIFY",
                    playerName = "TER STEGEN", playerNumber = "1", shortsColorHex = "#22C55E", socksColorHex = "#22C55E"
                )
            )
        ),

        // SERIE A
        ClubKitBundle(
            id = "seriea_inter_milan",
            clubName = "Inter Milan",
            shortCode = "INT",
            country = "Italy",
            league = "Serie A",
            category = KitCategory.SERIE_A,
            season = "2025/26",
            info = "Nerazzurri cobalt and obsidian stripes featuring two-star Scudetto badge styling.",
            downloads = 118400,
            popularityRank = 5,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#010E80",
            crestSecondaryHex = "#D4AF37",
            crestEmblem = LogoElementType.STAR,
            variants = buildFourSlotVariants(
                slug = "inter-milan",
                clubName = "Inter Milan",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#010E80", secondaryColorHex = "#0B0B0D", thirdColorHex = "#D4AF37",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0B0B0D",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#010E80", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "BETSSON SPORT",
                    playerName = "MARTINEZ", playerNumber = "10", shortsColorHex = "#0B0B0D", socksColorHex = "#0B0B0D"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F1F5F9", secondaryColorHex = "#010E80", thirdColorHex = "#D4AF37",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#010E80",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F1F5F9", cuffColorHex = "#010E80",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "BETSSON SPORT",
                    playerName = "BARELLA", playerNumber = "23", shortsColorHex = "#F1F5F9", socksColorHex = "#F1F5F9"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#FF6D00", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FF6D00",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0F172A", cuffColorHex = "#FF6D00",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "BETSSON SPORT",
                    playerName = "BASTONI", playerNumber = "95", shortsColorHex = "#0F172A", socksColorHex = "#FF6D00"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#10B981", secondaryColorHex = "#064E3B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#064E3B",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#10B981", cuffColorHex = "#064E3B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "BETSSON SPORT",
                    playerName = "SOMMER", playerNumber = "1", shortsColorHex = "#10B981", socksColorHex = "#10B981"
                )
            )
        ),
        ClubKitBundle(
            id = "seriea_juventus",
            clubName = "Juventus FC",
            shortCode = "JUV",
            country = "Italy",
            league = "Serie A",
            category = KitCategory.SERIE_A,
            season = "2025/26",
            info = "Bianconeri classic white-and-black vertical stripes with pink/white accent trim.",
            downloads = 104200,
            popularityRank = 6,
            isTrending = false,
            isRecentlyAdded = false,
            crestPrimaryHex = "#111111",
            crestSecondaryHex = "#FFFFFF",
            crestEmblem = LogoElementType.STRIPES,
            variants = buildFourSlotVariants(
                slug = "juventus",
                clubName = "Juventus FC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#111827", thirdColorHex = "#EC4899",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#EC4899",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "JEEP",
                    playerName = "YILDIZ", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#7DD3FC", secondaryColorHex = "#0F172A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.V_NECK, collarColorHex = "#0F172A",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#7DD3FC", cuffColorHex = "#0F172A",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "JEEP",
                    playerName = "VLAHOVIC", playerNumber = "9", shortsColorHex = "#0F172A", socksColorHex = "#7DD3FC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#D4AF37", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0F172A", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "JEEP",
                    playerName = "BREMER", playerNumber = "3", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#EF4444", secondaryColorHex = "#7F1D1D", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#7F1D1D",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#EF4444", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "JEEP",
                    playerName = "DI GREGORIO", playerNumber = "29", shortsColorHex = "#EF4444", socksColorHex = "#EF4444"
                )
            )
        ),

        // BUNDESLIGA
        ClubKitBundle(
            id = "bundesliga_bayern",
            clubName = "FC Bayern Munich",
            shortCode = "FCB",
            country = "Germany",
            league = "Bundesliga",
            category = KitCategory.BUNDESLIGA,
            season = "2025/26",
            info = "Bavarian crimson home shirt with bold white geometric chest styling and Allianz Arena heritage.",
            downloads = 138400,
            popularityRank = 5,
            isTrending = true,
            isRecentlyAdded = false,
            crestPrimaryHex = "#DC052D",
            crestSecondaryHex = "#0066B2",
            crestEmblem = LogoElementType.GEOMETRIC,
            variants = buildFourSlotVariants(
                slug = "bayern-munich",
                clubName = "FC Bayern Munich",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#DC052D", secondaryColorHex = "#FFFFFF", thirdColorHex = "#0066B2",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#DC052D", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "T MOBILE",
                    playerName = "MUSIALA", playerNumber = "42", shortsColorHex = "#DC052D", socksColorHex = "#DC052D"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#DC052D", thirdColorHex = "#1E293B",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#DC052D",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#DC052D",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "T MOBILE",
                    playerName = "KANE", playerNumber = "9", shortsColorHex = "#1E293B", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#00E676", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHECKS, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#00E676",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#111827", cuffColorHex = "#00E676",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "T MOBILE",
                    playerName = "KIMMICH", playerNumber = "6", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#38BDF8", secondaryColorHex = "#0369A1", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0369A1",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#38BDF8", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "T MOBILE",
                    playerName = "NEUER", playerNumber = "1", shortsColorHex = "#38BDF8", socksColorHex = "#38BDF8"
                )
            )
        ),

        // LIGUE 1
        ClubKitBundle(
            id = "ligue1_psg",
            clubName = "Paris Saint-Germain",
            shortCode = "PSG",
            country = "France",
            league = "Ligue 1",
            category = KitCategory.LIGUE_1,
            season = "2025/26",
            info = "Classic Parisian navy with iconic crimson-and-white Hechter center band.",
            downloads = 154800,
            popularityRank = 4,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#004170",
            crestSecondaryHex = "#DA291C",
            crestEmblem = LogoElementType.CROWN,
            variants = buildFourSlotVariants(
                slug = "psg",
                clubName = "Paris Saint-Germain",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#002B5C", secondaryColorHex = "#DA291C", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.STRIPES, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#002B5C", cuffColorHex = "#DA291C",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "QATAR AIRWAYS",
                    playerName = "DEMBELE", playerNumber = "10", shortsColorHex = "#002B5C", socksColorHex = "#002B5C"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#002B5C", thirdColorHex = "#DA291C",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#002B5C",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#002B5C",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "QATAR AIRWAYS",
                    playerName = "BARCOLA", playerNumber = "29", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#DA291C", secondaryColorHex = "#002B5C", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#002B5C",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#DA291C", cuffColorHex = "#002B5C",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "QATAR AIRWAYS",
                    playerName = "HAKIMI", playerNumber = "2", shortsColorHex = "#002B5C", socksColorHex = "#DA291C"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#22C55E", secondaryColorHex = "#14532D", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#14532D",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#22C55E", cuffColorHex = "#14532D",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "QATAR AIRWAYS",
                    playerName = "DONNARUMMA", playerNumber = "99", shortsColorHex = "#22C55E", socksColorHex = "#22C55E"
                )
            )
        ),

        // OTHER LEAGUES
        ClubKitBundle(
            id = "other_inter_miami",
            clubName = "Inter Miami CF",
            shortCode = "MIA",
            country = "USA",
            league = "MLS",
            category = KitCategory.OTHER_LEAGUES,
            season = "2025/26",
            info = "Inter Miami CF pack featuring signature Easy Pink Home, Obsidian Away, and Teal/Orange Retro Third!",
            downloads = 189000,
            popularityRank = 2,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#F7B5CD",
            crestSecondaryHex = "#231F20",
            crestEmblem = LogoElementType.EAGLE,
            variants = buildFourSlotVariants(
                slug = "inter-miami",
                clubName = "Inter Miami CF",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#F48FB1", secondaryColorHex = "#18181B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.V_NECK, collarColorHex = "#18181B",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#F48FB1", cuffColorHex = "#18181B",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "ROYAL CARIBBEAN",
                    playerName = "MESSI", playerNumber = "10", shortsColorHex = "#F48FB1", socksColorHex = "#F48FB1"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#18181B", secondaryColorHex = "#F48FB1", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#F48FB1",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#18181B", cuffColorHex = "#F48FB1",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "ROYAL CARIBBEAN",
                    playerName = "SUAREZ", playerNumber = "9", shortsColorHex = "#18181B", socksColorHex = "#18181B"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#006978", secondaryColorHex = "#FF5722", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#006978",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#006978", cuffColorHex = "#FF5722",
                    shoulderPattern = ShoulderPattern.CHEVRON_BAND, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "ROYAL CARIBBEAN",
                    playerName = "MESSI", playerNumber = "10", shortsColorHex = "#006978", socksColorHex = "#006978"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#A855F7", secondaryColorHex = "#3B0764", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#3B0764",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#A855F7", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "ROYAL CARIBBEAN",
                    playerName = "USTARI", playerNumber = "19", shortsColorHex = "#A855F7", socksColorHex = "#A855F7"
                )
            )
        ),
        ClubKitBundle(
            id = "other_al_nassr",
            clubName = "Al-Nassr FC",
            shortCode = "ALN",
            country = "Saudi Arabia",
            league = "Saudi Pro League",
            category = KitCategory.OTHER_LEAGUES,
            season = "2025/26",
            info = "Championship gold & royal navy Al-Nassr kit set featuring crisp Adidas 3-stripes.",
            downloads = 142300,
            popularityRank = 5,
            isTrending = true,
            isRecentlyAdded = false,
            crestPrimaryHex = "#FFD100",
            crestSecondaryHex = "#003087",
            crestEmblem = LogoElementType.LION,
            variants = buildFourSlotVariants(
                slug = "al-nassr",
                clubName = "Al-Nassr FC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFD100", secondaryColorHex = "#003087", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#003087",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#003087", cuffColorHex = "#FFD100",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "KAFD",
                    playerName = "RONALDO", playerNumber = "7", shortsColorHex = "#003087", socksColorHex = "#FFD100"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0A1931", secondaryColorHex = "#FFD100", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFD100",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0A1931", cuffColorHex = "#FFD100",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "KAFD",
                    playerName = "MANE", playerNumber = "10", shortsColorHex = "#0A1931", socksColorHex = "#0A1931"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#003087", thirdColorHex = "#FFD100",
                    pattern = JerseyPattern.HOOPS, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#003087",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#FFD100",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "KAFD",
                    playerName = "BROZOVIC", playerNumber = "77", shortsColorHex = "#003087", socksColorHex = "#FFFFFF"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#E11D48", secondaryColorHex = "#4C0519", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#4C0519",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#E11D48", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "KAFD",
                    playerName = "BENTO", playerNumber = "24", shortsColorHex = "#E11D48", socksColorHex = "#E11D48"
                )
            )
        ),

        // NATIONAL TEAMS
        ClubKitBundle(
            id = "nat_brazil",
            clubName = "Brazil National Team",
            shortCode = "BRA",
            country = "Brazil",
            league = "CONMEBOL",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Seleção canary yellow home kit with centered crest, Brazilian green collar trim, and coastal blue away.",
            downloads = 185600,
            popularityRank = 2,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFDF00",
            crestSecondaryHex = "#009C3B",
            crestEmblem = LogoElementType.STAR,
            variants = buildFourSlotVariants(
                slug = "brazil",
                clubName = "Brazil National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFDF00", secondaryColorHex = "#009C3B", thirdColorHex = "#002776",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.V_NECK, collarColorHex = "#009C3B",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFDF00", cuffColorHex = "#009C3B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    crestPlacement = CrestPlacement.CENTER_CHEST,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "VINI JR", playerNumber = "7", shortsColorHex = "#002776", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0033A0", secondaryColorHex = "#00E5FF", thirdColorHex = "#FFDF00",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFDF00",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0033A0", cuffColorHex = "#00E5FF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "RODRYGO", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#0033A0"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#007A33", secondaryColorHex = "#FFDF00", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FFDF00",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#007A33", cuffColorHex = "#FFDF00",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "RAPHINHA", playerNumber = "11", shortsColorHex = "#007A33", socksColorHex = "#007A33"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#1E293B", secondaryColorHex = "#FFDF00", thirdColorHex = "#009C3B",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFDF00",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#1E293B", cuffColorHex = "#009C3B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "ALISSON", playerNumber = "1", shortsColorHex = "#1E293B", socksColorHex = "#1E293B"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_argentina",
            clubName = "Argentina National Team",
            shortCode = "ARG",
            country = "Argentina",
            league = "CONMEBOL",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "World Champion sky-blue and white stripes with gold crest accents and cobalt away kit.",
            downloads = 208300,
            popularityRank = 1,
            isTrending = true,
            isRecentlyAdded = false,
            crestPrimaryHex = "#75AADB",
            crestSecondaryHex = "#D4AF37",
            crestEmblem = LogoElementType.LEAVES,
            variants = buildFourSlotVariants(
                slug = "argentina",
                clubName = "Argentina National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#75AADB", secondaryColorHex = "#FFFFFF", thirdColorHex = "#D4AF37",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#75AADB",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "MESSI", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#1D4ED8", secondaryColorHex = "#75AADB", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#75AADB",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#1D4ED8", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "ALVAREZ", playerNumber = "9", shortsColorHex = "#1D4ED8", socksColorHex = "#1D4ED8"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#D4AF37", thirdColorHex = "#75AADB",
                    pattern = JerseyPattern.CHECKS, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "MAC ALLISTER", playerNumber = "20", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#EF4444", secondaryColorHex = "#1E293B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1E293B",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#EF4444", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "MARTINEZ", playerNumber = "23", shortsColorHex = "#EF4444", socksColorHex = "#EF4444"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_japan",
            clubName = "Japan National Team",
            shortCode = "JPN",
            country = "Japan",
            league = "AFC",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Samurai Blue deep indigo match shirt with electric flame motif and crisp white away.",
            downloads = 114400,
            popularityRank = 6,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#001F5B",
            crestSecondaryHex = "#E11D48",
            crestEmblem = LogoElementType.EAGLE,
            variants = buildFourSlotVariants(
                slug = "japan",
                clubName = "Japan National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#061844", secondaryColorHex = "#2563EB", thirdColorHex = "#EF4444",
                    useGradient = true, gradientEndHex = "#1E3A8A",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#061844", cuffColorHex = "#EF4444",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "KUBO", playerNumber = "20", shortsColorHex = "#061844", socksColorHex = "#061844"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#EF4444", thirdColorHex = "#061844",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#EF4444",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F8FAFC", cuffColorHex = "#EF4444",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "MITOMA", playerNumber = "7", shortsColorHex = "#061844", socksColorHex = "#F8FAFC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#00E5FF", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#00E5FF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0F172A", cuffColorHex = "#00E5FF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "ENDO", playerNumber = "6", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#F97316", secondaryColorHex = "#431407", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#431407",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#F97316", cuffColorHex = "#431407",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "SUZUKI", playerNumber = "1", shortsColorHex = "#F97316", socksColorHex = "#F97316"
                )
            )
        ),

        // RETRO, CLASSIC, FANTASY
        ClubKitBundle(
            id = "retro_ac_milan_94",
            clubName = "AC Milan 1994 Retro",
            shortCode = "ACM",
            country = "Italy",
            league = "Retro Heritage",
            category = KitCategory.RETRO,
            season = "1993/94",
            info = "Classic Rossoneri 1994 European Cup edition with fold-over polo collar and iconic red-black stripes.",
            downloads = 96300,
            popularityRank = 7,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FB090B",
            crestSecondaryHex = "#111111",
            crestEmblem = LogoElementType.STRIPES,
            variants = buildFourSlotVariants(
                slug = "ac-milan-94",
                clubName = "AC Milan 1994 Retro",
                season = "1993/94",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FB090B", secondaryColorHex = "#111111", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#111111",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#FB090B", cuffColorHex = "#111111",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    templateFit = JerseyTemplateFit.RETRO_LOOSE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MOTTA",
                    playerName = "MALDINI", playerNumber = "3", shortsColorHex = "#FFFFFF", socksColorHex = "#111111"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#FB090B", thirdColorHex = "#111111",
                    pattern = JerseyPattern.HORIZONTAL_STRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FB090B",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#FB090B",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    templateFit = JerseyTemplateFit.RETRO_LOOSE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MOTTA",
                    playerName = "BARESI", playerNumber = "6", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111111", secondaryColorHex = "#FB090B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FB090B",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111111", cuffColorHex = "#FB090B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MOTTA",
                    playerName = "SAVICEVIC", playerNumber = "10", shortsColorHex = "#111111", socksColorHex = "#111111"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#15803D", secondaryColorHex = "#FACC15", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FACC15",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#15803D", cuffColorHex = "#FACC15",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MOTTA",
                    playerName = "ROSSI", playerNumber = "1", shortsColorHex = "#111111", socksColorHex = "#15803D"
                )
            )
        ),
        ClubKitBundle(
            id = "classic_brazil_70",
            clubName = "Brazil 1970 Classic",
            shortCode = "B70",
            country = "Brazil",
            league = "Classic Legends",
            category = KitCategory.CLASSIC,
            season = "1970",
            info = "Legendary 1970 World Cup champions canary yellow cotton-cut shirt with green crew collar.",
            downloads = 92100,
            popularityRank = 8,
            isTrending = false,
            isRecentlyAdded = false,
            crestPrimaryHex = "#FFDF00",
            crestSecondaryHex = "#009C3B",
            crestEmblem = LogoElementType.CROWN,
            variants = buildFourSlotVariants(
                slug = "brazil-1970",
                clubName = "Brazil 1970 Classic",
                season = "1970",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FACC15", secondaryColorHex = "#15803D", thirdColorHex = "#1E3A8A",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#15803D",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FACC15", cuffColorHex = "#15803D",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    templateFit = JerseyTemplateFit.CLASSIC_STADIUM,
                    manufacturer = ManufacturerMark.NONE, sponsorText = "",
                    playerName = "PELE", playerNumber = "10", shortsColorHex = "#1E3A8A", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#1E3A8A", secondaryColorHex = "#FFFFFF", thirdColorHex = "#FACC15",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#1E3A8A", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.NONE, sponsorText = "",
                    playerName = "JAIRZINHO", playerNumber = "7", shortsColorHex = "#FFFFFF", socksColorHex = "#1E3A8A"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#15803D", thirdColorHex = "#FACC15",
                    pattern = JerseyPattern.HOOPS, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#15803D",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#15803D",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.NONE, sponsorText = "",
                    playerName = "TOSTAO", playerNumber = "9", shortsColorHex = "#1E3A8A", socksColorHex = "#FFFFFF"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#475569", secondaryColorHex = "#FACC15", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FACC15",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#475569", cuffColorHex = "#FACC15",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.NONE, sponsorText = "",
                    playerName = "FELIX", playerNumber = "1", shortsColorHex = "#111827", socksColorHex = "#475569"
                )
            )
        ),
        ClubKitBundle(
            id = "fantasy_cyber_pulse",
            clubName = "Cyber Pulse Pro XI",
            shortCode = "CPX",
            country = "Fantasy",
            league = "DLS Pro Fantasy",
            category = KitCategory.FANTASY,
            season = "2026 PRO",
            info = "Custom esports match kit featuring midnight navy gradient, electric cyan geometric lines, and neon cuffs.",
            downloads = 141900,
            popularityRank = 3,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#080E1E",
            crestSecondaryHex = "#00E5FF",
            crestEmblem = LogoElementType.TIGER,
            variants = buildFourSlotVariants(
                slug = "cyber-pulse",
                clubName = "Cyber Pulse Pro XI",
                season = "2026 PRO",
                home = JerseyDesignSpec(
                    primaryColorHex = "#0B132B", secondaryColorHex = "#00E5FF", thirdColorHex = "#00E676",
                    useGradient = true, gradientEndHex = "#1D4ED8",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#00E5FF",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#050A18", cuffColorHex = "#00E676",
                    shoulderPattern = ShoulderPattern.CHEVRON_BAND, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "HYPERCORE",
                    playerName = "CYBER", playerNumber = "99", shortsColorHex = "#050A18", socksColorHex = "#00E5FF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#00E5FF", thirdColorHex = "#0B132B",
                    pattern = JerseyPattern.ABSTRACT_MODERN, collarStyle = CollarStyle.V_NECK, collarColorHex = "#0B132B",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F8FAFC", cuffColorHex = "#00E5FF",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "HYPERCORE",
                    playerName = "NEXUS", playerNumber = "11", shortsColorHex = "#0B132B", socksColorHex = "#F8FAFC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#022C22", secondaryColorHex = "#00E676", thirdColorHex = "#FFFFFF",
                    useGradient = true, gradientEndHex = "#065F46",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#00E676",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#022C22", cuffColorHex = "#00E676",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "HYPERCORE",
                    playerName = "APEX", playerNumber = "7", shortsColorHex = "#022C22", socksColorHex = "#00E676"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#701A75", secondaryColorHex = "#F472B6", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHECKS, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#F472B6",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#701A75", cuffColorHex = "#F472B6",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "HYPERCORE",
                    playerName = "VORTEX", playerNumber = "1", shortsColorHex = "#701A75", socksColorHex = "#701A75"
                )
            )
        )
    ) + ExpandedClubCatalog.additionalClubs

    val featuredTemplates: List<CustomizationTemplate> = listOf(
        CustomizationTemplate(
            id = "tpl_aero_stripes",
            title = "Aero Pro Stripes",
            subtitle = "Vertical match stripes + V-neck",
            badge = "PRO",
            designSpec = JerseyDesignSpec(
                clubName = "Your Club FC",
                primaryColorHex = "#0F296D",
                secondaryColorHex = "#00E5FF",
                thirdColorHex = "#FFFFFF",
                pattern = JerseyPattern.VERTICAL_STRIPES,
                collarStyle = CollarStyle.V_NECK,
                collarColorHex = "#FFFFFF",
                sleeveColorHex = "#0F296D",
                cuffColorHex = "#00E5FF",
                sponsorText = "VELOCITY",
                playerName = "CAPTAIN",
                playerNumber = "10"
            )
        ),
        CustomizationTemplate(
            id = "tpl_stealth_gradient",
            title = "Midnight Gradient",
            subtitle = "Fade base + neon trim",
            badge = "NEW",
            designSpec = JerseyDesignSpec(
                clubName = "Stealth XI",
                primaryColorHex = "#0B132B",
                secondaryColorHex = "#00E676",
                thirdColorHex = "#FFFFFF",
                useGradient = true,
                gradientEndHex = "#1E3A8A",
                pattern = JerseyPattern.ABSTRACT_MODERN,
                collarStyle = CollarStyle.MANDARIN,
                collarColorHex = "#00E676",
                sleeveStyle = SleeveStyle.RAGLAN,
                sleeveColorHex = "#070B16",
                cuffColorHex = "#00E676",
                sponsorText = "NOVA TECH",
                playerName = "STRIKER",
                playerNumber = "9"
            )
        ),
        CustomizationTemplate(
            id = "tpl_retro_polo",
            title = "90s Heritage Polo",
            subtitle = "Bold hoops + fold collar",
            badge = "RETRO",
            designSpec = JerseyDesignSpec(
                clubName = "Heritage Athletic",
                primaryColorHex = "#B91C1C",
                secondaryColorHex = "#FFFFFF",
                thirdColorHex = "#FACC15",
                pattern = JerseyPattern.HOOPS,
                collarStyle = CollarStyle.POLO_COLLAR,
                collarColorHex = "#FFFFFF",
                sleeveStyle = SleeveStyle.CONTRAST_CUFF,
                sleeveColorHex = "#B91C1C",
                cuffColorHex = "#FACC15",
                templateFit = JerseyTemplateFit.RETRO_LOOSE,
                sponsorText = "FOUNDERS",
                playerName = "LEGEND",
                playerNumber = "7"
            )
        ),
        CustomizationTemplate(
            id = "tpl_thunder_zig",
            title = "Thunderbolt Zigzag",
            subtitle = "High-energy esports wave",
            badge = "HOT",
            designSpec = JerseyDesignSpec(
                clubName = "Volt City FC",
                primaryColorHex = "#1E1B4B",
                secondaryColorHex = "#FFC400",
                thirdColorHex = "#00E5FF",
                pattern = JerseyPattern.ZIGZAG,
                collarStyle = CollarStyle.RIBBED_TRIM,
                collarColorHex = "#FFC400",
                sleeveColorHex = "#1E1B4B",
                cuffColorHex = "#FFC400",
                sponsorText = "VOLT+",
                playerName = "BLITZ",
                playerNumber = "11"
            )
        )
    )

    val logoTemplates: List<LogoTemplatePreset> = listOf(
        LogoTemplatePreset(
            id = "logo_royal_crown",
            title = "Royal Sovereign",
            subtitle = "Classic shield + crown",
            spec = LogoDesignSpec(
                clubName = "ROYAL UNITED",
                initials = "RUFC",
                establishedYear = "1902",
                shape = LogoShape.SHIELD,
                bgColorHex = "#0B1D48",
                secondaryColorHex = "#1D4ED8",
                useGradient = true,
                borderColorHex = "#FFC400",
                borderStyle = LogoBorderStyle.GOLD_TRIM,
                textColorHex = "#FFFFFF",
                fontStyle = LogoFontStyle.CLASSIC_SERIF,
                layers = listOf(
                    LogoLayer("l1", LogoElementType.CROWN, "#FFC400", 0f, -0.28f, 0.85f, 0f, 1f),
                    LogoLayer("l2", LogoElementType.FOOTBALL, "#FFFFFF", 0f, 0.02f, 0.72f, 0f, 0.95f),
                    LogoLayer("l3", LogoElementType.STAR, "#FFC400", -0.25f, -0.05f, 0.35f, -10f, 1f),
                    LogoLayer("l4", LogoElementType.STAR, "#FFC400", 0.25f, -0.05f, 0.35f, 10f, 1f)
                )
            )
        ),
        LogoTemplatePreset(
            id = "logo_cyber_eagle",
            title = "Cyber Eagle",
            subtitle = "Modern badge + wings",
            spec = LogoDesignSpec(
                clubName = "CYBER FALCONS",
                initials = "CF",
                establishedYear = "2026",
                shape = LogoShape.MODERN_BADGE,
                bgColorHex = "#080E1E",
                secondaryColorHex = "#00E5FF",
                useGradient = true,
                borderColorHex = "#00E5FF",
                borderStyle = LogoBorderStyle.DOUBLE,
                textColorHex = "#00E5FF",
                fontStyle = LogoFontStyle.ATHLETIC_CONDENSED,
                layers = listOf(
                    LogoLayer("l1", LogoElementType.WINGS, "#00E5FF", 0f, -0.06f, 1.15f, 0f, 0.35f),
                    LogoLayer("l2", LogoElementType.EAGLE, "#FFFFFF", 0f, -0.06f, 0.92f, 0f, 1f)
                )
            )
        ),
        LogoTemplatePreset(
            id = "logo_emerald_lion",
            title = "Emerald Lions",
            subtitle = "Heritage circle + laurel",
            spec = LogoDesignSpec(
                clubName = "EMERALD ATHLETIC",
                initials = "EA",
                establishedYear = "1974",
                shape = LogoShape.CIRCLE,
                bgColorHex = "#022C22",
                secondaryColorHex = "#00E676",
                useGradient = true,
                borderColorHex = "#00E676",
                borderStyle = LogoBorderStyle.DOUBLE,
                textColorHex = "#FFFFFF",
                fontStyle = LogoFontStyle.MODERN_SANS,
                layers = listOf(
                    LogoLayer("l1", LogoElementType.LEAVES, "#00E676", 0f, 0.04f, 1.12f, 0f, 0.9f),
                    LogoLayer("l2", LogoElementType.LION, "#FFC400", 0f, -0.04f, 0.85f, 0f, 1f)
                )
            )
        ),
        LogoTemplatePreset(
            id = "logo_crimson_tiger",
            title = "Crimson Tigers",
            subtitle = "Sharp diamond crest",
            spec = LogoDesignSpec(
                clubName = "CRIMSON TIGERS",
                initials = "CTFC",
                establishedYear = "2018",
                shape = LogoShape.DIAMOND,
                bgColorHex = "#7F1D1D",
                secondaryColorHex = "#EF4444",
                useGradient = true,
                borderColorHex = "#FFFFFF",
                borderStyle = LogoBorderStyle.SOLID,
                textColorHex = "#FFFFFF",
                fontStyle = LogoFontStyle.ATHLETIC_CONDENSED,
                layers = listOf(
                    LogoLayer("l1", LogoElementType.STRIPES, "#450A0A", 0f, 0f, 1.0f, 0f, 0.45f),
                    LogoLayer("l2", LogoElementType.TIGER, "#FFC400", 0f, -0.05f, 0.9f, 0f, 1f)
                )
            )
        )
    )
}
