package com.pokesearch

import com.pokesearch.model.*
import com.pokesearch.parser.SearchParser
import com.pokesearch.parser.SearchSerializer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchSerializerTest {

    private lateinit var parser: SearchParser

    @Before fun setUp() { parser = SearchParser() }

    private fun roundTrip(s: String): String =
        SearchSerializer.serializeRoot(parser.parse(s))

    private fun node(def: FilterDef, value: FilterValue, negated: Boolean = false) =
        QueryNode.FilterNode(NodeId.generate(), def, value, negated)

    private fun group(op: LogicOperator, vararg children: QueryNode, negated: Boolean = false) =
        QueryNode.GroupNode(NodeId.generate(), op, children.toList(), negated)

    private fun root(op: LogicOperator, vararg children: QueryNode) =
        QueryNode.GroupNode(NodeId.ROOT, op, children.toList())

    // ── Boolean filters ───────────────────────────────────────────────────────

    @Test fun serializes_shiny()      { assertEquals("shiny",  SearchSerializer.serializeFilter(node(FilterDefs.SHINY, FilterValue.BooleanPresent))) }
    @Test fun serializes_lucky()      { assertEquals("lucky",  SearchSerializer.serializeFilter(node(FilterDefs.LUCKY, FilterValue.BooleanPresent))) }
    @Test fun serializes_shadow()     { assertEquals("shadow", SearchSerializer.serializeFilter(node(FilterDefs.SHADOW, FilterValue.BooleanPresent))) }
    @Test fun serializes_purified()   { assertEquals("purified", SearchSerializer.serializeFilter(node(FilterDefs.PURIFIED, FilterValue.BooleanPresent))) }
    @Test fun serializes_legendary()  { assertEquals("legendary", SearchSerializer.serializeFilter(node(FilterDefs.LEGENDARY, FilterValue.BooleanPresent))) }
    @Test fun serializes_mega()       { assertEquals("mega",   SearchSerializer.serializeFilter(node(FilterDefs.MEGA, FilterValue.BooleanPresent))) }
    @Test fun serializes_evolve()     { assertEquals("evolve", SearchSerializer.serializeFilter(node(FilterDefs.EVOLVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_tradeevolve(){ assertEquals("tradeevolve", SearchSerializer.serializeFilter(node(FilterDefs.TRADE_EVOLVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_alolan()     { assertEquals("alolan", SearchSerializer.serializeFilter(node(FilterDefs.ALOLAN, FilterValue.BooleanPresent))) }
    @Test fun serializes_favorite()   { assertEquals("favorite", SearchSerializer.serializeFilter(node(FilterDefs.FAVORITE, FilterValue.BooleanPresent))) }

    // ── Negation ──────────────────────────────────────────────────────────────

    @Test fun serializes_negated_filter() {
        val f = node(FilterDefs.SHINY, FilterValue.BooleanPresent, negated = true)
        assertEquals("!shiny", SearchSerializer.serializeFilter(f))
    }

    // ── CP / HP / numeric ranges ──────────────────────────────────────────────

    @Test fun serializes_cp_exact()  { assertEquals("cp1500",      SearchSerializer.serializeFilter(node(FilterDefs.CP, FilterValue.NumericRange(1500, 1500)))) }
    @Test fun serializes_cp_max()    { assertEquals("cp-1500",     SearchSerializer.serializeFilter(node(FilterDefs.CP, FilterValue.NumericRange(null, 1500)))) }
    @Test fun serializes_cp_min()    { assertEquals("cp1500-",     SearchSerializer.serializeFilter(node(FilterDefs.CP, FilterValue.NumericRange(1500, null)))) }
    @Test fun serializes_cp_range()  { assertEquals("cp1000-1500", SearchSerializer.serializeFilter(node(FilterDefs.CP, FilterValue.NumericRange(1000, 1500)))) }
    @Test fun serializes_hp_range()  { assertEquals("hp50-100",    SearchSerializer.serializeFilter(node(FilterDefs.HP, FilterValue.NumericRange(50, 100)))) }
    @Test fun serializes_atk()       { assertEquals("atk15",       SearchSerializer.serializeFilter(node(FilterDefs.ATK, FilterValue.NumericRange(15, 15)))) }
    @Test fun serializes_def_range() { assertEquals("def0-14",     SearchSerializer.serializeFilter(node(FilterDefs.DEF, FilterValue.NumericRange(0, 14)))) }
    @Test fun serializes_sta_max()   { assertEquals("sta-10",      SearchSerializer.serializeFilter(node(FilterDefs.STA, FilterValue.NumericRange(null, 10)))) }
    @Test fun serializes_level()     { assertEquals("level40",     SearchSerializer.serializeFilter(node(FilterDefs.LEVEL, FilterValue.NumericRange(40, 40)))) }
    @Test fun serializes_age_range() { assertEquals("age1-7",      SearchSerializer.serializeFilter(node(FilterDefs.AGE, FilterValue.NumericRange(1, 7)))) }
    @Test fun serializes_year()      { assertEquals("year2024",    SearchSerializer.serializeFilter(node(FilterDefs.YEAR, FilterValue.NumericRange(2024, 2024)))) }

    // ── Stars / enums ─────────────────────────────────────────────────────────

    @Test fun serializes_4star() { assertEquals("4*", SearchSerializer.serializeFilter(node(FilterDefs.IV_STARS, FilterValue.EnumVal("4*")))) }
    @Test fun serializes_3star() { assertEquals("3*", SearchSerializer.serializeFilter(node(FilterDefs.IV_STARS, FilterValue.EnumVal("3*")))) }

    @Test fun serializes_type_fire()  { assertEquals("fire",   SearchSerializer.serializeFilter(node(FilterDefs.TYPE,       FilterValue.EnumVal("fire")))) }
    @Test fun serializes_gen1()       { assertEquals("gen1",   SearchSerializer.serializeFilter(node(FilterDefs.GENERATION, FilterValue.EnumVal("gen1")))) }
    @Test fun serializes_male()       { assertEquals("male",   SearchSerializer.serializeFilter(node(FilterDefs.GENDER,     FilterValue.EnumVal("male")))) }
    @Test fun serializes_2km()        { assertEquals("2km",    SearchSerializer.serializeFilter(node(FilterDefs.EGG_KM,     FilterValue.EnumVal("2km")))) }
    @Test fun serializes_buddy0()     { assertEquals("buddy0", SearchSerializer.serializeFilter(node(FilterDefs.BUDDY_LEVEL,FilterValue.EnumVal("buddy0")))) }
    @Test fun serializes_buddy4()     { assertEquals("buddy4", SearchSerializer.serializeFilter(node(FilterDefs.BUDDY_LEVEL,FilterValue.EnumVal("buddy4")))) }

    // ── Name / move / tag / Pokédex ───────────────────────────────────────────

    @Test fun serializes_name()     { assertEquals("pikachu", SearchSerializer.serializeFilter(node(FilterDefs.NAME, FilterValue.TextVal("pikachu")))) }
    @Test fun serializes_move()     { assertEquals("@ember",  SearchSerializer.serializeFilter(node(FilterDefs.MOVE, FilterValue.TextVal("ember")))) }
    @Test fun serializes_legacy()   { assertEquals("@legacy", SearchSerializer.serializeFilter(node(FilterDefs.LEGACY_MOVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_elite()    { assertEquals("@elite",  SearchSerializer.serializeFilter(node(FilterDefs.ELITE_MOVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_special()  { assertEquals("@special",SearchSerializer.serializeFilter(node(FilterDefs.SPECIAL_MOVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_purified_move() { assertEquals("@purified", SearchSerializer.serializeFilter(node(FilterDefs.PURIFIED_MOVE, FilterValue.BooleanPresent))) }
    @Test fun serializes_tag()      { assertEquals("#mytag",  SearchSerializer.serializeFilter(node(FilterDefs.TAG, FilterValue.TextVal("mytag")))) }

    @Test fun serializes_pokedex_single() {
        assertEquals("#151", SearchSerializer.serializeFilter(node(FilterDefs.POKEDEX, FilterValue.PokedexRange(151, null))))
    }
    @Test fun serializes_pokedex_range() {
        assertEquals("#1-151", SearchSerializer.serializeFilter(node(FilterDefs.POKEDEX, FilterValue.PokedexRange(1, 151))))
    }

    // ── Group serialization ───────────────────────────────────────────────────

    @Test fun serializes_and_group() {
        val r = root(LogicOperator.AND,
            node(FilterDefs.SHINY, FilterValue.BooleanPresent),
            node(FilterDefs.LUCKY, FilterValue.BooleanPresent)
        )
        assertEquals("shiny&lucky", SearchSerializer.serializeRoot(r))
    }

    @Test fun serializes_or_group() {
        val r = root(LogicOperator.OR,
            node(FilterDefs.SHINY, FilterValue.BooleanPresent),
            node(FilterDefs.LUCKY, FilterValue.BooleanPresent)
        )
        assertEquals("shiny,lucky", SearchSerializer.serializeRoot(r))
    }

    @Test fun serializes_nested_group() {
        val inner = group(LogicOperator.OR,
            node(FilterDefs.SHINY, FilterValue.BooleanPresent),
            node(FilterDefs.LUCKY, FilterValue.BooleanPresent)
        )
        val r = root(LogicOperator.AND,
            inner,
            node(FilterDefs.CP, FilterValue.NumericRange(null, 1500))
        )
        assertEquals("(shiny,lucky)&cp-1500", SearchSerializer.serializeRoot(r))
    }

    @Test fun serializes_negated_group() {
        val inner = group(LogicOperator.OR,
            node(FilterDefs.SHINY, FilterValue.BooleanPresent),
            node(FilterDefs.LUCKY, FilterValue.BooleanPresent),
            negated = true
        )
        val r = root(LogicOperator.AND, inner)
        assertEquals("!(shiny,lucky)", SearchSerializer.serializeRoot(r))
    }

    @Test fun serializes_empty_root() {
        val r = root(LogicOperator.OR)
        assertEquals("", SearchSerializer.serializeRoot(r))
    }

    @Test fun serializes_single_filter_root() {
        val r = root(LogicOperator.OR, node(FilterDefs.SHINY, FilterValue.BooleanPresent))
        assertEquals("shiny", SearchSerializer.serializeRoot(r))
    }

    // ── Round-trip tests ──────────────────────────────────────────────────────

    @Test fun roundTrip_shiny()           { assertEquals("shiny",           roundTrip("shiny")) }
    @Test fun roundTrip_not_shiny()       { assertEquals("!shiny",          roundTrip("!shiny")) }
    @Test fun roundTrip_cp_range()        { assertEquals("cp1000-1500",     roundTrip("cp1000-1500")) }
    @Test fun roundTrip_cp_max()          { assertEquals("cp-1500",         roundTrip("cp-1500")) }
    @Test fun roundTrip_stars()           { assertEquals("4*",              roundTrip("4*")) }
    @Test fun roundTrip_move()            { assertEquals("@ember",          roundTrip("@ember")) }
    @Test fun roundTrip_legacy()          { assertEquals("@legacy",         roundTrip("@legacy")) }
    @Test fun roundTrip_tag()             { assertEquals("#mytag",          roundTrip("#mytag")) }
    @Test fun roundTrip_pokedex()         { assertEquals("#1-151",          roundTrip("#1-151")) }
    @Test fun roundTrip_and_expression()  { assertEquals("shiny&lucky",     roundTrip("shiny&lucky")) }
    @Test fun roundTrip_or_expression()   { assertEquals("shiny,lucky",     roundTrip("shiny,lucky")) }
    @Test fun roundTrip_type_fire()       { assertEquals("fire",            roundTrip("fire")) }
    @Test fun roundTrip_generation()      { assertEquals("gen1",            roundTrip("gen1")) }
    @Test fun roundTrip_buddy_level()     { assertEquals("buddy4",          roundTrip("buddy4")) }
    @Test fun roundTrip_egg_distance()    { assertEquals("10km",            roundTrip("10km")) }
    @Test fun roundTrip_nested_group()    { assertEquals("(shiny,lucky)&cp-1500", roundTrip("(shiny,lucky)&cp-1500")) }
    @Test fun roundTrip_negated_group()   { assertEquals("!(shiny,lucky)&cp-1500", roundTrip("!(shiny,lucky)&cp-1500")) }

    @Test fun roundTrip_complex() {
        // (1*&4*),(!(3*)&shiny)
        val result = roundTrip("(1*&4*),(!(3*)&shiny)")
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    // ── numericRangeString helper ─────────────────────────────────────────────

    @Test fun numericRangeString_exact()   { assertEquals("cp1500",      SearchSerializer.numericRangeString("cp", FilterValue.NumericRange(1500, 1500))) }
    @Test fun numericRangeString_maxOnly() { assertEquals("cp-1500",     SearchSerializer.numericRangeString("cp", FilterValue.NumericRange(null, 1500))) }
    @Test fun numericRangeString_minOnly() { assertEquals("cp1500-",     SearchSerializer.numericRangeString("cp", FilterValue.NumericRange(1500, null))) }
    @Test fun numericRangeString_range()   { assertEquals("cp1000-1500", SearchSerializer.numericRangeString("cp", FilterValue.NumericRange(1000, 1500))) }
    @Test fun numericRangeString_empty()   { assertEquals("cp",          SearchSerializer.numericRangeString("cp", FilterValue.NumericRange(null, null))) }

    // ── filterValueSummary helper ─────────────────────────────────────────────

    @Test fun summary_boolean() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.SHINY, FilterValue.BooleanPresent)
        assertEquals("Shiny", s)
    }

    @Test fun summary_text() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.MOVE, FilterValue.TextVal("ember"))
        assertTrue(s.contains("ember"))
    }

    @Test fun summary_numericRange_exact() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.CP, FilterValue.NumericRange(1500, 1500))
        assertTrue(s.contains("1500"))
    }

    @Test fun summary_numericRange_range() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.CP, FilterValue.NumericRange(1000, 1500))
        assertTrue(s.contains("1000") && s.contains("1500"))
    }

    @Test fun summary_enum() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.TYPE, FilterValue.EnumVal("fire"))
        assertTrue(s.lowercase().contains("fire"))
    }

    @Test fun summary_pokedex_single() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.POKEDEX, FilterValue.PokedexRange(151, null))
        assertTrue(s.contains("151"))
    }

    @Test fun summary_pokedex_range() {
        val s = SearchSerializer.filterValueSummary(FilterDefs.POKEDEX, FilterValue.PokedexRange(1, 151))
        assertTrue(s.contains("1") && s.contains("151"))
    }
}
