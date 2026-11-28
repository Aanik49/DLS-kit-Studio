package com.example.data.remote

import com.example.data.model.DlsPlayerCardSpec
import com.example.data.model.PlayerCardTier
import com.example.data.model.PlayerCountry
import com.example.data.model.PlayerPosition
import com.example.data.model.PreferredFoot

/**
 * Predefined 100-player DLS 26 database across Forwards (30), Midfielders (30),
 * Defenders (28), and Goalkeepers (12). Every player can be searched, viewed,
 * fully edited, customized with a photo, and exported as a PNG card.
 */
object DlsPlayerDatabase {

    private fun p(
        id: String,
        fullName: String,
        shortName: String,
        ovr: Int,
        pos: PlayerPosition,
        country: PlayerCountry,
        height: Int,
        foot: PreferredFoot,
        spe: Int,
        acc: Int,
        sta: Int,
        str: Int,
        con: Int,
        pas: Int,
        sho: Int,
        tac: Int,
        clubHex: String,
        tier: PlayerCardTier = when {
            ovr >= 90 -> PlayerCardTier.MAXED_BLACK
            ovr >= 80 -> PlayerCardTier.LEGENDARY_GOLD
            ovr >= 70 -> PlayerCardTier.RARE_BLUE
            else -> PlayerCardTier.COMMON_SILVER
        },
        boosted: Boolean = ovr >= 90,
        gkh: Int = 52,
        gkr: Int = 55
    ): DlsPlayerCardSpec = DlsPlayerCardSpec(
        id = id,
        fullName = fullName.uppercase(),
        cardShortName = shortName.uppercase(),
        rating = ovr,
        ratingBoosted = boosted,
        position = pos,
        country = country,
        tier = tier,
        heightCm = height,
        preferredFoot = foot,
        spe = spe,
        acc = acc,
        sta = sta,
        str = str,
        con = con,
        pas = pas,
        sho = sho,
        tac = tac,
        gkh = gkh,
        gkr = gkr,
        clubAccentHex = clubHex
    )

    val players: List<DlsPlayerCardSpec> = listOf(
        // ==================== FORWARDS (30) ====================
        p("p001", "Kylian Mbappé", "Mbappé", 94, PlayerPosition.CF, PlayerCountry.FRANCE, 178, PreferredFoot.RIGHT, 95, 96, 82, 78, 100, 100, 100, 59, "#FFFFFF", tier = PlayerCardTier.LEGENDARY_GOLD, boosted = true),
        p("p002", "Erling Haaland", "Haaland", 86, PlayerPosition.CF, PlayerCountry.NORWAY, 195, PreferredFoot.LEFT, 90, 86, 84, 92, 83, 73, 93, 48, "#6CABDD"),
        p("p003", "Vinícius Júnior", "Vinícius Jr", 86, PlayerPosition.LW, PlayerCountry.BRAZIL, 176, PreferredFoot.RIGHT, 94, 95, 85, 71, 91, 82, 85, 42, "#FFFFFF"),
        p("p004", "Mohamed Salah", "Salah", 85, PlayerPosition.RW, PlayerCountry.EGYPT, 175, PreferredFoot.LEFT, 89, 91, 88, 76, 89, 84, 89, 51, "#C8102E"),
        p("p005", "Harry Kane", "Kane", 86, PlayerPosition.CF, PlayerCountry.ENGLAND, 188, PreferredFoot.RIGHT, 74, 72, 84, 86, 86, 85, 94, 54, "#DC052D"),
        p("p006", "Lionel Messi", "Messi", 93, PlayerPosition.RW, PlayerCountry.ARGENTINA, 170, PreferredFoot.LEFT, 86, 91, 79, 72, 99, 97, 96, 44, "#F7B5CD", tier = PlayerCardTier.SPECIAL_ICON, boosted = true),
        p("p007", "Cristiano Ronaldo", "Ronaldo", 92, PlayerPosition.CF, PlayerCountry.PORTUGAL, 187, PreferredFoot.BOTH, 88, 86, 82, 88, 92, 85, 97, 45, "#FFD700", tier = PlayerCardTier.SPECIAL_ICON, boosted = true),
        p("p008", "Bukayo Saka", "Saka", 84, PlayerPosition.RW, PlayerCountry.ENGLAND, 178, PreferredFoot.LEFT, 87, 89, 86, 75, 88, 84, 84, 61, "#EF0107"),
        p("p009", "Lamine Yamal", "Yamal", 83, PlayerPosition.RW, PlayerCountry.SPAIN, 180, PreferredFoot.LEFT, 88, 91, 80, 65, 90, 84, 80, 45, "#004D98"),
        p("p010", "Lautaro Martínez", "L. Martínez", 85, PlayerPosition.CF, PlayerCountry.ARGENTINA, 174, PreferredFoot.RIGHT, 84, 86, 86, 84, 87, 77, 89, 55, "#010E80"),
        p("p011", "Robert Lewandowski", "Lewandowski", 84, PlayerPosition.CF, PlayerCountry.POLAND, 185, PreferredFoot.RIGHT, 78, 77, 80, 87, 86, 79, 92, 48, "#A50044"),
        p("p012", "Rodrygo Goes", "Rodrygo", 83, PlayerPosition.RW, PlayerCountry.BRAZIL, 174, PreferredFoot.RIGHT, 89, 91, 82, 68, 88, 81, 83, 47, "#FFFFFF"),
        p("p013", "Son Heung-min", "Son", 84, PlayerPosition.LW, PlayerCountry.SOUTH_KOREA, 183, PreferredFoot.BOTH, 89, 88, 85, 74, 86, 81, 89, 49, "#132257"),
        p("p014", "Antoine Griezmann", "Griezmann", 84, PlayerPosition.SS, PlayerCountry.FRANCE, 176, PreferredFoot.LEFT, 81, 83, 88, 74, 88, 86, 86, 63, "#CB3524"),
        p("p015", "Raphinha Belloli", "Raphinha", 84, PlayerPosition.LW, PlayerCountry.BRAZIL, 176, PreferredFoot.LEFT, 90, 91, 87, 72, 87, 83, 85, 58, "#004D98"),
        p("p016", "Victor Osimhen", "Osimhen", 83, PlayerPosition.CF, PlayerCountry.NIGERIA, 185, PreferredFoot.RIGHT, 91, 90, 84, 86, 82, 71, 87, 46, "#FDB912"),
        p("p017", "Rafael Leão", "Leão", 83, PlayerPosition.LW, PlayerCountry.PORTUGAL, 188, PreferredFoot.RIGHT, 94, 93, 81, 82, 88, 78, 82, 40, "#FB090B"),
        p("p018", "Julián Álvarez", "Álvarez", 83, PlayerPosition.CF, PlayerCountry.ARGENTINA, 170, PreferredFoot.RIGHT, 86, 88, 89, 75, 86, 81, 86, 59, "#CB3524"),
        p("p019", "Alexander Isak", "Isak", 84, PlayerPosition.CF, PlayerCountry.SWEDEN, 192, PreferredFoot.RIGHT, 89, 88, 82, 81, 86, 77, 88, 43, "#111827"),
        p("p020", "Viktor Gyökeres", "Gyökeres", 83, PlayerPosition.CF, PlayerCountry.SWEDEN, 189, PreferredFoot.RIGHT, 89, 87, 88, 91, 83, 76, 88, 52, "#008057"),
        p("p021", "Ousmane Dembélé", "Dembélé", 84, PlayerPosition.RW, PlayerCountry.FRANCE, 178, PreferredFoot.BOTH, 93, 95, 81, 67, 90, 83, 82, 44, "#004170"),
        p("p022", "Khvicha Kvaratskhelia", "Kvaratskhelia", 83, PlayerPosition.LW, PlayerCountry.GEORGIA, 183, PreferredFoot.BOTH, 87, 89, 83, 76, 90, 81, 82, 48, "#004170"),
        p("p023", "Neymar Júnior", "Neymar Jr", 84, PlayerPosition.LW, PlayerCountry.BRAZIL, 175, PreferredFoot.BOTH, 84, 88, 74, 66, 94, 87, 85, 39, "#1E40AF"),
        p("p024", "Marcus Thuram", "Thuram", 82, PlayerPosition.CF, PlayerCountry.FRANCE, 192, PreferredFoot.RIGHT, 89, 86, 83, 88, 83, 75, 84, 51, "#010E80"),
        p("p025", "Luis Díaz", "L. Díaz", 83, PlayerPosition.LW, PlayerCountry.COLOMBIA, 178, PreferredFoot.RIGHT, 91, 92, 86, 73, 87, 78, 82, 52, "#C8102E"),
        p("p026", "Ollie Watkins", "Watkins", 82, PlayerPosition.CF, PlayerCountry.ENGLAND, 180, PreferredFoot.RIGHT, 87, 86, 86, 81, 82, 75, 85, 50, "#670E36"),
        p("p027", "Darwin Núñez", "Núñez", 81, PlayerPosition.CF, PlayerCountry.URUGUAY, 187, PreferredFoot.RIGHT, 91, 89, 85, 86, 80, 72, 83, 47, "#C8102E"),
        p("p028", "Paulo Dybala", "Dybala", 83, PlayerPosition.SS, PlayerCountry.ARGENTINA, 177, PreferredFoot.LEFT, 81, 85, 76, 66, 91, 86, 87, 43, "#8E1F2F"),
        p("p029", "Nico Williams", "N. Williams", 82, PlayerPosition.LW, PlayerCountry.SPAIN, 181, PreferredFoot.BOTH, 93, 94, 83, 69, 86, 79, 79, 46, "#EE2523"),
        p("p030", "Christian Pulisic", "Pulisic", 81, PlayerPosition.RW, PlayerCountry.USA, 177, PreferredFoot.RIGHT, 88, 90, 82, 68, 86, 80, 81, 49, "#FB090B"),

        // ==================== MIDFIELDERS (30) ====================
        p("p031", "Jude Bellingham", "Bellingham", 86, PlayerPosition.AM, PlayerCountry.ENGLAND, 186, PreferredFoot.RIGHT, 84, 84, 92, 85, 90, 86, 86, 78, "#FFFFFF"),
        p("p032", "Rodri Hernández", "Rodri", 86, PlayerPosition.DM, PlayerCountry.SPAIN, 191, PreferredFoot.RIGHT, 74, 73, 91, 89, 88, 91, 82, 87, "#6CABDD"),
        p("p033", "Kevin De Bruyne", "De Bruyne", 85, PlayerPosition.AM, PlayerCountry.BELGIUM, 181, PreferredFoot.BOTH, 76, 77, 83, 79, 89, 95, 88, 65, "#6CABDD"),
        p("p034", "Federico Valverde", "Valverde", 85, PlayerPosition.CM, PlayerCountry.URUGUAY, 182, PreferredFoot.RIGHT, 90, 88, 93, 83, 85, 86, 85, 81, "#FFFFFF"),
        p("p035", "Martin Ødegaard", "Ødegaard", 85, PlayerPosition.AM, PlayerCountry.NORWAY, 178, PreferredFoot.LEFT, 80, 82, 89, 70, 91, 91, 83, 66, "#EF0107"),
        p("p036", "Florian Wirtz", "Wirtz", 84, PlayerPosition.AM, PlayerCountry.GERMANY, 177, PreferredFoot.RIGHT, 84, 87, 85, 70, 91, 88, 83, 58, "#E32221"),
        p("p037", "Jamal Musiala", "Musiala", 84, PlayerPosition.AM, PlayerCountry.GERMANY, 184, PreferredFoot.RIGHT, 85, 89, 83, 71, 93, 84, 83, 61, "#DC052D"),
        p("p038", "Pedri González", "Pedri", 84, PlayerPosition.CM, PlayerCountry.SPAIN, 174, PreferredFoot.RIGHT, 81, 84, 88, 70, 92, 90, 76, 72, "#004D98"),
        p("p039", "Declan Rice", "Rice", 84, PlayerPosition.DM, PlayerCountry.ENGLAND, 188, PreferredFoot.RIGHT, 81, 80, 92, 87, 84, 85, 76, 86, "#EF0107"),
        p("p040", "Cole Palmer", "Palmer", 84, PlayerPosition.AM, PlayerCountry.ENGLAND, 189, PreferredFoot.LEFT, 83, 84, 84, 73, 89, 87, 87, 52, "#034694"),
        p("p041", "Bruno Fernandes", "B. Fernandes", 84, PlayerPosition.AM, PlayerCountry.PORTUGAL, 179, PreferredFoot.RIGHT, 78, 80, 90, 73, 87, 91, 86, 68, "#DA291C"),
        p("p042", "Phil Foden", "Foden", 85, PlayerPosition.AM, PlayerCountry.ENGLAND, 171, PreferredFoot.LEFT, 85, 88, 84, 67, 92, 87, 86, 56, "#6CABDD"),
        p("p043", "Bernardo Silva", "B. Silva", 84, PlayerPosition.CM, PlayerCountry.PORTUGAL, 173, PreferredFoot.LEFT, 80, 84, 92, 69, 92, 88, 79, 71, "#6CABDD"),
        p("p044", "Luka Modrić", "Modrić", 83, PlayerPosition.CM, PlayerCountry.CROATIA, 172, PreferredFoot.BOTH, 75, 78, 81, 68, 92, 93, 79, 72, "#FFFFFF"),
        p("p045", "Alexis Mac Allister", "Mac Allister", 83, PlayerPosition.CM, PlayerCountry.ARGENTINA, 176, PreferredFoot.RIGHT, 78, 79, 88, 78, 87, 88, 81, 77, "#C8102E"),
        p("p046", "Nicolò Barella", "Barella", 84, PlayerPosition.CM, PlayerCountry.ITALY, 175, PreferredFoot.RIGHT, 83, 85, 92, 76, 88, 87, 79, 80, "#010E80"),
        p("p047", "Aurélien Tchouaméni", "Tchouaméni", 82, PlayerPosition.DM, PlayerCountry.FRANCE, 188, PreferredFoot.RIGHT, 81, 80, 87, 88, 82, 83, 75, 84, "#FFFFFF"),
        p("p048", "Eduardo Camavinga", "Camavinga", 82, PlayerPosition.CM, PlayerCountry.FRANCE, 182, PreferredFoot.LEFT, 85, 86, 87, 80, 86, 84, 71, 82, "#FFFFFF"),
        p("p049", "Frenkie de Jong", "De Jong", 83, PlayerPosition.CM, PlayerCountry.NETHERLANDS, 181, PreferredFoot.RIGHT, 84, 85, 87, 78, 90, 88, 72, 78, "#004D98"),
        p("p050", "Joshua Kimmich", "Kimmich", 84, PlayerPosition.DM, PlayerCountry.GERMANY, 177, PreferredFoot.RIGHT, 76, 78, 91, 76, 86, 91, 77, 83, "#DC052D"),
        p("p051", "Pablo Gavi", "Gavi", 81, PlayerPosition.CM, PlayerCountry.SPAIN, 173, PreferredFoot.RIGHT, 82, 84, 89, 75, 87, 84, 72, 79, "#004D98"),
        p("p052", "Dani Olmo", "Olmo", 82, PlayerPosition.AM, PlayerCountry.SPAIN, 179, PreferredFoot.RIGHT, 82, 84, 82, 72, 89, 86, 82, 59, "#004D98"),
        p("p053", "Vítor Vitinha", "Vitinha", 83, PlayerPosition.CM, PlayerCountry.PORTUGAL, 172, PreferredFoot.RIGHT, 81, 84, 88, 70, 90, 89, 78, 75, "#004170"),
        p("p054", "Bruno Guimarães", "Guimarães", 82, PlayerPosition.CM, PlayerCountry.BRAZIL, 182, PreferredFoot.RIGHT, 77, 78, 89, 82, 85, 86, 76, 81, "#111827"),
        p("p055", "Hakan Çalhanoğlu", "Çalhanoğlu", 83, PlayerPosition.DM, PlayerCountry.TURKEY, 178, PreferredFoot.RIGHT, 75, 76, 86, 75, 87, 91, 84, 77, "#010E80"),
        p("p056", "Tijjani Reijnders", "Reijnders", 82, PlayerPosition.CM, PlayerCountry.NETHERLANDS, 185, PreferredFoot.RIGHT, 84, 83, 88, 78, 86, 85, 80, 74, "#FB090B"),
        p("p057", "Granit Xhaka", "Xhaka", 82, PlayerPosition.DM, PlayerCountry.SWITZERLAND, 186, PreferredFoot.LEFT, 71, 70, 88, 84, 83, 89, 79, 81, "#E32221"),
        p("p058", "Enzo Fernández", "Enzo", 81, PlayerPosition.CM, PlayerCountry.ARGENTINA, 178, PreferredFoot.RIGHT, 78, 79, 87, 77, 85, 87, 76, 76, "#034694"),
        p("p059", "Moisés Caicedo", "Caicedo", 82, PlayerPosition.DM, PlayerCountry.ECUADOR, 178, PreferredFoot.RIGHT, 83, 83, 90, 83, 83, 82, 70, 86, "#034694"),
        p("p060", "Takefusa Kubo", "Kubo", 80, PlayerPosition.RM, PlayerCountry.JAPAN, 173, PreferredFoot.LEFT, 87, 91, 81, 64, 88, 81, 78, 49, "#0067B1"),

        // ==================== DEFENDERS (28) ====================
        p("p061", "Virgil van Dijk", "Van Dijk", 86, PlayerPosition.CB, PlayerCountry.NETHERLANDS, 195, PreferredFoot.RIGHT, 83, 80, 84, 94, 80, 81, 66, 92, "#C8102E"),
        p("p062", "William Saliba", "Saliba", 85, PlayerPosition.CB, PlayerCountry.FRANCE, 192, PreferredFoot.RIGHT, 85, 83, 85, 90, 81, 80, 56, 90, "#EF0107"),
        p("p063", "Rúben Dias", "Rúben Dias", 85, PlayerPosition.CB, PlayerCountry.PORTUGAL, 187, PreferredFoot.RIGHT, 77, 76, 86, 91, 79, 79, 55, 91, "#6CABDD"),
        p("p064", "Antonio Rüdiger", "Rüdiger", 84, PlayerPosition.CB, PlayerCountry.GERMANY, 190, PreferredFoot.RIGHT, 86, 83, 85, 91, 76, 76, 61, 89, "#FFFFFF"),
        p("p065", "Achraf Hakimi", "Hakimi", 84, PlayerPosition.RB, PlayerCountry.MOROCCO, 181, PreferredFoot.RIGHT, 94, 93, 90, 78, 84, 82, 76, 79, "#004170"),
        p("p066", "Trent Alexander-Arnold", "Alexander-Arnold", 84, PlayerPosition.RB, PlayerCountry.ENGLAND, 180, PreferredFoot.RIGHT, 81, 81, 86, 74, 85, 93, 78, 78, "#C8102E"),
        p("p067", "Theo Hernández", "T. Hernández", 84, PlayerPosition.LB, PlayerCountry.FRANCE, 184, PreferredFoot.LEFT, 94, 93, 89, 86, 83, 80, 77, 81, "#FB090B"),
        p("p068", "Joško Gvardiol", "Gvardiol", 83, PlayerPosition.LB, PlayerCountry.CROATIA, 185, PreferredFoot.LEFT, 84, 82, 86, 88, 82, 80, 71, 86, "#6CABDD"),
        p("p069", "Gabriel Magalhães", "Gabriel", 84, PlayerPosition.CB, PlayerCountry.BRAZIL, 190, PreferredFoot.LEFT, 79, 77, 84, 91, 75, 75, 60, 89, "#EF0107"),
        p("p070", "Alessandro Bastoni", "Bastoni", 84, PlayerPosition.CB, PlayerCountry.ITALY, 190, PreferredFoot.LEFT, 80, 79, 85, 86, 82, 83, 58, 89, "#010E80"),
        p("p071", "Ronald Araújo", "Araújo", 83, PlayerPosition.CB, PlayerCountry.URUGUAY, 192, PreferredFoot.RIGHT, 87, 84, 83, 91, 75, 74, 58, 88, "#004D98"),
        p("p072", "Marquinhos Corrêa", "Marquinhos", 83, PlayerPosition.CB, PlayerCountry.BRAZIL, 183, PreferredFoot.RIGHT, 82, 81, 85, 84, 81, 81, 59, 88, "#004170"),
        p("p073", "Dani Carvajal", "Carvajal", 83, PlayerPosition.RB, PlayerCountry.SPAIN, 173, PreferredFoot.RIGHT, 83, 84, 88, 81, 82, 82, 68, 84, "#FFFFFF"),
        p("p074", "Kyle Walker", "Walker", 82, PlayerPosition.RB, PlayerCountry.ENGLAND, 183, PreferredFoot.RIGHT, 92, 90, 86, 85, 79, 77, 67, 82, "#6CABDD"),
        p("p075", "Alphonso Davies", "Davies", 82, PlayerPosition.LB, PlayerCountry.CANADA, 183, PreferredFoot.LEFT, 95, 95, 86, 79, 84, 78, 70, 78, "#DC052D"),
        p("p076", "Andrew Robertson", "Robertson", 82, PlayerPosition.LB, PlayerCountry.SCOTLAND, 178, PreferredFoot.LEFT, 84, 84, 90, 76, 82, 84, 64, 82, "#C8102E"),
        p("p077", "Federico Dimarco", "Dimarco", 83, PlayerPosition.LB, PlayerCountry.ITALY, 175, PreferredFoot.LEFT, 84, 85, 86, 75, 84, 87, 79, 79, "#010E80"),
        p("p078", "Jeremie Frimpong", "Frimpong", 82, PlayerPosition.RB, PlayerCountry.NETHERLANDS, 172, PreferredFoot.RIGHT, 95, 96, 87, 70, 85, 78, 72, 74, "#E32221"),
        p("p079", "Jules Koundé", "Koundé", 83, PlayerPosition.RB, PlayerCountry.FRANCE, 180, PreferredFoot.RIGHT, 86, 85, 87, 84, 81, 80, 62, 86, "#004D98"),
        p("p080", "Éder Militão", "Militão", 83, PlayerPosition.CB, PlayerCountry.BRAZIL, 186, PreferredFoot.RIGHT, 86, 85, 84, 87, 77, 76, 58, 87, "#FFFFFF"),
        p("p081", "Micky van de Ven", "Van de Ven", 81, PlayerPosition.CB, PlayerCountry.NETHERLANDS, 193, PreferredFoot.LEFT, 93, 90, 83, 86, 76, 75, 55, 84, "#132257"),
        p("p082", "Cristian Romero", "Romero", 83, PlayerPosition.CB, PlayerCountry.ARGENTINA, 185, PreferredFoot.RIGHT, 81, 80, 84, 88, 77, 76, 57, 89, "#132257"),
        p("p083", "Ibrahima Konaté", "Konaté", 82, PlayerPosition.CB, PlayerCountry.FRANCE, 194, PreferredFoot.RIGHT, 84, 81, 83, 91, 76, 75, 55, 86, "#C8102E"),
        p("p084", "John Stones", "Stones", 82, PlayerPosition.CB, PlayerCountry.ENGLAND, 188, PreferredFoot.RIGHT, 78, 77, 83, 84, 83, 83, 62, 86, "#6CABDD"),
        p("p085", "Pau Cubarsí", "Cubarsí", 79, PlayerPosition.CB, PlayerCountry.SPAIN, 184, PreferredFoot.RIGHT, 78, 77, 81, 80, 81, 84, 52, 84, "#004D98"),
        p("p086", "Nuno Mendes", "N. Mendes", 82, PlayerPosition.LB, PlayerCountry.PORTUGAL, 180, PreferredFoot.LEFT, 93, 93, 87, 79, 83, 80, 69, 81, "#004170"),
        p("p087", "Alejandro Grimaldo", "Grimaldo", 83, PlayerPosition.LB, PlayerCountry.SPAIN, 171, PreferredFoot.LEFT, 85, 86, 88, 71, 85, 88, 80, 78, "#E32221"),
        p("p088", "Gleison Bremer", "Bremer", 83, PlayerPosition.CB, PlayerCountry.BRAZIL, 188, PreferredFoot.RIGHT, 84, 82, 84, 90, 74, 72, 56, 89, "#111827"),

        // ==================== GOALKEEPERS (12) ====================
        p("p089", "Thibaut Courtois", "Courtois", 86, PlayerPosition.GK, PlayerCountry.BELGIUM, 200, PreferredFoot.LEFT, 60, 62, 74, 84, 65, 76, 52, 50, "#10B981", gkh = 87, gkr = 89),
        p("p090", "Alisson Becker", "Alisson", 86, PlayerPosition.GK, PlayerCountry.BRAZIL, 193, PreferredFoot.RIGHT, 64, 66, 75, 83, 70, 82, 54, 52, "#F59E0B", gkh = 86, gkr = 88),
        p("p091", "Gianluigi Donnarumma", "Donnarumma", 85, PlayerPosition.GK, PlayerCountry.ITALY, 196, PreferredFoot.RIGHT, 62, 65, 73, 85, 64, 74, 50, 48, "#22C55E", gkh = 85, gkr = 89),
        p("p092", "Emiliano Martínez", "E. Martínez", 85, PlayerPosition.GK, PlayerCountry.ARGENTINA, 195, PreferredFoot.RIGHT, 61, 64, 75, 85, 66, 79, 52, 50, "#38BDF8", gkh = 85, gkr = 88),
        p("p093", "Marc-André ter Stegen", "Ter Stegen", 84, PlayerPosition.GK, PlayerCountry.GERMANY, 187, PreferredFoot.RIGHT, 61, 63, 74, 80, 71, 84, 53, 50, "#A855F7", gkh = 84, gkr = 87),
        p("p094", "Ederson Moraes", "Ederson", 84, PlayerPosition.GK, PlayerCountry.BRAZIL, 188, PreferredFoot.LEFT, 66, 68, 76, 82, 75, 90, 58, 52, "#EC4899", gkh = 82, gkr = 86),
        p("p095", "Mike Maignan", "Maignan", 84, PlayerPosition.GK, PlayerCountry.FRANCE, 191, PreferredFoot.RIGHT, 65, 68, 75, 84, 69, 81, 52, 50, "#F97316", gkh = 84, gkr = 87),
        p("p096", "Jan Oblak", "Oblak", 84, PlayerPosition.GK, PlayerCountry.SLOVENIA, 188, PreferredFoot.RIGHT, 58, 60, 73, 83, 63, 74, 49, 48, "#EAB308", gkh = 86, gkr = 86),
        p("p097", "Gregor Kobel", "Kobel", 83, PlayerPosition.GK, PlayerCountry.SWITZERLAND, 195, PreferredFoot.RIGHT, 60, 63, 73, 84, 63, 73, 49, 47, "#FACC15", gkh = 83, gkr = 86),
        p("p098", "David Raya", "Raya", 83, PlayerPosition.GK, PlayerCountry.SPAIN, 183, PreferredFoot.RIGHT, 64, 67, 75, 78, 71, 84, 52, 50, "#14B8A6", gkh = 82, gkr = 86),
        p("p099", "Manuel Neuer", "Neuer", 83, PlayerPosition.GK, PlayerCountry.GERMANY, 193, PreferredFoot.RIGHT, 62, 63, 72, 84, 72, 86, 55, 52, "#EF4444", gkh = 84, gkr = 85),
        p("p100", "Diogo Costa", "Diogo Costa", 82, PlayerPosition.GK, PlayerCountry.PORTUGAL, 186, PreferredFoot.RIGHT, 63, 65, 74, 80, 69, 82, 51, 49, "#2563EB", gkh = 82, gkr = 85)
    )
}
