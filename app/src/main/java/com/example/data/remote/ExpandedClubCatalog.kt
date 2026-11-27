package com.example.data.remote

import com.example.data.model.ClubKitBundle
import com.example.data.model.CollarStyle
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.JerseyPattern
import com.example.data.model.KitCategory
import com.example.data.model.KitSlotType
import com.example.data.model.KitVariant
import com.example.data.model.LogoElementType
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.SleeveStyle

object ExpandedClubCatalog {

    private fun pack(
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

    val additionalClubs: List<ClubKitBundle> = listOf(
        // PREMIER LEAGUE ADDITIONS
        ClubKitBundle(
            id = "epl_chelsea",
            clubName = "Chelsea FC",
            shortCode = "CHE",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "Stamford Bridge royal blue home shirt with crisp pinstripes, ice-white away, and obsidian lightning third.",
            downloads = 149200,
            popularityRank = 6,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#034694",
            crestSecondaryHex = "#DBA111",
            crestEmblem = LogoElementType.LION,
            variants = pack(
                slug = "chelsea",
                clubName = "Chelsea FC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#034694", secondaryColorHex = "#60A5FA", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#034694", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "INFINITE ATHLETE",
                    playerName = "PALMER", playerNumber = "20", shortsColorHex = "#034694", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#034694", thirdColorHex = "#EA580C",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.V_NECK, collarColorHex = "#034694",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F8FAFC", cuffColorHex = "#EA580C",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "INFINITE ATHLETE",
                    playerName = "FERNANDEZ", playerNumber = "8", shortsColorHex = "#F8FAFC", socksColorHex = "#034694"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#F43F5E", thirdColorHex = "#22D3EE",
                    pattern = JerseyPattern.LIGHTNING_BOLT, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#22D3EE",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0F172A", cuffColorHex = "#F43F5E",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "INFINITE ATHLETE",
                    playerName = "CAICEDO", playerNumber = "25", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#EA580C", secondaryColorHex = "#431407", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HEX_HONEYCOMB, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#431407",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#EA580C", cuffColorHex = "#431407",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "INFINITE ATHLETE",
                    playerName = "SANCHEZ", playerNumber = "1", shortsColorHex = "#EA580C", socksColorHex = "#EA580C"
                )
            )
        ),
        ClubKitBundle(
            id = "epl_man_utd",
            clubName = "Manchester United",
            shortCode = "MUN",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "Old Trafford scarlet red home jersey with gradient hoops, midnight navy chevron away, and cream heritage third.",
            downloads = 172600,
            popularityRank = 3,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#DA291C",
            crestSecondaryHex = "#FBE122",
            crestEmblem = LogoElementType.SHIELD_INNER,
            variants = pack(
                slug = "man-utd",
                clubName = "Manchester United",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#DA291C", secondaryColorHex = "#991B1B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GRADIENT_HOOPS, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#DA291C", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SNAPDRAGON",
                    playerName = "FERNANDES", playerNumber = "8", shortsColorHex = "#FFFFFF", socksColorHex = "#111827"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0B1D48", secondaryColorHex = "#93C5FD", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHEVRON_V, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#93C5FD",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0B1D48", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SNAPDRAGON",
                    playerName = "MAINOO", playerNumber = "37", shortsColorHex = "#0B1D48", socksColorHex = "#0B1D48"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#F5F5F4", secondaryColorHex = "#111827", thirdColorHex = "#DA291C",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F5F5F4", cuffColorHex = "#DA291C",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.DIAGONAL_TRIM,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SNAPDRAGON",
                    playerName = "GARNACHO", playerNumber = "17", shortsColorHex = "#111827", socksColorHex = "#F5F5F4"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#84CC16", secondaryColorHex = "#1A2E05", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1A2E05",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#84CC16", cuffColorHex = "#1A2E05",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SNAPDRAGON",
                    playerName = "ONANA", playerNumber = "24", shortsColorHex = "#84CC16", socksColorHex = "#84CC16"
                )
            )
        ),
        ClubKitBundle(
            id = "epl_tottenham",
            clubName = "Tottenham Hotspur",
            shortCode = "TOT",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "North London lilywhite home jersey with navy raglan sleeves, cobalt stadium wave away, and mint hex third.",
            downloads = 109400,
            popularityRank = 9,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#132257",
            crestEmblem = LogoElementType.EAGLE,
            variants = pack(
                slug = "tottenham",
                clubName = "Tottenham Hotspur",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#132257", thirdColorHex = "#38BDF8",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#132257",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#132257", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "AIA",
                    playerName = "SON", playerNumber = "7", shortsColorHex = "#132257", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#1D4ED8", secondaryColorHex = "#7DD3FC", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.WAVE_RIPPLE, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#132257",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#1D4ED8", cuffColorHex = "#7DD3FC",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "AIA",
                    playerName = "MADDISON", playerNumber = "10", shortsColorHex = "#132257", socksColorHex = "#7DD3FC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#10B981", secondaryColorHex = "#064E3B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HEX_HONEYCOMB, collarStyle = CollarStyle.V_NECK, collarColorHex = "#064E3B",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#10B981", cuffColorHex = "#064E3B",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "AIA",
                    playerName = "KULUSEVSKI", playerNumber = "21", shortsColorHex = "#064E3B", socksColorHex = "#10B981"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#7C3AED", secondaryColorHex = "#2E1065", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#2E1065",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#7C3AED", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "AIA",
                    playerName = "VICARIO", playerNumber = "1", shortsColorHex = "#7C3AED", socksColorHex = "#7C3AED"
                )
            )
        ),
        ClubKitBundle(
            id = "epl_newcastle",
            clubName = "Newcastle United",
            shortCode = "NEW",
            country = "England",
            league = "Premier League",
            category = KitCategory.PREMIER_LEAGUE,
            season = "2025/26",
            info = "St James' Park black-and-white vertical stripes with burgundy crest hoop away and crisp white pinstripe third.",
            downloads = 98700,
            popularityRank = 10,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#111827",
            crestSecondaryHex = "#D4AF37",
            crestEmblem = LogoElementType.LION,
            variants = pack(
                slug = "newcastle",
                clubName = "Newcastle United",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#111827", thirdColorHex = "#0EA5E9",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SELA",
                    playerName = "ISAK", playerNumber = "14", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#7F1D1D", secondaryColorHex = "#1E3A8A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HOOPS, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#7F1D1D", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SELA",
                    playerName = "GUIMARAES", playerNumber = "39", shortsColorHex = "#1E3A8A", socksColorHex = "#7F1D1D"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#059669", thirdColorHex = "#111827",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.V_NECK, collarColorHex = "#059669",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#F8FAFC", cuffColorHex = "#059669",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SELA",
                    playerName = "GORDON", playerNumber = "10", shortsColorHex = "#059669", socksColorHex = "#F8FAFC"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#FACC15", secondaryColorHex = "#713F12", thirdColorHex = "#111827",
                    pattern = JerseyPattern.DIAMOND_ARGYLE, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#713F12",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#FACC15", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "SELA",
                    playerName = "POPE", playerNumber = "22", shortsColorHex = "#FACC15", socksColorHex = "#FACC15"
                )
            )
        ),

        // LA LIGA ADDITIONS
        ClubKitBundle(
            id = "laliga_atletico",
            clubName = "Atletico Madrid",
            shortCode = "ATM",
            country = "Spain",
            league = "La Liga",
            category = KitCategory.LA_LIGA,
            season = "2025/26",
            info = "Rojiblancos red and white vertical stripes with cobalt shorts, sky double-chevron away, and obsidian sash third.",
            downloads = 112800,
            popularityRank = 7,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#CB3524",
            crestSecondaryHex = "#172554",
            crestEmblem = LogoElementType.STRIPES,
            variants = pack(
                slug = "atletico-madrid",
                clubName = "Atletico Madrid",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#CB3524", thirdColorHex = "#172554",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.V_NECK, collarColorHex = "#172554",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#CB3524", cuffColorHex = "#172554",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "RIYADH AIR",
                    playerName = "GRIEZMANN", playerNumber = "7", shortsColorHex = "#172554", socksColorHex = "#CB3524"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0EA5E9", secondaryColorHex = "#172554", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.DOUBLE_CHEVRON, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#172554",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0EA5E9", cuffColorHex = "#172554",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "RIYADH AIR",
                    playerName = "ALVAREZ", playerNumber = "19", shortsColorHex = "#172554", socksColorHex = "#0EA5E9"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#F43F5E", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHEST_SASH, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#F43F5E",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#F43F5E",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "RIYADH AIR",
                    playerName = "LLORENTE", playerNumber = "14", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#15803D", secondaryColorHex = "#052E16", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#052E16",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#15803D", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "RIYADH AIR",
                    playerName = "OBLAK", playerNumber = "13", shortsColorHex = "#15803D", socksColorHex = "#15803D"
                )
            )
        ),
        ClubKitBundle(
            id = "laliga_athletic",
            clubName = "Athletic Club",
            shortCode = "ATH",
            country = "Spain",
            league = "La Liga",
            category = KitCategory.LA_LIGA,
            season = "2025/26",
            info = "San Mames lions iconic red-and-white stripes, obsidian brush-slash away, and emerald third kit.",
            downloads = 76400,
            popularityRank = 14,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#EE2523",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.LION,
            variants = pack(
                slug = "athletic-club",
                clubName = "Athletic Club",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#EE2523", thirdColorHex = "#111827",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#EE2523", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "KUTXABANK",
                    playerName = "WILLIAMS JR", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#1E293B", secondaryColorHex = "#38BDF8", thirdColorHex = "#EE2523",
                    pattern = JerseyPattern.BRUSH_SLASH, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#38BDF8",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#1E293B", cuffColorHex = "#38BDF8",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "KUTXABANK",
                    playerName = "SANCET", playerNumber = "8", shortsColorHex = "#1E293B", socksColorHex = "#1E293B"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#065F46", secondaryColorHex = "#FFFFFF", thirdColorHex = "#EE2523",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#065F46", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "KUTXABANK",
                    playerName = "VIVIAN", playerNumber = "3", shortsColorHex = "#065F46", socksColorHex = "#065F46"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#F59E0B", secondaryColorHex = "#451A03", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#451A03",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#F59E0B", cuffColorHex = "#451A03",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "KUTXABANK",
                    playerName = "SIMON", playerNumber = "1", shortsColorHex = "#F59E0B", socksColorHex = "#F59E0B"
                )
            )
        ),

        // SERIE A ADDITIONS
        ClubKitBundle(
            id = "seriea_ac_milan_26",
            clubName = "AC Milan",
            shortCode = "MIL",
            country = "Italy",
            league = "Serie A",
            category = KitCategory.SERIE_A,
            season = "2025/26",
            info = "Modern 2025/26 Rossoneri red-black match stripes, classic white red-black chest band away, and grey hex third.",
            downloads = 134500,
            popularityRank = 6,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FB090B",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.SHIELD_INNER,
            variants = pack(
                slug = "ac-milan-26",
                clubName = "AC Milan",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FB090B", secondaryColorHex = "#0B0D12", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.VERTICAL_STRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0B0D12", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "LEAO", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#0B0D12"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#FB090B", thirdColorHex = "#111827",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FB090B",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#FB090B",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "PULISIC", playerNumber = "11", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#334155", secondaryColorHex = "#A3E635", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HEX_HONEYCOMB, collarStyle = CollarStyle.V_NECK, collarColorHex = "#A3E635",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#334155", cuffColorHex = "#A3E635",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "REIJNDERS", playerNumber = "14", shortsColorHex = "#334155", socksColorHex = "#334155"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#06B6D4", secondaryColorHex = "#083344", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#083344",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#06B6D4", cuffColorHex = "#083344",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "EMIRATES FLY BETTER",
                    playerName = "MAIGNAN", playerNumber = "16", shortsColorHex = "#06B6D4", socksColorHex = "#06B6D4"
                )
            )
        ),
        ClubKitBundle(
            id = "seriea_napoli",
            clubName = "SSC Napoli",
            shortCode = "NAP",
            country = "Italy",
            league = "Serie A",
            category = KitCategory.SERIE_A,
            season = "2025/26",
            info = "Partenopei iconic Azzurro sky-blue home jersey, crisp white chevron away, and obsidian gold-cross third.",
            downloads = 91500,
            popularityRank = 11,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#12A0D7",
            crestSecondaryHex = "#0B1D48",
            crestEmblem = LogoElementType.STAR,
            variants = pack(
                slug = "napoli",
                clubName = "SSC Napoli",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#12A0D7", secondaryColorHex = "#FFFFFF", thirdColorHex = "#0B1D48",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#12A0D7", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MSC",
                    playerName = "LUKAKU", playerNumber = "11", shortsColorHex = "#FFFFFF", socksColorHex = "#12A0D7"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#0B1D48", thirdColorHex = "#12A0D7",
                    pattern = JerseyPattern.CHEVRON_V, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#0B1D48",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#0B1D48",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MSC",
                    playerName = "MCTOMINAY", playerNumber = "8", shortsColorHex = "#0B1D48", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#D4AF37", thirdColorHex = "#12A0D7",
                    pattern = JerseyPattern.CROSS_ST_GEORGE, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MSC",
                    playerName = "DI LORENZO", playerNumber = "22", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#F97316", secondaryColorHex = "#431407", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#431407",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#F97316", cuffColorHex = "#431407",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "MSC",
                    playerName = "MERET", playerNumber = "1", shortsColorHex = "#F97316", socksColorHex = "#F97316"
                )
            )
        ),

        // BUNDESLIGA ADDITIONS
        ClubKitBundle(
            id = "bundesliga_dortmund",
            clubName = "Borussia Dortmund",
            shortCode = "BVB",
            country = "Germany",
            league = "Bundesliga",
            category = KitCategory.BUNDESLIGA,
            season = "2025/26",
            info = "Signal Iduna Park BVB cyber yellow & black pinstripes home, aggressive painted-slash away, and cup hoops third.",
            downloads = 127800,
            popularityRank = 6,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FDE100",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.GEOMETRIC,
            variants = pack(
                slug = "dortmund",
                clubName = "Borussia Dortmund",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FDE100", secondaryColorHex = "#111827", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#111827", cuffColorHex = "#FDE100",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "1&1",
                    playerName = "BRANDT", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#FDE100"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#FDE100", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.BRUSH_SLASH, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FDE100",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#FDE100",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "1&1",
                    playerName = "ADEYEMI", playerNumber = "27", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#FDE100", thirdColorHex = "#111827",
                    pattern = JerseyPattern.GRADIENT_HOOPS, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFFFFF", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "EVONIK",
                    playerName = "GUIRASSY", playerNumber = "9", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#DC2626", secondaryColorHex = "#450A0A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#450A0A",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#DC2626", cuffColorHex = "#450A0A",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "1&1",
                    playerName = "KOBEL", playerNumber = "1", shortsColorHex = "#DC2626", socksColorHex = "#DC2626"
                )
            )
        ),
        ClubKitBundle(
            id = "bundesliga_leverkusen",
            clubName = "Bayer Leverkusen",
            shortCode = "B04",
            country = "Germany",
            league = "Bundesliga",
            category = KitCategory.BUNDESLIGA,
            season = "2025/26",
            info = "Die Werkself obsidian black home with bold crimson sash, red heritage cross away, and wave ripple third.",
            downloads = 94600,
            popularityRank = 9,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#E32221",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.LION,
            variants = pack(
                slug = "leverkusen",
                clubName = "Bayer Leverkusen",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#E32221", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHEST_SASH, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#E32221",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#111827", cuffColorHex = "#E32221",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "BARMENIA",
                    playerName = "WIRTZ", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#E32221"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#E32221", secondaryColorHex = "#FFFFFF", thirdColorHex = "#111827",
                    pattern = JerseyPattern.CROSS_ST_GEORGE, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#E32221", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "BARMENIA",
                    playerName = "FRIMPONG", playerNumber = "30", shortsColorHex = "#FFFFFF", socksColorHex = "#E32221"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0369A1", secondaryColorHex = "#38BDF8", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.WAVE_RIPPLE, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0369A1", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "BARMENIA",
                    playerName = "XHAKA", playerNumber = "34", shortsColorHex = "#0369A1", socksColorHex = "#0369A1"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#22C55E", secondaryColorHex = "#052E16", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#052E16",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#22C55E", cuffColorHex = "#052E16",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "BARMENIA",
                    playerName = "HRADECKY", playerNumber = "1", shortsColorHex = "#22C55E", socksColorHex = "#22C55E"
                )
            )
        ),

        // LIGUE 1 ADDITIONS
        ClubKitBundle(
            id = "ligue1_marseille",
            clubName = "Olympique Marseille",
            shortCode = "OM",
            country = "France",
            league = "Ligue 1",
            category = KitCategory.LIGUE_1,
            season = "2025/26",
            info = "Velodrome crisp white home with sky blue chest band, Mediterranean wave away, and navy diamond third.",
            downloads = 88900,
            popularityRank = 11,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#2FAEE0",
            crestEmblem = LogoElementType.STAR,
            variants = pack(
                slug = "marseille",
                clubName = "Olympique Marseille",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#2FAEE0", thirdColorHex = "#0F172A",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#2FAEE0",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFFFFF", cuffColorHex = "#2FAEE0",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "CMA CGM",
                    playerName = "GREENWOOD", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#0EA5E9", secondaryColorHex = "#0B1D48", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.WAVE_RIPPLE, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0EA5E9", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "CMA CGM",
                    playerName = "RABIOT", playerNumber = "25", shortsColorHex = "#0B1D48", socksColorHex = "#0EA5E9"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#2FAEE0", thirdColorHex = "#F59E0B",
                    pattern = JerseyPattern.DIAMOND_ARGYLE, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#2FAEE0",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0F172A", cuffColorHex = "#2FAEE0",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "CMA CGM",
                    playerName = "HOJBJERG", playerNumber = "23", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#F43F5E", secondaryColorHex = "#4C0519", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#4C0519",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#F43F5E", cuffColorHex = "#4C0519",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "CMA CGM",
                    playerName = "RULLI", playerNumber = "1", shortsColorHex = "#F43F5E", socksColorHex = "#F43F5E"
                )
            )
        ),
        ClubKitBundle(
            id = "ligue1_monaco",
            clubName = "AS Monaco",
            shortCode = "ASM",
            country = "Monaco",
            league = "Ligue 1",
            category = KitCategory.LIGUE_1,
            season = "2025/26",
            info = "Principality red and white signature diagonal sash, obsidian gold pinstripe away, and quartered third.",
            downloads = 82400,
            popularityRank = 13,
            isTrending = false,
            isRecentlyAdded = false,
            crestPrimaryHex = "#E51B22",
            crestSecondaryHex = "#D4AF37",
            crestEmblem = LogoElementType.CROWN,
            variants = pack(
                slug = "as-monaco",
                clubName = "AS Monaco",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#E51B22", thirdColorHex = "#D4AF37",
                    pattern = JerseyPattern.CHEST_SASH, collarStyle = CollarStyle.V_NECK, collarColorHex = "#E51B22",
                    sleeveStyle = SleeveStyle.RAGLAN, sleeveColorHex = "#E51B22", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "APM MONACO",
                    playerName = "GOLOVIN", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#E51B22"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#D4AF37", thirdColorHex = "#E51B22",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "APM MONACO",
                    playerName = "MINAMINO", playerNumber = "18", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#F5F5F4", secondaryColorHex = "#E51B22", thirdColorHex = "#111827",
                    pattern = JerseyPattern.QUARTERS, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F5F5F4", cuffColorHex = "#E51B22",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "APM MONACO",
                    playerName = "AKLIOUCHE", playerNumber = "11", shortsColorHex = "#111827", socksColorHex = "#F5F5F4"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#2563EB", secondaryColorHex = "#1E3A8A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1E3A8A",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#2563EB", cuffColorHex = "#1E3A8A",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.DIAMOND_CREST, sponsorText = "APM MONACO",
                    playerName = "MAJECKI", playerNumber = "1", shortsColorHex = "#2563EB", socksColorHex = "#2563EB"
                )
            )
        ),

        // OTHER LEAGUES ADDITIONS
        ClubKitBundle(
            id = "other_al_hilal",
            clubName = "Al-Hilal SFC",
            shortCode = "HIL",
            country = "Saudi Arabia",
            league = "Saudi Pro League",
            category = KitCategory.OTHER_LEAGUES,
            season = "2025/26",
            info = "Riyadh Royal Blue aero hex match jersey, crisp white reverse sash away, and navy lightning third.",
            downloads = 136500,
            popularityRank = 5,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#0055A4",
            crestSecondaryHex = "#FFFFFF",
            crestEmblem = LogoElementType.CROWN,
            variants = pack(
                slug = "al-hilal",
                clubName = "Al-Hilal SFC",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#0055A4", secondaryColorHex = "#38BDF8", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HEX_HONEYCOMB, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0055A4", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "SAVVY",
                    playerName = "MITROVIC", playerNumber = "9", shortsColorHex = "#0055A4", socksColorHex = "#0055A4"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#0055A4", thirdColorHex = "#38BDF8",
                    pattern = JerseyPattern.REVERSE_SASH, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0055A4",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#0055A4",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "SAVVY",
                    playerName = "NEVES", playerNumber = "8", shortsColorHex = "#FFFFFF", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0B132B", secondaryColorHex = "#00E5FF", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.LIGHTNING_BOLT, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#00E5FF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0B132B", cuffColorHex = "#00E5FF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "SAVVY",
                    playerName = "AL-DAWSARI", playerNumber = "29", shortsColorHex = "#0B132B", socksColorHex = "#0B132B"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#FACC15", secondaryColorHex = "#422006", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#422006",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#FACC15", cuffColorHex = "#422006",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "SAVVY",
                    playerName = "BONO", playerNumber = "37", shortsColorHex = "#FACC15", socksColorHex = "#FACC15"
                )
            )
        ),
        ClubKitBundle(
            id = "other_flamengo",
            clubName = "CR Flamengo",
            shortCode = "FLA",
            country = "Brazil",
            league = "Brasileirao",
            category = KitCategory.OTHER_LEAGUES,
            season = "2025/26",
            info = "Maracana Mengao iconic crimson & black horizontal hoops, crisp white chest band away, and black slash third.",
            downloads = 131200,
            popularityRank = 6,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#C4161C",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.SHIELD_INNER,
            variants = pack(
                slug = "flamengo",
                clubName = "CR Flamengo",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#C4161C", secondaryColorHex = "#111827", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HOOPS, collarStyle = CollarStyle.V_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#C4161C",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "PIXBET",
                    playerName = "DE ARRASCAETA", playerNumber = "14", shortsColorHex = "#FFFFFF", socksColorHex = "#111827"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#C4161C", thirdColorHex = "#111827",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#C4161C",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFFFFF", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "PIXBET",
                    playerName = "PEDRO", playerNumber = "9", shortsColorHex = "#111827", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#C4161C", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.BRUSH_SLASH, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#C4161C",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#C4161C",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "PIXBET",
                    playerName = "GERSON", playerNumber = "8", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#10B981", secondaryColorHex = "#064E3B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#064E3B",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#10B981", cuffColorHex = "#064E3B",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "PIXBET",
                    playerName = "ROSSI", playerNumber = "1", shortsColorHex = "#10B981", socksColorHex = "#10B981"
                )
            )
        ),
        ClubKitBundle(
            id = "other_river_plate",
            clubName = "CA River Plate",
            shortCode = "RIV",
            country = "Argentina",
            league = "Liga Profesional",
            category = KitCategory.OTHER_LEAGUES,
            season = "2025/26",
            info = "El Monumental iconic white home kit with crimson La Banda diagonal sash, crimson/black half away, and obsidian third.",
            downloads = 97300,
            popularityRank = 10,
            isTrending = false,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#DA291C",
            crestEmblem = LogoElementType.STRIPES,
            variants = pack(
                slug = "river-plate",
                clubName = "CA River Plate",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#DA291C", thirdColorHex = "#111827",
                    pattern = JerseyPattern.CHEST_SASH, collarStyle = CollarStyle.V_NECK, collarColorHex = "#DA291C",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#DA291C",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "CODERE",
                    playerName = "MASTANTUONO", playerNumber = "30", shortsColorHex = "#111827", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#DA291C", secondaryColorHex = "#111827", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.HALF_AND_HALF, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#DA291C", cuffColorHex = "#111827",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "CODERE",
                    playerName = "BORJA", playerNumber = "9", shortsColorHex = "#111827", socksColorHex = "#DA291C"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#DA291C", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#DA291C",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#111827", cuffColorHex = "#DA291C",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "CODERE",
                    playerName = "LANZINI", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#0EA5E9", secondaryColorHex = "#0C4A6E", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0C4A6E",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#0EA5E9", cuffColorHex = "#0C4A6E",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "CODERE",
                    playerName = "ARMANI", playerNumber = "1", shortsColorHex = "#0EA5E9", socksColorHex = "#0EA5E9"
                )
            )
        ),

        // NATIONAL TEAMS ADDITIONS
        ClubKitBundle(
            id = "nat_france",
            clubName = "France National Team",
            shortCode = "FRA",
            country = "France",
            league = "UEFA",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Les Bleus classic cobalt blue home with tricolor cuffs, crisp pinstripe white away, and midnight chevron third.",
            downloads = 169400,
            popularityRank = 3,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#002395",
            crestSecondaryHex = "#ED2939",
            crestEmblem = LogoElementType.CROWN,
            variants = pack(
                slug = "france",
                clubName = "France National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#0033A0", secondaryColorHex = "#1D4ED8", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#0033A0", cuffColorHex = "#ED2939",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "MBAPPE", playerNumber = "10", shortsColorHex = "#FFFFFF", socksColorHex = "#ED2939"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#0033A0", thirdColorHex = "#ED2939",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#0033A0",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#FFFFFF", cuffColorHex = "#0033A0",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "GRIEZMANN", playerNumber = "7", shortsColorHex = "#0033A0", socksColorHex = "#FFFFFF"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0B132B", secondaryColorHex = "#D4AF37", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.CHEVRON_V, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0B132B", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "TCHOUAMENI", playerNumber = "8", shortsColorHex = "#0B132B", socksColorHex = "#0B132B"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#10B981", secondaryColorHex = "#064E3B", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#064E3B",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#10B981", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "MAIGNAN", playerNumber = "16", shortsColorHex = "#10B981", socksColorHex = "#10B981"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_spain",
            clubName = "Spain National Team",
            shortCode = "ESP",
            country = "Spain",
            league = "UEFA",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "European Champions La Roja crimson wave home shirt, sunburst canary double-chevron away, and navy sash third.",
            downloads = 158100,
            popularityRank = 4,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#AA151B",
            crestSecondaryHex = "#F1BF00",
            crestEmblem = LogoElementType.CROWN,
            variants = pack(
                slug = "spain",
                clubName = "Spain National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#C8102E", secondaryColorHex = "#F1BF00", thirdColorHex = "#0B1D48",
                    pattern = JerseyPattern.WAVE_RIPPLE, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#F1BF00",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#C8102E", cuffColorHex = "#F1BF00",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.VENT_PANEL,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "YAMAL", playerNumber = "19", shortsColorHex = "#0B1D48", socksColorHex = "#C8102E"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#FDE047", secondaryColorHex = "#0EA5E9", thirdColorHex = "#C8102E",
                    pattern = JerseyPattern.DOUBLE_CHEVRON, collarStyle = CollarStyle.V_NECK, collarColorHex = "#0EA5E9",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FDE047", cuffColorHex = "#0EA5E9",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "PEDRI", playerNumber = "20", shortsColorHex = "#0EA5E9", socksColorHex = "#FDE047"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0B1D48", secondaryColorHex = "#C8102E", thirdColorHex = "#F1BF00",
                    pattern = JerseyPattern.CHEST_SASH, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#F1BF00",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0B1D48", cuffColorHex = "#F1BF00",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "RODRI", playerNumber = "16", shortsColorHex = "#0B1D48", socksColorHex = "#0B1D48"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#475569", secondaryColorHex = "#0F172A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0F172A",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#475569", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "SIMON", playerNumber = "23", shortsColorHex = "#475569", socksColorHex = "#475569"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_england",
            clubName = "England National Team",
            shortCode = "ENG",
            country = "England",
            league = "UEFA",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Three Lions white home jersey with navy cuff trim, St. George cross red away, and plum heritage pinstripe third.",
            downloads = 147300,
            popularityRank = 5,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#0A2463",
            crestEmblem = LogoElementType.LION,
            variants = pack(
                slug = "england",
                clubName = "England National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#0A2463", thirdColorHex = "#CE1124",
                    pattern = JerseyPattern.CHEST_BAND, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0A2463",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFFFFF", cuffColorHex = "#0A2463",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "BELLINGHAM", playerNumber = "10", shortsColorHex = "#0A2463", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#CE1124", secondaryColorHex = "#FFFFFF", thirdColorHex = "#0A2463",
                    pattern = JerseyPattern.CROSS_ST_GEORGE, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#CE1124", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "SAKA", playerNumber = "7", shortsColorHex = "#CE1124", socksColorHex = "#CE1124"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#4A154B", secondaryColorHex = "#FDE047", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.PINSTRIPES, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#FDE047",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#4A154B", cuffColorHex = "#FDE047",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "KANE", playerNumber = "9", shortsColorHex = "#4A154B", socksColorHex = "#4A154B"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#38BDF8", secondaryColorHex = "#0369A1", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#0369A1",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#38BDF8", cuffColorHex = "#0369A1",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.STRIKER_SWOOSH, sponsorText = "",
                    playerName = "PICKFORD", playerNumber = "1", shortsColorHex = "#38BDF8", socksColorHex = "#38BDF8"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_germany",
            clubName = "Germany National Team",
            shortCode = "GER",
            country = "Germany",
            league = "UEFA",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Die Mannschaft crisp white home with DFB chevron, vibrant pink/purple gradient away, and obsidian gold third.",
            downloads = 139600,
            popularityRank = 6,
            isTrending = true,
            isRecentlyAdded = false,
            crestPrimaryHex = "#FFFFFF",
            crestSecondaryHex = "#111827",
            crestEmblem = LogoElementType.EAGLE,
            variants = pack(
                slug = "germany",
                clubName = "Germany National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#FFFFFF", secondaryColorHex = "#111827", thirdColorHex = "#DC2626",
                    pattern = JerseyPattern.CHEVRON_V, collarStyle = CollarStyle.V_NECK, collarColorHex = "#111827",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#FFFFFF", cuffColorHex = "#DC2626",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    crestPlacement = CrestPlacement.CENTER_CHEST,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "MUSIALA", playerNumber = "10", shortsColorHex = "#111827", socksColorHex = "#FFFFFF"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#EC4899", secondaryColorHex = "#6D28D9", thirdColorHex = "#FFFFFF",
                    useGradient = true, gradientEndHex = "#4C1D95",
                    pattern = JerseyPattern.GRADIENT_HOOPS, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#FFFFFF",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#EC4899", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "WIRTZ", playerNumber = "17", shortsColorHex = "#4C1D95", socksColorHex = "#4C1D95"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#111827", secondaryColorHex = "#D4AF37", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.GEOMETRIC, collarStyle = CollarStyle.MANDARIN, collarColorHex = "#D4AF37",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#111827", cuffColorHex = "#D4AF37",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "HAVERTZ", playerNumber = "7", shortsColorHex = "#111827", socksColorHex = "#111827"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#2563EB", secondaryColorHex = "#1E3A8A", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.ZIGZAG, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#1E3A8A",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#2563EB", cuffColorHex = "#FFFFFF",
                    shoulderPattern = ShoulderPattern.THREE_STRIPES, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.TRIPLE_PEAK, sponsorText = "",
                    playerName = "TER STEGEN", playerNumber = "1", shortsColorHex = "#2563EB", socksColorHex = "#2563EB"
                )
            )
        ),
        ClubKitBundle(
            id = "nat_portugal",
            clubName = "Portugal National Team",
            shortCode = "POR",
            country = "Portugal",
            league = "UEFA",
            category = KitCategory.NATIONAL_TEAMS,
            season = "2025/26",
            info = "Selecao das Quinas classic crimson & emerald chevron home, Azulejo tile diamond away, and obsidian lightning third.",
            downloads = 152400,
            popularityRank = 4,
            isTrending = true,
            isRecentlyAdded = true,
            crestPrimaryHex = "#DA291C",
            crestSecondaryHex = "#046A38",
            crestEmblem = LogoElementType.SHIELD_INNER,
            variants = pack(
                slug = "portugal",
                clubName = "Portugal National Team",
                season = "2025/26",
                home = JerseyDesignSpec(
                    primaryColorHex = "#DA291C", secondaryColorHex = "#046A38", thirdColorHex = "#FACC15",
                    pattern = JerseyPattern.CHEVRON_V, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#046A38",
                    sleeveStyle = SleeveStyle.CONTRAST_CUFF, sleeveColorHex = "#DA291C", cuffColorHex = "#046A38",
                    shoulderPattern = ShoulderPattern.PIPING, sidePattern = SidePattern.MESH_STRIPE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "",
                    playerName = "RONALDO", playerNumber = "7", shortsColorHex = "#046A38", socksColorHex = "#DA291C"
                ),
                away = JerseyDesignSpec(
                    primaryColorHex = "#F8FAFC", secondaryColorHex = "#1D4ED8", thirdColorHex = "#DA291C",
                    pattern = JerseyPattern.DIAMOND_ARGYLE, collarStyle = CollarStyle.POLO_COLLAR, collarColorHex = "#1D4ED8",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#F8FAFC", cuffColorHex = "#1D4ED8",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "",
                    playerName = "FERNANDES", playerNumber = "8", shortsColorHex = "#1D4ED8", socksColorHex = "#F8FAFC"
                ),
                third = JerseyDesignSpec(
                    primaryColorHex = "#0F172A", secondaryColorHex = "#FACC15", thirdColorHex = "#046A38",
                    pattern = JerseyPattern.LIGHTNING_BOLT, collarStyle = CollarStyle.V_NECK, collarColorHex = "#FACC15",
                    sleeveStyle = SleeveStyle.SET_IN, sleeveColorHex = "#0F172A", cuffColorHex = "#FACC15",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "",
                    playerName = "LEAO", playerNumber = "17", shortsColorHex = "#0F172A", socksColorHex = "#0F172A"
                ),
                gk = JerseyDesignSpec(
                    primaryColorHex = "#F59E0B", secondaryColorHex = "#78350F", thirdColorHex = "#FFFFFF",
                    pattern = JerseyPattern.SOLID, collarStyle = CollarStyle.CREW_NECK, collarColorHex = "#78350F",
                    sleeveStyle = SleeveStyle.LONG_SLEEVE, sleeveColorHex = "#F59E0B", cuffColorHex = "#78350F",
                    shoulderPattern = ShoulderPattern.NONE, sidePattern = SidePattern.NONE,
                    manufacturer = ManufacturerMark.LEAP_CAT, sponsorText = "",
                    playerName = "COSTA", playerNumber = "22", shortsColorHex = "#F59E0B", socksColorHex = "#F59E0B"
                )
            )
        )
    )
}
