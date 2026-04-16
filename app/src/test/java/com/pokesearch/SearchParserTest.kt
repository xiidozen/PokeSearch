package com.pokesearch

import com.pokesearch.model.*
import com.pokesearch.parser.SearchParser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchParserTest {

    private lateinit var parser: SearchParser

    @Before fun setUp() { parser = SearchParser() }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parse(s: String) = parser.parse(s)
    private fun firstFilter(s: String) =
        (parse(s).children.firstOrNull() as? QueryNode.FilterNode)
            ?: error("Expected a FilterNode as first child of '$s'")

    // ── Empty / trivial ───────────────────────────────────────────────────────

    @Test fun emptyString_returnsEmptyRoot() {
        val root = parse("")
        assertTrue(root.children.isEmpty())
    }

    @Test fun whitespaceOnly_returnsEmptyRoot() {
        val root = parse("   ")
        assertTrue(root.children.isEmpty())
    }

    // ── Boolean filters ───────────────────────────────────────────────────────

    @Test fun parsesShiny() {
        val f = firstFilter("shiny")
        assertEquals("shiny", f.filterDef.id)
        assertEquals(FilterValue.BooleanPresent, f.value)
        assertFalse(f.negated)
    }

    @Test fun parsesLucky() {
        assertEquals("lucky", firstFilter("lucky").filterDef.id)
    }

    @Test fun parsesShadow()   { assertEquals("shadow",   firstFilter("shadow").filterDef.id) }
    @Test fun parsesPurified() { assertEquals("purified", firstFilter("purified").filterDef.id) }
    @Test fun parsesLegendary(){ assertEquals("legendary",firstFilter("legendary").filterDef.id) }
    @Test fun parsesMythical() { assertEquals("mythical", firstFilter("mythical").filterDef.id) }
    @Test fun parsesUltraBeast(){ assertEquals("ultrabeast", firstFilter("ultrabeast").filterDef.id) }
    @Test fun parsesMega()     { assertEquals("mega",      firstFilter("mega").filterDef.id) }
    @Test fun parsesDefender() { assertEquals("defender",  firstFilter("defender").filterDef.id) }
    @Test fun parsesTraded()   { assertEquals("traded",    firstFilter("traded").filterDef.id) }
    @Test fun parsesCostume()  { assertEquals("costume",   firstFilter("costume").filterDef.id) }
    @Test fun parsesNew()      { assertEquals("new",       firstFilter("new").filterDef.id) }
    @Test fun parsesWeather()  { assertEquals("weather",   firstFilter("weather").filterDef.id) }
    @Test fun parsesXl()       { assertEquals("xl",        firstFilter("xl").filterDef.id) }
    @Test fun parsesRegional() { assertEquals("regional",  firstFilter("regional").filterDef.id) }
    @Test fun parsesEvolve()   { assertEquals("evolve",    firstFilter("evolve").filterDef.id) }
    @Test fun parsesTradeEvolve(){ assertEquals("tradeevolve", firstFilter("tradeevolve").filterDef.id) }
    @Test fun parsesEgg()      { assertEquals("egg",       firstFilter("egg").filterDef.id) }
    @Test fun parsesHatched()  { assertEquals("hatched",   firstFilter("hatched").filterDef.id) }
    @Test fun parsesBuddy()    { assertEquals("buddy",     firstFilter("buddy").filterDef.id) }
    @Test fun parsesAlolan()   { assertEquals("alolan",    firstFilter("alolan").filterDef.id) }
    @Test fun parsesGalarian() { assertEquals("galarian",  firstFilter("galarian").filterDef.id) }
    @Test fun parsesHisuian()  { assertEquals("hisuian",   firstFilter("hisuian").filterDef.id) }
    @Test fun parsesPaldean()  { assertEquals("paldean",   firstFilter("paldean").filterDef.id) }
    @Test fun parsesFavorite() { assertEquals("favorite",  firstFilter("favorite").filterDef.id) }

    // ── Negation ──────────────────────────────────────────────────────────────

    @Test fun parsesNegatedFilter() {
        val f = firstFilter("!shiny")
        assertEquals("shiny", f.filterDef.id)
        assertTrue(f.negated)
    }

    @Test fun parsesDoubleNegation() {
        // !!shiny is unusual but parser should handle gracefully
        val root = parse("!!shiny")
        // Inner negation flips twice — result depends on parser; we just ensure no crash
        assertNotNull(root)
    }

    // ── CP / HP / numeric ranges ──────────────────────────────────────────────

    @Test fun parsesCpExact() {
        val f = firstFilter("cp1500")
        assertEquals("cp", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(1500, 1500), f.value)
    }

    @Test fun parsesCpMax() {
        val f = firstFilter("cp-1500")
        assertEquals("cp", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(null, 1500), f.value)
    }

    @Test fun parsesCpMin() {
        val f = firstFilter("cp1500-")
        assertEquals(FilterValue.NumericRange(1500, null), f.value)
    }

    @Test fun parsesCpRange() {
        val f = firstFilter("cp1000-1500")
        assertEquals(FilterValue.NumericRange(1000, 1500), f.value)
    }

    @Test fun parsesHpRange() {
        val f = firstFilter("hp50-100")
        assertEquals("hp", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(50, 100), f.value)
    }

    @Test fun parsesAtkIv() {
        val f = firstFilter("atk15")
        assertEquals("atk", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(15, 15), f.value)
    }

    @Test fun parsesDefIv() {
        val f = firstFilter("def0-14")
        assertEquals("def", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(0, 14), f.value)
    }

    @Test fun parsesStaIv() {
        val f = firstFilter("sta-10")
        assertEquals("sta", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(null, 10), f.value)
    }

    @Test fun parsesLevel() {
        val f = firstFilter("level40")
        assertEquals("level", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(40, 40), f.value)
    }

    @Test fun parsesAge() {
        val f = firstFilter("age1-7")
        assertEquals("age", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(1, 7), f.value)
    }

    @Test fun parsesYear() {
        val f = firstFilter("year2024")
        assertEquals("year", f.filterDef.id)
        assertEquals(FilterValue.NumericRange(2024, 2024), f.value)
    }

    // ── Star ratings ──────────────────────────────────────────────────────────

    @Test fun parsesFourStar() {
        val f = firstFilter("4*")
        assertEquals("stars", f.filterDef.id)
        assertEquals(FilterValue.EnumVal("4*"), f.value)
    }

    @Test fun parsesZeroStar() {
        val f = firstFilter("0*")
        assertEquals("stars", f.filterDef.id)
        assertEquals(FilterValue.EnumVal("0*"), f.value)
    }

    @Test fun parsesThreeStar() {
        val f = firstFilter("3*")
        assertEquals(FilterValue.EnumVal("3*"), f.value)
    }

    // ── Type filters ──────────────────────────────────────────────────────────

    @Test fun parsesTypeFilter() {
        val f = firstFilter("fire")
        assertEquals("type", f.filterDef.id)
        assertEquals(FilterValue.EnumVal("fire"), f.value)
    }

    @Test fun parsesAllTypes() {
        val types = listOf("normal","fire","water","electric","grass","ice","fighting",
            "poison","ground","flying","psychic","bug","rock","ghost","dragon","dark","steel","fairy")
        for (type in types) {
            val f = firstFilter(type)
            assertEquals("type should be detected for '$type'", "type", f.filterDef.id)
        }
    }

    // ── Generation ────────────────────────────────────────────────────────────

    @Test fun parsesGeneration() {
        val f = firstFilter("gen1")
        assertEquals("generation", f.filterDef.id)
        assertEquals(FilterValue.EnumVal("gen1"), f.value)
    }

    @Test fun parsesAllGenerations() {
        for (i in 1..9) {
            val f = firstFilter("gen$i")
            assertEquals("generation", f.filterDef.id)
        }
    }

    // ── Pokédex range ─────────────────────────────────────────────────────────

    @Test fun parsesPokedexSingle() {
        val f = firstFilter("#151")
        assertEquals("pokedex", f.filterDef.id)
        assertEquals(FilterValue.PokedexRange(151, null), f.value)
    }

    @Test fun parsesPokedexRange() {
        val f = firstFilter("#1-151")
        assertEquals(FilterValue.PokedexRange(1, 151), f.value)
    }

    // ── Move filters ──────────────────────────────────────────────────────────

    @Test fun parsesAnyMove() {
        val f = firstFilter("@ember")
        assertEquals("move", f.filterDef.id)
        assertEquals(FilterValue.TextVal("ember"), f.value)
    }

    @Test fun parsesLegacyMove() {
        val f = firstFilter("@legacy")
        assertEquals("legacy_move", f.filterDef.id)
        assertEquals(FilterValue.BooleanPresent, f.value)
    }

    @Test fun parsesEliteMove() {
        val f = firstFilter("@elite")
        assertEquals("elite_move", f.filterDef.id)
    }

    @Test fun parsesSpecialMove() {
        val f = firstFilter("@special")
        assertEquals("special_move", f.filterDef.id)
    }

    @Test fun parsesPurifiedMove() {
        val f = firstFilter("@purified")
        assertEquals("purified_move", f.filterDef.id)
    }

    // ── Egg / buddy / gender / forms ──────────────────────────────────────────

    @Test fun parsesEggDistance() {
        listOf("2km","5km","7km","10km","12km").forEach { km ->
            val f = firstFilter(km)
            assertEquals("egg_km", f.filterDef.id)
            assertEquals(FilterValue.EnumVal(km), f.value)
        }
    }

    @Test fun parsesBuddyLevel() {
        for (i in 0..4) {
            val f = firstFilter("buddy$i")
            assertEquals("buddy_level", f.filterDef.id)
            assertEquals(FilterValue.EnumVal("buddy$i"), f.value)
        }
    }

    @Test fun parsesGender() {
        assertEquals("gender", firstFilter("male").filterDef.id)
        assertEquals("gender", firstFilter("female").filterDef.id)
    }

    // ── Custom tags ───────────────────────────────────────────────────────────

    @Test fun parsesCustomTag() {
        val f = firstFilter("#mytag")
        assertEquals("tag", f.filterDef.id)
        assertEquals(FilterValue.TextVal("mytag"), f.value)
    }

    @Test fun parsesFavoriteTag() {
        val f = firstFilter("#favorite")
        assertEquals("favorite", f.filterDef.id)
    }

    // ── Name / raw fallback ───────────────────────────────────────────────────

    @Test fun parsesNameFallback() {
        val f = firstFilter("pikachu")
        assertEquals("name", f.filterDef.id)
        assertEquals(FilterValue.TextVal("pikachu"), f.value)
    }

    // ── Compound expressions ──────────────────────────────────────────────────

    @Test fun parsesAndExpression() {
        val root = parse("shiny&lucky")
        assertEquals(LogicOperator.AND, root.operator)
        assertEquals(2, root.children.size)
        assertEquals("shiny", (root.children[0] as QueryNode.FilterNode).filterDef.id)
        assertEquals("lucky", (root.children[1] as QueryNode.FilterNode).filterDef.id)
    }

    @Test fun parsesOrExpression() {
        val root = parse("shiny,lucky")
        assertEquals(LogicOperator.OR, root.operator)
        assertEquals(2, root.children.size)
    }

    @Test fun parsesGrouped() {
        val root = parse("(shiny,lucky)&cp-1500")
        // Root should be AND of a group and a cp filter
        assertEquals(LogicOperator.AND, root.operator)
        assertEquals(2, root.children.size)
        val leftGroup = root.children[0] as QueryNode.GroupNode
        assertEquals(LogicOperator.OR, leftGroup.operator)
        assertEquals(2, leftGroup.children.size)
    }

    @Test fun parsesNegatedGroup() {
        val root = parse("!(shiny,lucky)")
        val group = root.children[0] as QueryNode.GroupNode
        assertTrue(group.negated)
    }

    @Test fun parsesDeepNesting() {
        // ((cp1000-1500&4*),(shiny&!shadow))
        val root = parse("((cp1000-1500&4*),(shiny&!shadow))")
        assertNotNull(root)
        assertTrue(root.children.isNotEmpty())
    }

    @Test fun parsesComplexExpression() {
        // The example from the design: ((IV 1 and IV 4) or (not IV 3 and SHINY))
        // Translated to PokéGO syntax: (1*&4*),(!(3*)&shiny)  [approximation]
        val root = parse("(1*&4*),(!(3*)&shiny)")
        assertNotNull(root)
        assertEquals(LogicOperator.OR, root.operator)
        assertEquals(2, root.children.size)
    }

    @Test fun andBindsTighterThanOr() {
        // a,b&c should parse as OR(a, AND(b, c))
        val root = parse("shiny,lucky&shadow")
        assertEquals(LogicOperator.OR, root.operator)
        assertEquals(2, root.children.size)
        val right = root.children[1] as QueryNode.GroupNode
        assertEquals(LogicOperator.AND, right.operator)
        assertEquals(2, right.children.size)
    }

    @Test fun parsesThreeWayAnd() {
        val root = parse("shiny&lucky&shadow")
        assertEquals(LogicOperator.AND, root.operator)
        assertEquals(3, root.children.size)
    }

    @Test fun parsesThreeWayOr() {
        val root = parse("shiny,lucky,shadow")
        assertEquals(LogicOperator.OR, root.operator)
        assertEquals(3, root.children.size)
    }

    // ── Numeric range helper ──────────────────────────────────────────────────

    @Test fun numericRangeHelper_exact() {
        val r = parser.parseNumericRange("1500")
        assertEquals(FilterValue.NumericRange(1500, 1500), r)
    }

    @Test fun numericRangeHelper_maxOnly() {
        val r = parser.parseNumericRange("-1500")
        assertEquals(FilterValue.NumericRange(null, 1500), r)
    }

    @Test fun numericRangeHelper_minOnly() {
        val r = parser.parseNumericRange("1500-")
        assertEquals(FilterValue.NumericRange(1500, null), r)
    }

    @Test fun numericRangeHelper_range() {
        val r = parser.parseNumericRange("1000-1500")
        assertEquals(FilterValue.NumericRange(1000, 1500), r)
    }

    @Test fun numericRangeHelper_empty() {
        val r = parser.parseNumericRange("")
        assertEquals(FilterValue.NumericRange(null, null), r)
    }
}
