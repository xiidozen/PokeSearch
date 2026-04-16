package com.pokesearch

import com.pokesearch.model.*
import com.pokesearch.ui.components.calculateTargetIndex
import com.pokesearch.ui.components.opposite
import com.pokesearch.viewmodel.MainViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReorderTest {

    // ── LogicOperator.opposite() ──────────────────────────────────────────────

    @Test fun andOpposite_isOr() {
        assertEquals(LogicOperator.OR, LogicOperator.AND.opposite())
    }

    @Test fun orOpposite_isAnd() {
        assertEquals(LogicOperator.AND, LogicOperator.OR.opposite())
    }

    // ── calculateTargetIndex ──────────────────────────────────────────────────

    private fun heights(vararg h: Int): Map<String, Int> =
        h.mapIndexed { i, v -> "id$i" to v }.toMap()

    private fun ids(n: Int): List<String> = (0 until n).map { "id$it" }

    @Test fun targetIndex_noMovement() {
        // Dragging item 0 with 0 offset should stay at 0
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 0f,
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(0, result)
    }

    @Test fun targetIndex_moveDown_oneSlot() {
        // Dragging item 0 down by more than one item height → moves to index 1
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 80f,   // > 72dp item height
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(1, result)
    }

    @Test fun targetIndex_moveDown_twoSlots() {
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 160f,  // > 2 * 72dp
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(2, result)
    }

    @Test fun targetIndex_moveUp_oneSlot() {
        val result = calculateTargetIndex(
            originalIdx = 2,
            dragOffsetY = -80f,  // move up
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(1, result)
    }

    @Test fun targetIndex_clampsToZero() {
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = -500f,  // way above the top
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(0, result)
    }

    @Test fun targetIndex_clampsToMax() {
        val result = calculateTargetIndex(
            originalIdx = 2,
            dragOffsetY = 500f,  // way below the bottom
            childIds = ids(3),
            itemHeights = heights(72, 72, 72)
        )
        assertEquals(2, result)
    }

    @Test fun targetIndex_singleItem_alwaysZero() {
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 999f,
            childIds = ids(1),
            itemHeights = heights(72)
        )
        assertEquals(0, result)
    }

    @Test fun targetIndex_variableHeights_movesCorrectly() {
        // Item heights: 100, 50, 80
        // Dragging item 0 (height 100) down 120px:
        // tops: 0, 100, 150; centers: 50, 125, 190
        // dragged center = 0 + 120 + 50 = 170 → closest to 190 (idx 2)
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 120f,
            childIds = ids(3),
            itemHeights = mapOf("id0" to 100, "id1" to 50, "id2" to 80)
        )
        assertEquals(2, result)
    }

    @Test fun targetIndex_missingHeightUsesDefault() {
        // Items with missing height entries should still work (72px default)
        val result = calculateTargetIndex(
            originalIdx = 0,
            dragOffsetY = 80f,
            childIds = listOf("a", "b", "c"),
            itemHeights = emptyMap()  // all default 72
        )
        assertEquals(1, result)
    }

    // ── ViewModel.reorderNode ─────────────────────────────────────────────────

    private lateinit var vm: MainViewModel

    private fun makeFilter(name: String): QueryNode.FilterNode =
        QueryNode.FilterNode(NodeId(name), FilterDefs.SHINY, FilterValue.BooleanPresent)

    @Before fun setUpVm() { vm = MainViewModel() }

    private fun buildRoot(vararg nodeNames: String): QueryNode.GroupNode {
        val children = nodeNames.map { makeFilter(it) }
        return QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, children)
    }

    @Test fun reorderNode_movesFirstToLast() {
        // Three filters [A, B, C] → reorder A to index 2 → [B, C, A]
        val filters = listOf("A", "B", "C").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, filters)
        // Use the private reorderInTree via the ViewModel
        vm.loadRoot(root)
        vm.reorderNode(NodeId("A"), 2)
        val newChildren = vm.uiState.value.root.children
        assertEquals("B", (newChildren[0] as QueryNode.FilterNode).id.value)
        assertEquals("C", (newChildren[1] as QueryNode.FilterNode).id.value)
        assertEquals("A", (newChildren[2] as QueryNode.FilterNode).id.value)
    }

    @Test fun reorderNode_movesLastToFirst() {
        val filters = listOf("A", "B", "C").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, filters)
        vm.loadRoot(root)
        vm.reorderNode(NodeId("C"), 0)
        val newChildren = vm.uiState.value.root.children
        assertEquals("C", (newChildren[0] as QueryNode.FilterNode).id.value)
        assertEquals("A", (newChildren[1] as QueryNode.FilterNode).id.value)
        assertEquals("B", (newChildren[2] as QueryNode.FilterNode).id.value)
    }

    @Test fun reorderNode_sameIndex_noChange() {
        val filters = listOf("A", "B", "C").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, filters)
        vm.loadRoot(root)
        vm.reorderNode(NodeId("B"), 1)
        val newChildren = vm.uiState.value.root.children
        assertEquals("A", (newChildren[0] as QueryNode.FilterNode).id.value)
        assertEquals("B", (newChildren[1] as QueryNode.FilterNode).id.value)
        assertEquals("C", (newChildren[2] as QueryNode.FilterNode).id.value)
    }

    @Test fun reorderNode_clampsToValidRange() {
        val filters = listOf("A", "B").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, filters)
        vm.loadRoot(root)
        vm.reorderNode(NodeId("A"), 99)  // out of range → clamped to 1
        val newChildren = vm.uiState.value.root.children
        assertEquals("B", (newChildren[0] as QueryNode.FilterNode).id.value)
        assertEquals("A", (newChildren[1] as QueryNode.FilterNode).id.value)
    }

    @Test fun reorderNode_inNestedGroup() {
        // Root → OR[inner_group] where inner_group = AND[A, B, C]
        val a = makeFilter("A")
        val b = makeFilter("B")
        val c = makeFilter("C")
        val inner = QueryNode.GroupNode(NodeId("inner"), LogicOperator.AND, listOf(a, b, c))
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, listOf(inner))
        vm.loadRoot(root)
        vm.reorderNode(NodeId("C"), 0)
        val innerResult = vm.uiState.value.root.children[0] as QueryNode.GroupNode
        assertEquals("C", (innerResult.children[0] as QueryNode.FilterNode).id.value)
        assertEquals("A", (innerResult.children[1] as QueryNode.FilterNode).id.value)
        assertEquals("B", (innerResult.children[2] as QueryNode.FilterNode).id.value)
    }

    @Test fun reorderNode_unknownId_noChange() {
        val filters = listOf("A", "B").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, filters)
        vm.loadRoot(root)
        val beforeString = vm.uiState.value.searchString
        vm.reorderNode(NodeId("nonexistent"), 0)
        assertEquals(beforeString, vm.uiState.value.searchString)
    }

    // ── changeGroupOperator (AND/OR toggle) ───────────────────────────────────

    @Test fun changeGroupOperator_rootAndToOr() {
        val filters = listOf("A", "B").map { makeFilter(it) }
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.AND, filters)
        vm.loadRoot(root)
        vm.changeGroupOperator(NodeId.ROOT, LogicOperator.OR)
        assertEquals(LogicOperator.OR, vm.uiState.value.root.operator)
    }

    @Test fun changeGroupOperator_nestedGroup() {
        val inner = QueryNode.GroupNode(NodeId("inner"), LogicOperator.AND, emptyList())
        val root  = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, listOf(inner))
        vm.loadRoot(root)
        vm.changeGroupOperator(NodeId("inner"), LogicOperator.OR)
        val innerResult = vm.uiState.value.root.children[0] as QueryNode.GroupNode
        assertEquals(LogicOperator.OR, innerResult.operator)
    }

    @Test fun changeGroupOperator_affectsSearchString() {
        val a = makeFilter("A")
        val b = makeFilter("B")
        val root = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.AND, listOf(a, b))
        vm.loadRoot(root)
        vm.changeGroupOperator(NodeId.ROOT, LogicOperator.OR)
        // AND separator is &, OR separator is ,
        assertTrue(vm.uiState.value.searchString.contains(","))
        assertFalse(vm.uiState.value.searchString.contains("&"))
    }
}
