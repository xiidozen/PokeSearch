package com.pokesearch.model

/**
 * Catalogue of every filter supported by Pokémon GO's search syntax.
 * Adding new filters here automatically makes them available throughout the app.
 */
object FilterDefs {

    // ── Shared option lists ───────────────────────────────────────────────────

    private val TYPE_OPTIONS = listOf(
        EnumOption("normal",   "Normal"),       EnumOption("fire",     "Fire 🔥"),
        EnumOption("water",    "Water 💧"),     EnumOption("electric", "Electric ⚡"),
        EnumOption("grass",    "Grass 🌿"),     EnumOption("ice",      "Ice ❄️"),
        EnumOption("fighting", "Fighting 🥊"),  EnumOption("poison",   "Poison ☠️"),
        EnumOption("ground",   "Ground"),       EnumOption("flying",   "Flying 💨"),
        EnumOption("psychic",  "Psychic 🔮"),   EnumOption("bug",      "Bug 🐛"),
        EnumOption("rock",     "Rock 🪨"),      EnumOption("ghost",    "Ghost 👻"),
        EnumOption("dragon",   "Dragon 🐉"),    EnumOption("dark",     "Dark 🌑"),
        EnumOption("steel",    "Steel ⚙️"),     EnumOption("fairy",    "Fairy ✨")
    )

    // ── Identity ─────────────────────────────────────────────────────────────

    val NAME = FilterDef(
        id = "name", displayName = "Pokémon Name",
        category = FilterCategory.IDENTITY, valueType = ValueType.TEXT,
        description = "Partial name match (e.g. \"char\" matches Charmander, Charmeleon, Charizard)",
        searchToken = ""
    )

    val POKEDEX = FilterDef(
        id = "pokedex", displayName = "Pokédex Number",
        category = FilterCategory.IDENTITY, valueType = ValueType.POKEDEX_RANGE,
        description = "Single number (#151) or range (#1-151)",
        searchToken = "#"
    )

    val TYPE = FilterDef(
        id = "type", displayName = "Type",
        category = FilterCategory.IDENTITY, valueType = ValueType.ENUM_SINGLE,
        description = "Filter by primary or secondary type",
        enumOptions = TYPE_OPTIONS
    )

    val GENERATION = FilterDef(
        id = "generation", displayName = "Generation",
        category = FilterCategory.IDENTITY, valueType = ValueType.ENUM_SINGLE,
        description = "Filter by Pokémon generation (also accepts: kanto, johto, hoenn, sinnoh, unova, kalos, alola, galar, paldea)",
        enumOptions = listOf(
            EnumOption("gen1", "Gen 1 – Kanto"),
            EnumOption("gen2", "Gen 2 – Johto"),
            EnumOption("gen3", "Gen 3 – Hoenn"),
            EnumOption("gen4", "Gen 4 – Sinnoh"),
            EnumOption("gen5", "Gen 5 – Unova"),
            EnumOption("gen6", "Gen 6 – Kalos"),
            EnumOption("gen7", "Gen 7 – Alola"),
            EnumOption("gen8", "Gen 8 – Galar"),
            EnumOption("gen9", "Gen 9 – Paldea")
        )
    )

    val BABY = FilterDef(
        id = "baby", displayName = "Baby Pokémon",
        category = FilterCategory.IDENTITY, valueType = ValueType.BOOLEAN,
        description = "Is a Baby Pokémon (e.g. Pichu, Togepi)"
    )

    // ── Pokémon Size ──────────────────────────────────────────────────────────

    val POKEMON_SIZE = FilterDef(
        id = "pokemon_size", displayName = "Size Category",
        category = FilterCategory.SIZE, valueType = ValueType.ENUM_SINGLE,
        description = "Physical size category of the Pokémon",
        enumOptions = listOf(
            EnumOption("xxs", "XXS – Tiny"),
            EnumOption("xs",  "XS – Small"),
            EnumOption("xl",  "XL – Large"),
            EnumOption("xxl", "XXL – Extra Large")
        )
    )

    // ── Combat Stats ──────────────────────────────────────────────────────────

    val CP = FilterDef(
        id = "cp", displayName = "CP",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Combat Power. Use min/max for a range, leave one blank for open-ended.",
        searchToken = "cp"
    )

    val HP = FilterDef(
        id = "hp", displayName = "HP",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Hit Points range",
        searchToken = "hp"
    )

    val IV_STARS = FilterDef(
        id = "stars", displayName = "IV Stars",
        category = FilterCategory.COMBAT, valueType = ValueType.ENUM_SINGLE,
        description = "Overall IV rating expressed as stars",
        enumOptions = listOf(
            EnumOption("0*", "0 ★ – 0%"),
            EnumOption("1*", "1 ★ – 51-63%"),
            EnumOption("2*", "2 ★ – 66-80%"),
            EnumOption("3*", "3 ★ – 82-97%"),
            EnumOption("4*", "4 ★ – Perfect (100%)")
        )
    )

    val ATK = FilterDef(
        id = "atk", displayName = "Attack IV",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Attack IV value (0–15)",
        searchToken = "atk"
    )

    val DEF = FilterDef(
        id = "def", displayName = "Defense IV",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Defense IV value (0–15)",
        searchToken = "def"
    )

    val STA = FilterDef(
        id = "sta", displayName = "Stamina IV",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Stamina (HP) IV value (0–15)",
        searchToken = "sta"
    )

    val LEVEL = FilterDef(
        id = "level", displayName = "Level",
        category = FilterCategory.COMBAT, valueType = ValueType.NUMERIC_RANGE,
        description = "Pokémon level (1–50)",
        searchToken = "level"
    )

    val WEAK_TO = FilterDef(
        id = "weak_to", displayName = "Weak To Type",
        category = FilterCategory.COMBAT, valueType = ValueType.TEXT,
        description = "Pokémon is weak to this type (e.g. fire). Serializes as <fire.",
        searchToken = "<"
    )

    val STRONG_AGAINST = FilterDef(
        id = "strong_against", displayName = "Strong Against Type",
        category = FilterCategory.COMBAT, valueType = ValueType.TEXT,
        description = "Pokémon resists / is strong against this type (e.g. water). Serializes as >water.",
        searchToken = ">"
    )

    // ── Moves ─────────────────────────────────────────────────────────────────

    val MOVE = FilterDef(
        id = "move", displayName = "Any Move",
        category = FilterCategory.MOVES, valueType = ValueType.TEXT,
        description = "Has this move in any slot. E.g. \"ember\" matches @ember.",
        searchToken = "@"
    )

    val QUICK_MOVE_TYPE = FilterDef(
        id = "quick_move_type", displayName = "Quick Move Type",
        category = FilterCategory.MOVES, valueType = ValueType.TEXT,
        description = "Has a Quick (fast) move of this type. Serializes as @1fire.",
        searchToken = "@1"
    )

    val CHARGE_MOVE_TYPE = FilterDef(
        id = "charge_move_type", displayName = "Charge Move Type",
        category = FilterCategory.MOVES, valueType = ValueType.TEXT,
        description = "Has a Charge move of this type. Serializes as @2fire.",
        searchToken = "@2"
    )

    val LEGACY_MOVE = FilterDef(
        id = "legacy_move", displayName = "Has Legacy Move",
        category = FilterCategory.MOVES, valueType = ValueType.BOOLEAN,
        description = "Has at least one legacy (no-longer-obtainable) move",
        searchToken = "@legacy"
    )

    val ELITE_MOVE = FilterDef(
        id = "elite_move", displayName = "Has Elite TM Move",
        category = FilterCategory.MOVES, valueType = ValueType.BOOLEAN,
        description = "Has a move obtained via Elite TM",
        searchToken = "@elite"
    )

    val SPECIAL_MOVE = FilterDef(
        id = "special_move", displayName = "Has Special Move",
        category = FilterCategory.MOVES, valueType = ValueType.BOOLEAN,
        description = "Has a special or event-exclusive move",
        searchToken = "@special"
    )

    val PURIFIED_MOVE = FilterDef(
        id = "purified_move", displayName = "Has Return (Purified Move)",
        category = FilterCategory.MOVES, valueType = ValueType.BOOLEAN,
        description = "Has the Return charged move (exclusive to purified Pokémon)",
        searchToken = "@purified"
    )

    // ── Status & Special ──────────────────────────────────────────────────────

    val SHINY = FilterDef(
        id = "shiny", displayName = "Shiny",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a shiny Pokémon"
    )
    val LUCKY = FilterDef(
        id = "lucky", displayName = "Lucky",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a Lucky Pokémon (obtained via trade)"
    )
    val SHADOW = FilterDef(
        id = "shadow", displayName = "Shadow",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a Shadow Pokémon"
    )
    val PURIFIED = FilterDef(
        id = "purified", displayName = "Purified",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a Purified Pokémon"
    )
    val LEGENDARY = FilterDef(
        id = "legendary", displayName = "Legendary",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a Legendary Pokémon"
    )
    val MYTHICAL = FilterDef(
        id = "mythical", displayName = "Mythical",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a Mythical Pokémon"
    )
    val ULTRA_BEAST = FilterDef(
        id = "ultrabeast", displayName = "Ultra Beast",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is an Ultra Beast"
    )
    val MEGA = FilterDef(
        id = "mega", displayName = "Mega / Primal (active)",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is currently Mega-evolved or Primal"
    )
    val DEFENDER = FilterDef(
        id = "defender", displayName = "Defending Gym",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is currently defending a Gym"
    )
    val TRADED = FilterDef(
        id = "traded", displayName = "Was Traded",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Was obtained via a trade"
    )
    val COSTUME = FilterDef(
        id = "costume", displayName = "Costume",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is wearing a costume"
    )
    val NEW = FilterDef(
        id = "new", displayName = "New Form",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is an unseen/new form for your account"
    )
    val WEATHER = FilterDef(
        id = "weather", displayName = "Weather Boosted",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is currently weather-boosted in the wild"
    )
    val CANDY_XL = FilterDef(
        id = "candyxl", displayName = "XL Candy Eligible",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Can receive XL Candy (level 31+)",
        searchToken = "candyxl"
    )
    val REGIONAL = FilterDef(
        id = "regional", displayName = "Regional Exclusive",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Is a region-exclusive Pokémon"
    )
    val BACKGROUND = FilterDef(
        id = "background", displayName = "Background Forme",
        category = FilterCategory.STATUS, valueType = ValueType.BOOLEAN,
        description = "Has a background forme"
    )

    // ── Evolution ─────────────────────────────────────────────────────────────

    val EVOLVE = FilterDef(
        id = "evolve", displayName = "Can Evolve",
        category = FilterCategory.EVOLUTION, valueType = ValueType.BOOLEAN,
        description = "Has candy and items needed to evolve"
    )
    val EVOLVE_NEW = FilterDef(
        id = "evolvenew", displayName = "Can Evolve (New Form)",
        category = FilterCategory.EVOLUTION, valueType = ValueType.BOOLEAN,
        description = "Can evolve into a new form not yet in your Pokédex",
        searchToken = "evolvenew"
    )
    val TRADE_EVOLVE = FilterDef(
        id = "tradeevolve", displayName = "Trade Evolution",
        category = FilterCategory.EVOLUTION, valueType = ValueType.BOOLEAN,
        description = "Can benefit from trade evolution (free candy cost after trading)"
    )
    val EVOLVE_ITEM = FilterDef(
        id = "item", displayName = "Needs Evolution Item",
        category = FilterCategory.EVOLUTION, valueType = ValueType.BOOLEAN,
        description = "Requires a special item to evolve (e.g. King's Rock)"
    )
    val MEGA_EVOLVE = FilterDef(
        id = "megaevolve", displayName = "Can Mega Evolve",
        category = FilterCategory.EVOLUTION, valueType = ValueType.BOOLEAN,
        description = "Has enough Mega Energy to Mega Evolve right now",
        searchToken = "megaevolve"
    )
    val MEGA_LEVEL = FilterDef(
        id = "mega_level", displayName = "Mega Level",
        category = FilterCategory.EVOLUTION, valueType = ValueType.ENUM_SINGLE,
        description = "Current Mega Evolution level (tracks how many times mega-evolved)",
        enumOptions = listOf(
            EnumOption("mega0", "Mega 0 – Never Mega Evolved"),
            EnumOption("mega1", "Mega 1 – Base Level"),
            EnumOption("mega2", "Mega 2 – High Level"),
            EnumOption("mega3", "Mega 3 – Mega Master")
        )
    )

    // ── Egg & Hatch ───────────────────────────────────────────────────────────

    val EGG = FilterDef(
        id = "egg", displayName = "In Egg",
        category = FilterCategory.EGG, valueType = ValueType.BOOLEAN,
        description = "Currently inside an egg"
    )
    val EGGS_ONLY = FilterDef(
        id = "eggsonly", displayName = "Eggs Only",
        category = FilterCategory.EGG, valueType = ValueType.BOOLEAN,
        description = "Show only eggs (hides all hatched Pokémon)",
        searchToken = "eggsonly"
    )
    val HATCHED = FilterDef(
        id = "hatched", displayName = "Hatched From Egg",
        category = FilterCategory.EGG, valueType = ValueType.BOOLEAN,
        description = "Was hatched from an egg"
    )
    val EGG_KM = FilterDef(
        id = "egg_km", displayName = "Egg Distance",
        category = FilterCategory.EGG, valueType = ValueType.ENUM_SINGLE,
        description = "Filter by egg hatch distance category",
        enumOptions = listOf(
            EnumOption("2km",  "2 km"),
            EnumOption("5km",  "5 km"),
            EnumOption("7km",  "7 km (Gift)"),
            EnumOption("10km", "10 km"),
            EnumOption("12km", "12 km (Adventure)")
        )
    )
    val DISTANCE = FilterDef(
        id = "distance", displayName = "Egg Distance Walked",
        category = FilterCategory.EGG, valueType = ValueType.NUMERIC_RANGE,
        description = "Distance already walked for this egg (km range)",
        searchToken = "distance"
    )

    // ── Buddy ─────────────────────────────────────────────────────────────────

    val BUDDY = FilterDef(
        id = "buddy", displayName = "Has Buddy History",
        category = FilterCategory.BUDDY, valueType = ValueType.BOOLEAN,
        description = "Has any buddy interaction history"
    )
    val BUDDY_LEVEL = FilterDef(
        id = "buddy_level", displayName = "Buddy Level",
        category = FilterCategory.BUDDY, valueType = ValueType.ENUM_SINGLE,
        description = "Buddy friendship level",
        enumOptions = listOf(
            EnumOption("buddy0", "Neutral"),
            EnumOption("buddy1", "Good Buddy"),
            EnumOption("buddy2", "Great Buddy"),
            EnumOption("buddy3", "Ultra Buddy"),
            EnumOption("buddy4", "Best Buddy")
        )
    )

    // ── Gender ────────────────────────────────────────────────────────────────

    val GENDER = FilterDef(
        id = "gender", displayName = "Gender",
        category = FilterCategory.GENDER, valueType = ValueType.ENUM_SINGLE,
        description = "Filter by gender",
        enumOptions = listOf(
            EnumOption("male",   "Male ♂️"),
            EnumOption("female", "Female ♀️")
        )
    )
    val GENDER_UNKNOWN = FilterDef(
        id = "genderunknown", displayName = "Gender Unknown",
        category = FilterCategory.GENDER, valueType = ValueType.BOOLEAN,
        description = "Has no gender (genderless Pokémon)",
        searchToken = "genderunknown"
    )

    // ── Regional Forms ────────────────────────────────────────────────────────

    val ALOLAN = FilterDef(
        id = "alolan", displayName = "Alolan Form",
        category = FilterCategory.FORMS, valueType = ValueType.BOOLEAN,
        description = "Is an Alolan regional variant"
    )
    val GALARIAN = FilterDef(
        id = "galarian", displayName = "Galarian Form",
        category = FilterCategory.FORMS, valueType = ValueType.BOOLEAN,
        description = "Is a Galarian regional variant"
    )
    val HISUIAN = FilterDef(
        id = "hisuian", displayName = "Hisuian Form",
        category = FilterCategory.FORMS, valueType = ValueType.BOOLEAN,
        description = "Is a Hisuian regional variant"
    )
    val PALDEAN = FilterDef(
        id = "paldean", displayName = "Paldean Form",
        category = FilterCategory.FORMS, valueType = ValueType.BOOLEAN,
        description = "Is a Paldean regional variant"
    )

    // ── Tags & Favorites ──────────────────────────────────────────────────────

    val FAVORITE = FilterDef(
        id = "favorite", displayName = "Favorite ★",
        category = FilterCategory.TAGS, valueType = ValueType.BOOLEAN,
        description = "Is marked as a favorite (starred)"
    )
    val TAG = FilterDef(
        id = "tag", displayName = "Custom Tag",
        category = FilterCategory.TAGS, valueType = ValueType.TEXT,
        description = "Has a specific custom tag. Enter the tag name (without #).",
        searchToken = "#"
    )

    // ── Age & Time ────────────────────────────────────────────────────────────

    val AGE = FilterDef(
        id = "age", displayName = "Days Since Caught",
        category = FilterCategory.TEMPORAL, valueType = ValueType.NUMERIC_RANGE,
        description = "Days since the Pokémon was caught (age0 = today, age1-7 = last week)",
        searchToken = "age"
    )
    val YEAR = FilterDef(
        id = "year", displayName = "Year Caught",
        category = FilterCategory.TEMPORAL, valueType = ValueType.NUMERIC_RANGE,
        description = "Calendar year caught (e.g. year2024)",
        searchToken = "year"
    )

    // ── Encounter & Origin ────────────────────────────────────────────────────

    val RAID = FilterDef(
        id = "raid", displayName = "Caught in Raid",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught from any raid battle"
    )
    val REMOTE_RAID = FilterDef(
        id = "remoteraid", displayName = "Caught via Remote Raid",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught from a Remote Raid",
        searchToken = "remoteraid"
    )
    val MEGA_RAID = FilterDef(
        id = "megaraid", displayName = "Caught in Mega Raid",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught from a Mega Raid",
        searchToken = "megaraid"
    )
    val EX_RAID = FilterDef(
        id = "exraid", displayName = "Caught in EX Raid",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught from an EX Raid",
        searchToken = "exraid"
    )
    val PRIMAL_RAID = FilterDef(
        id = "primalraid", displayName = "Caught in Primal Raid",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught from a Primal Raid (Kyogre/Groudon)",
        searchToken = "primalraid"
    )
    val RESEARCH = FilterDef(
        id = "research", displayName = "From Field Research",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was caught as a Field Research or Special Research reward"
    )
    val GBL = FilterDef(
        id = "gbl", displayName = "From GO Battle League",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was obtained as a GO Battle League reward"
    )
    val ROCKET = FilterDef(
        id = "rocket", displayName = "From Team GO Rocket",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was rescued from a Team GO Rocket Grunt or Leader"
    )
    val SNAPSHOT = FilterDef(
        id = "snapshot", displayName = "From GO Snapshot",
        category = FilterCategory.ENCOUNTER, valueType = ValueType.BOOLEAN,
        description = "Was encountered via GO Snapshot (AR camera)"
    )

    // ── Custom / Escape Hatch ─────────────────────────────────────────────────

    val RAW = FilterDef(
        id = "raw", displayName = "Custom / Raw Filter",
        category = FilterCategory.IDENTITY, valueType = ValueType.TEXT,
        description = "Enter any raw search term directly for syntax not yet covered by the UI.",
        searchToken = ""
    )

    // ── Master list ───────────────────────────────────────────────────────────

    val ALL: List<FilterDef> = listOf(
        NAME, POKEDEX, TYPE, GENERATION, BABY,
        POKEMON_SIZE,
        CP, HP, IV_STARS, ATK, DEF, STA, LEVEL, WEAK_TO, STRONG_AGAINST,
        MOVE, QUICK_MOVE_TYPE, CHARGE_MOVE_TYPE, LEGACY_MOVE, ELITE_MOVE, SPECIAL_MOVE, PURIFIED_MOVE,
        SHINY, LUCKY, SHADOW, PURIFIED, LEGENDARY, MYTHICAL, ULTRA_BEAST,
        MEGA, DEFENDER, TRADED, COSTUME, NEW, WEATHER, CANDY_XL, REGIONAL, BACKGROUND,
        EVOLVE, EVOLVE_NEW, TRADE_EVOLVE, EVOLVE_ITEM, MEGA_EVOLVE, MEGA_LEVEL,
        EGG, EGGS_ONLY, HATCHED, EGG_KM, DISTANCE,
        BUDDY, BUDDY_LEVEL,
        GENDER, GENDER_UNKNOWN,
        ALOLAN, GALARIAN, HISUIAN, PALDEAN,
        FAVORITE, TAG,
        AGE, YEAR,
        RAID, REMOTE_RAID, MEGA_RAID, EX_RAID, PRIMAL_RAID, RESEARCH, GBL, ROCKET, SNAPSHOT,
        RAW
    )

    val BY_CATEGORY: Map<FilterCategory, List<FilterDef>> =
        ALL.groupBy { it.category }

    /**
     * Boolean filters keyed by their search token.
     * Excludes @/# prefixed tokens since those are handled specially by the parser.
     */
    val BOOLEAN_BY_TOKEN: Map<String, FilterDef> = ALL
        .filter { def ->
            def.valueType == ValueType.BOOLEAN &&
                !def.searchToken.startsWith("@") &&
                !def.searchToken.startsWith("#")
        }
        .associateBy { it.searchToken.ifBlank { it.id } }

    /** All enum option keys mapped to their parent FilterDef. */
    val ENUM_BY_OPTION: Map<String, FilterDef> = ALL
        .filter { it.valueType == ValueType.ENUM_SINGLE }
        .flatMap { def -> def.enumOptions.map { it.key to def } }
        .toMap()

    fun findById(id: String): FilterDef? = ALL.find { it.id == id }
}
