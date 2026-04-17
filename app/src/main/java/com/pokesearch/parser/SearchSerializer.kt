package com.pokesearch.parser

import com.pokesearch.model.*

/**
 * Converts a [QueryNode] tree back into the Pokémon GO search string syntax.
 */
object SearchSerializer {

    /**
     * Serialize the root [GroupNode].
     * The root group never gets outer parentheses; only nested groups do.
     */
    fun serializeRoot(root: QueryNode.GroupNode): String {
        if (root.children.isEmpty()) return ""
        val sep = root.operator.separator
        return root.children.joinToString(sep) { serializeNode(it, isRoot = false) }
    }

    // ── Node serialization ────────────────────────────────────────────────────

    // Negation is handled inside serializeFilter (for filters) and serializeGroup (for groups).
    private fun serializeNode(node: QueryNode, isRoot: Boolean): String = when (node) {
        is QueryNode.FilterNode -> serializeFilter(node)
        is QueryNode.GroupNode  -> serializeGroup(node, isRoot)
    }

    private fun serializeGroup(group: QueryNode.GroupNode, isRoot: Boolean): String {
        if (group.children.isEmpty()) return ""
        val sep   = group.operator.separator
        val inner = group.children.joinToString(sep) { serializeNode(it, isRoot = false) }
        val wrapped = when {
            isRoot                                   -> inner
            group.negated || group.children.size > 1 -> "($inner)"
            else                                     -> inner
        }
        return if (group.negated) "!$wrapped" else wrapped
    }

    // ── Filter serialization ──────────────────────────────────────────────────

    fun serializeFilter(filter: QueryNode.FilterNode): String {
        val str = filterToSearchToken(filter.filterDef, filter.value)
        return if (filter.negated) "!$str" else str
    }

    fun filterToSearchToken(def: FilterDef, value: FilterValue): String = when (def.id) {

        // Identity
        "name"       -> (value as FilterValue.TextVal).text
        "pokedex"    -> {
            val pv = value as FilterValue.PokedexRange
            if (pv.end != null) "#${pv.start}-${pv.end}" else "#${pv.start}"
        }

        // Enum / plain key (enum key IS the search token)
        "type", "generation", "gender", "egg_km", "buddy_level",
        "stars", "mega_level", "pokemon_size" ->
            (value as FilterValue.EnumVal).key

        // Numeric ranges
        "cp"       -> numericRangeString("cp",       value as FilterValue.NumericRange)
        "hp"       -> numericRangeString("hp",       value as FilterValue.NumericRange)
        "atk"      -> numericRangeString("atk",      value as FilterValue.NumericRange)
        "def"      -> numericRangeString("def",      value as FilterValue.NumericRange)
        "sta"      -> numericRangeString("sta",      value as FilterValue.NumericRange)
        "level"    -> numericRangeString("level",    value as FilterValue.NumericRange)
        "age"      -> numericRangeString("age",      value as FilterValue.NumericRange)
        "year"     -> numericRangeString("year",     value as FilterValue.NumericRange)
        "distance" -> numericRangeString("distance", value as FilterValue.NumericRange)

        // Combat advantage (< and >)
        "weak_to"       -> "<${(value as FilterValue.TextVal).text}"
        "strong_against" -> ">${(value as FilterValue.TextVal).text}"

        // Moves
        "move"            -> "@${(value as FilterValue.TextVal).text}"
        "quick_move_type" -> "@1${(value as FilterValue.TextVal).text}"
        "charge_move_type"-> "@2${(value as FilterValue.TextVal).text}"
        "legacy_move"     -> "@legacy"
        "elite_move"      -> "@elite"
        "special_move"    -> "@special"
        "purified_move"   -> "@purified"

        // Tags
        "tag"      -> "#${(value as FilterValue.TextVal).text}"
        "favorite" -> "favorite"

        // Raw / custom
        "raw" -> (value as FilterValue.TextVal).text

        // All other filters: emit their searchToken (or id if token is blank).
        // This covers all boolean filters (shiny, lucky, megaevolve, raid, …)
        // and any future additions, without needing an explicit case here.
        else -> def.searchToken.ifBlank { def.id }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun numericRangeString(prefix: String, range: FilterValue.NumericRange): String {
        val (min, max) = range
        return when {
            min != null && max != null && min == max -> "$prefix$min"
            min != null && max != null               -> "$prefix$min-$max"
            min != null                              -> "$prefix$min-"
            max != null                              -> "$prefix-$max"
            else                                     -> prefix
        }
    }

    /** Human-readable summary of a filter's value (used in the UI chip label). */
    fun filterValueSummary(def: FilterDef, value: FilterValue): String = when (value) {
        is FilterValue.BooleanPresent -> def.displayName
        is FilterValue.TextVal        -> "${def.displayName}: ${value.text}"
        is FilterValue.EnumVal        -> {
            val label = def.enumOptions.find { it.key == value.key }?.displayName ?: value.key
            "${def.displayName}: $label"
        }
        is FilterValue.NumericRange   -> {
            val (min, max) = value
            when {
                min != null && max != null && min == max -> "${def.displayName}: $min"
                min != null && max != null               -> "${def.displayName}: $min–$max"
                min != null                              -> "${def.displayName}: ≥$min"
                max != null                              -> "${def.displayName}: ≤$max"
                else                                     -> def.displayName
            }
        }
        is FilterValue.PokedexRange   ->
            if (value.end != null) "Pokédex: #${value.start}–#${value.end}"
            else "Pokédex: #${value.start}"
    }
}
