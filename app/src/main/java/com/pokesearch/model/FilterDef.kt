package com.pokesearch.model

enum class FilterCategory(val displayName: String) {
    IDENTITY("Pokémon Identity"),
    COMBAT("Combat Stats"),
    MOVES("Moves"),
    STATUS("Status & Special"),
    EVOLUTION("Evolution"),
    EGG("Egg & Hatch"),
    BUDDY("Buddy"),
    GENDER("Gender"),
    FORMS("Regional Forms"),
    TAGS("Tags & Favorites"),
    TEMPORAL("Age & Time"),
    ENCOUNTER("Encounter & Origin"),
    SIZE("Pokémon Size")
}

enum class ValueType {
    BOOLEAN,
    TEXT,
    NUMERIC_RANGE,
    ENUM_SINGLE,
    POKEDEX_RANGE
}

enum class LogicOperator(val displayName: String, val separator: String) {
    AND("AND", "&"),
    OR("OR", ",")
}

data class EnumOption(val key: String, val displayName: String)

/**
 * Definition of a single filter type supported in Pokémon GO's search syntax.
 *
 * @param id           Unique internal identifier for this filter type.
 * @param displayName  Human-readable name shown in the UI.
 * @param category     Category grouping for the filter picker UI.
 * @param valueType    How values for this filter are expressed (boolean, text, etc.).
 * @param description  Tooltip/help text for the filter.
 * @param searchToken  The token prefix or keyword that appears in the actual search string.
 *                     For BOOLEAN filters this is the exact keyword (e.g. "shiny").
 *                     For TEXT/NUMERIC_RANGE filters this is the prefix (e.g. "cp", "@").
 *                     For ENUM_SINGLE the individual enum keys are used directly.
 * @param enumOptions  Ordered list of valid values for ENUM_SINGLE filters.
 */
data class FilterDef(
    val id: String,
    val displayName: String,
    val category: FilterCategory,
    val valueType: ValueType,
    val description: String = "",
    val searchToken: String = id,
    val enumOptions: List<EnumOption> = emptyList()
)

sealed class FilterValue {
    /** Used for BOOLEAN filters — the filter is present, no value needed. */
    data object BooleanPresent : FilterValue()

    /** Used for TEXT filters (name, move, tag). */
    data class TextVal(val text: String) : FilterValue()

    /**
     * Used for NUMERIC_RANGE filters (CP, HP, ATK, DEF, STA, level, age, year).
     * null min  → no lower bound (e.g. cp-1500)
     * null max  → no upper bound (e.g. cp1500-)
     * min==max  → exact value   (e.g. cp1500)
     * both set  → range         (e.g. cp1000-1500)
     */
    data class NumericRange(val min: Int?, val max: Int?) : FilterValue()

    /** Used for ENUM_SINGLE filters — holds the selected enum key. */
    data class EnumVal(val key: String) : FilterValue()

    /** Used for the Pokédex filter. end==null means single number. */
    data class PokedexRange(val start: Int, val end: Int?) : FilterValue()
}
