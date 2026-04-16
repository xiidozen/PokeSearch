package com.pokesearch

import com.pokesearch.model.*
import org.junit.Assert.*
import org.junit.Test

class FilterDefsTest {

    @Test fun allFilters_haveUniqueIds() {
        val ids = FilterDefs.ALL.map { it.id }
        assertEquals("Duplicate filter ids found", ids.size, ids.distinct().size)
    }

    @Test fun allEnumOptions_haveNonBlankKeys() {
        FilterDefs.ALL.filter { it.valueType == ValueType.ENUM_SINGLE }.forEach { def ->
            def.enumOptions.forEach { opt ->
                assertTrue("Blank key in ${def.id}", opt.key.isNotBlank())
                assertTrue("Blank displayName in ${def.id}", opt.displayName.isNotBlank())
            }
        }
    }

    @Test fun allEnumDefs_haveAtLeastOneOption() {
        FilterDefs.ALL.filter { it.valueType == ValueType.ENUM_SINGLE }.forEach { def ->
            assertTrue("${def.id} has no enum options", def.enumOptions.isNotEmpty())
        }
    }

    @Test fun booleanByToken_containsShiny() {
        assertNotNull(FilterDefs.BOOLEAN_BY_TOKEN["shiny"])
    }

    @Test fun booleanByToken_doesNotContainAtPrefixed() {
        assertNull("@legacy should not be in BOOLEAN_BY_TOKEN", FilterDefs.BOOLEAN_BY_TOKEN["@legacy"])
        assertNull("@elite should not be in BOOLEAN_BY_TOKEN",  FilterDefs.BOOLEAN_BY_TOKEN["@elite"])
    }

    @Test fun enumByOption_containsTypes() {
        listOf("fire","water","grass","electric","ice","fighting","poison","ground",
            "flying","psychic","bug","rock","ghost","dragon","dark","steel","fairy","normal"
        ).forEach { type ->
            assertNotNull("type '$type' missing from ENUM_BY_OPTION", FilterDefs.ENUM_BY_OPTION[type])
            assertEquals("type", FilterDefs.ENUM_BY_OPTION[type]?.id)
        }
    }

    @Test fun enumByOption_containsGenerations() {
        for (i in 1..9) {
            assertNotNull("gen$i missing", FilterDefs.ENUM_BY_OPTION["gen$i"])
        }
    }

    @Test fun enumByOption_containsBuddyLevels() {
        for (i in 0..4) {
            assertNotNull("buddy$i missing", FilterDefs.ENUM_BY_OPTION["buddy$i"])
        }
    }

    @Test fun enumByOption_containsEggDistances() {
        listOf("2km","5km","7km","10km","12km").forEach { km ->
            assertNotNull("$km missing", FilterDefs.ENUM_BY_OPTION[km])
        }
    }

    @Test fun enumByOption_containsGenders() {
        assertNotNull(FilterDefs.ENUM_BY_OPTION["male"])
        assertNotNull(FilterDefs.ENUM_BY_OPTION["female"])
    }

    @Test fun enumByOption_containsStarRatings() {
        for (i in 0..4) {
            assertNotNull("$i* missing", FilterDefs.ENUM_BY_OPTION["$i*"])
        }
    }

    @Test fun byCategory_coversAllFilters() {
        val categoryCount = FilterDefs.BY_CATEGORY.values.sumOf { it.size }
        assertEquals(FilterDefs.ALL.size, categoryCount)
    }

    @Test fun findById_returnsCorrectFilter() {
        assertEquals(FilterDefs.SHINY, FilterDefs.findById("shiny"))
        assertEquals(FilterDefs.CP,    FilterDefs.findById("cp"))
        assertEquals(FilterDefs.MOVE,  FilterDefs.findById("move"))
    }

    @Test fun findById_unknownId_returnsNull() {
        assertNull(FilterDefs.findById("notarealfilter"))
    }

    @Test fun allCategories_haveAtLeastOneFilter() {
        FilterCategory.entries.forEach { cat ->
            val filters = FilterDefs.BY_CATEGORY[cat] ?: emptyList()
            assertTrue("No filters in category ${cat.displayName}", filters.isNotEmpty())
        }
    }
}
