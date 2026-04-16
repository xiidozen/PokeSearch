package com.pokesearch.parser

import com.pokesearch.model.*

/**
 * Recursive-descent parser that converts a Pokémon GO search string into a [QueryNode.GroupNode].
 *
 * Grammar (operator precedence — AND binds tighter than OR, matching PokéGO behaviour):
 * ```
 * Expression → OrExpr
 * OrExpr     → AndExpr (',' AndExpr)*
 * AndExpr    → UnaryExpr ('&' UnaryExpr)*
 * UnaryExpr  → '!' UnaryExpr | Primary
 * Primary    → '(' Expression ')' | FilterToken
 * FilterToken → <sequence of chars excluding & , ( ) !>
 * ```
 */
class SearchParser {

    private var pos = 0
    private var input = ""

    /** Parse [searchString] and return a [QueryNode.GroupNode] representing the full query. */
    fun parse(searchString: String): QueryNode.GroupNode {
        pos = 0
        input = searchString.trim()
        if (input.isEmpty()) {
            return emptyRoot()
        }
        return wrapInGroup(parseOr())
    }

    // ── Grammar rules ─────────────────────────────────────────────────────────

    private fun parseOr(): QueryNode {
        val left = parseAnd()
        if (peek() != ',') return left
        val children = mutableListOf(left)
        while (peek() == ',') {
            advance()           // consume ','
            children.add(parseAnd())
        }
        return QueryNode.GroupNode(NodeId.generate(), LogicOperator.OR, children)
    }

    private fun parseAnd(): QueryNode {
        val left = parseUnary()
        if (peek() != '&') return left
        val children = mutableListOf(left)
        while (peek() == '&') {
            advance()           // consume '&'
            children.add(parseUnary())
        }
        return QueryNode.GroupNode(NodeId.generate(), LogicOperator.AND, children)
    }

    private fun parseUnary(): QueryNode {
        skipWhitespace()
        return if (peek() == '!') {
            advance()           // consume '!'
            val child = parsePrimary()
            when (child) {
                is QueryNode.FilterNode -> child.copy(negated = true)
                is QueryNode.GroupNode  -> child.copy(negated = true)
            }
        } else {
            parsePrimary()
        }
    }

    private fun parsePrimary(): QueryNode {
        skipWhitespace()
        return if (peek() == '(') {
            advance()           // consume '('
            val expr = parseOr()
            skipWhitespace()
            if (peek() == ')') advance()    // consume ')'
            // If the parenthesised sub-expression is already a group, return it directly.
            // A single filter inside parens is wrapped in a trivial group.
            when (expr) {
                is QueryNode.GroupNode  -> expr
                is QueryNode.FilterNode ->
                    QueryNode.GroupNode(NodeId.generate(), LogicOperator.AND, listOf(expr))
            }
        } else {
            parseFilterToken()
        }
    }

    private fun parseFilterToken(): QueryNode.FilterNode {
        skipWhitespace()
        val start = pos
        // Consume everything that is not an operator character.
        // Special case: '@' and '#' introduce prefixed tokens — keep reading.
        while (pos < input.length && input[pos] !in OPERATOR_CHARS) {
            pos++
        }
        val token = input.substring(start, pos).trim()
        return buildFilterNode(token)
    }

    // ── Token → FilterNode ────────────────────────────────────────────────────

    private fun buildFilterNode(token: String): QueryNode.FilterNode {
        val id = NodeId.generate()
        val lower = token.lowercase()

        return when {
            token.isEmpty() ->
                QueryNode.FilterNode(id, FilterDefs.RAW, FilterValue.TextVal(""))

            // ── Pokédex: #N or #N-M ──────────────────────────────────────────
            lower.matches(REGEX_POKEDEX) -> {
                val inner = token.drop(1)
                val dashIdx = inner.indexOf('-')
                val start: Int
                val end: Int?
                if (dashIdx < 0) {
                    start = inner.toIntOrNull() ?: 1; end = null
                } else {
                    start = inner.substring(0, dashIdx).toIntOrNull() ?: 1
                    end   = inner.substring(dashIdx + 1).toIntOrNull()
                }
                QueryNode.FilterNode(id, FilterDefs.POKEDEX, FilterValue.PokedexRange(start, end))
            }

            // ── IV stars: 0* … 4* ────────────────────────────────────────────
            lower.matches(REGEX_STARS) ->
                QueryNode.FilterNode(id, FilterDefs.IV_STARS, FilterValue.EnumVal(lower))

            // ── Numeric-range filters ─────────────────────────────────────────
            lower.matches(REGEX_CP)    -> numericFilter(id, FilterDefs.CP,    lower, "cp")
            lower.matches(REGEX_HP)    -> numericFilter(id, FilterDefs.HP,    lower, "hp")
            lower.matches(REGEX_ATK)   -> numericFilter(id, FilterDefs.ATK,   lower, "atk")
            lower.matches(REGEX_DEF)   -> numericFilter(id, FilterDefs.DEF,   lower, "def")
            lower.matches(REGEX_STA)   -> numericFilter(id, FilterDefs.STA,   lower, "sta")
            lower.matches(REGEX_LEVEL) -> numericFilter(id, FilterDefs.LEVEL, lower, "level")
            lower.matches(REGEX_AGE)   -> numericFilter(id, FilterDefs.AGE,   lower, "age")
            lower.matches(REGEX_YEAR)  -> numericFilter(id, FilterDefs.YEAR,  lower, "year")

            // ── Move filters: @<name>, @legacy, @elite, @special, @purified ──
            lower.startsWith("@") -> {
                val movePart = lower.drop(1)
                when (movePart) {
                    "legacy"   -> QueryNode.FilterNode(id, FilterDefs.LEGACY_MOVE,   FilterValue.BooleanPresent)
                    "elite"    -> QueryNode.FilterNode(id, FilterDefs.ELITE_MOVE,    FilterValue.BooleanPresent)
                    "special"  -> QueryNode.FilterNode(id, FilterDefs.SPECIAL_MOVE,  FilterValue.BooleanPresent)
                    "purified" -> QueryNode.FilterNode(id, FilterDefs.PURIFIED_MOVE, FilterValue.BooleanPresent)
                    else       -> QueryNode.FilterNode(id, FilterDefs.MOVE, FilterValue.TextVal(token.drop(1)))
                }
            }

            // ── Tag filters: #<name>, #favorite ──────────────────────────────
            lower.startsWith("#") -> {
                val tagPart = lower.drop(1)
                // Numeric Pokédex already handled above, so '#' here is a tag.
                when (tagPart) {
                    "favorite" -> QueryNode.FilterNode(id, FilterDefs.FAVORITE, FilterValue.BooleanPresent)
                    else       -> QueryNode.FilterNode(id, FilterDefs.TAG, FilterValue.TextVal(token.drop(1)))
                }
            }

            // ── Egg-distance keywords ─────────────────────────────────────────
            lower in EGG_DISTANCES ->
                QueryNode.FilterNode(id, FilterDefs.EGG_KM, FilterValue.EnumVal(lower))

            // ── Buddy-level keywords: buddy0 … buddy4 ────────────────────────
            lower.matches(REGEX_BUDDY_LEVEL) ->
                QueryNode.FilterNode(id, FilterDefs.BUDDY_LEVEL, FilterValue.EnumVal(lower))

            // ── Known boolean filters (shiny, lucky, shadow, …) ───────────────
            FilterDefs.BOOLEAN_BY_TOKEN.containsKey(lower) ->
                QueryNode.FilterNode(id, FilterDefs.BOOLEAN_BY_TOKEN[lower]!!, FilterValue.BooleanPresent)

            // ── Known enum options (fire, gen1, male, alolan, …) ─────────────
            FilterDefs.ENUM_BY_OPTION.containsKey(lower) ->
                QueryNode.FilterNode(id, FilterDefs.ENUM_BY_OPTION[lower]!!, FilterValue.EnumVal(lower))

            // ── Fallback: treat as Pokémon name search ────────────────────────
            else ->
                QueryNode.FilterNode(id, FilterDefs.NAME, FilterValue.TextVal(token))
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun numericFilter(
        id: NodeId,
        def: FilterDef,
        lower: String,
        prefix: String
    ): QueryNode.FilterNode {
        val inner = lower.drop(prefix.length)
        return QueryNode.FilterNode(id, def, parseNumericRange(inner))
    }

    internal fun parseNumericRange(inner: String): FilterValue.NumericRange {
        if (inner.isEmpty()) return FilterValue.NumericRange(null, null)
        return when {
            inner.startsWith("-") ->
                FilterValue.NumericRange(null, inner.drop(1).toIntOrNull())
            inner.endsWith("-") ->
                FilterValue.NumericRange(inner.dropLast(1).toIntOrNull(), null)
            inner.contains("-") -> {
                val dash = inner.indexOf('-')
                FilterValue.NumericRange(
                    inner.substring(0, dash).toIntOrNull(),
                    inner.substring(dash + 1).toIntOrNull()
                )
            }
            else -> {
                val v = inner.toIntOrNull()
                FilterValue.NumericRange(v, v)
            }
        }
    }

    private fun wrapInGroup(node: QueryNode): QueryNode.GroupNode = when (node) {
        // A non-negated group at the top level becomes the root group directly.
        is QueryNode.GroupNode  ->
            if (!node.negated) node.copy(id = NodeId.ROOT)
            else QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, listOf(node))
        is QueryNode.FilterNode ->
            QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, listOf(node))
    }

    private fun emptyRoot() =
        QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, emptyList())

    private fun peek(): Char? {
        skipWhitespace()
        return if (pos < input.length) input[pos] else null
    }

    private fun advance() { pos++ }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }

    companion object {
        private val OPERATOR_CHARS = setOf('&', ',', '(', ')', '!')

        private val REGEX_POKEDEX     = Regex("#\\d+(-\\d+)?")
        private val REGEX_STARS       = Regex("[0-4]\\*")
        private val REGEX_CP          = Regex("cp\\d*-?\\d*")
        private val REGEX_HP          = Regex("hp\\d*-?\\d*")
        private val REGEX_ATK         = Regex("atk\\d*-?\\d*")
        private val REGEX_DEF         = Regex("def\\d*-?\\d*")
        private val REGEX_STA         = Regex("sta\\d*-?\\d*")
        private val REGEX_LEVEL       = Regex("level\\d*-?\\d*")
        private val REGEX_AGE         = Regex("age\\d*-?\\d*")
        private val REGEX_YEAR        = Regex("year\\d*-?\\d*")
        private val REGEX_BUDDY_LEVEL = Regex("buddy[0-4]")

        private val EGG_DISTANCES = setOf("2km", "5km", "7km", "10km", "12km")
    }
}
