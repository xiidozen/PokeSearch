package com.pokesearch

import com.pokesearch.model.*
import org.junit.Assert.*
import org.junit.Test

class QueryNodeTest {

    private fun makeFilter(id: String = "shiny") =
        QueryNode.FilterNode(NodeId.generate(), FilterDefs.SHINY, FilterValue.BooleanPresent)

    private fun makeGroup(op: LogicOperator = LogicOperator.AND, vararg children: QueryNode) =
        QueryNode.GroupNode(NodeId.generate(), op, children.toList())

    // ── addChild ──────────────────────────────────────────────────────────────

    @Test fun addChild_toRoot() {
        val root = makeGroup(LogicOperator.OR)
        val filter = makeFilter()
        val result = root.addChild(root.id, filter)
        assertEquals(1, result.children.size)
        assertEquals(filter.id, result.children[0].id)
    }

    @Test fun addChild_toNestedGroup() {
        val inner = makeGroup(LogicOperator.AND)
        val root  = makeGroup(LogicOperator.OR, inner)
        val filter = makeFilter()
        val result = root.addChild(inner.id, filter)
        val innerResult = result.children[0] as QueryNode.GroupNode
        assertEquals(1, innerResult.children.size)
    }

    @Test fun addChild_doesNotMutateOriginal() {
        val root = makeGroup(LogicOperator.OR)
        val filter = makeFilter()
        root.addChild(root.id, filter)
        assertEquals(0, root.children.size) // original unchanged
    }

    // ── removeNode ────────────────────────────────────────────────────────────

    @Test fun removeNode_directChild() {
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, filter)
        val result = root.removeNode(filter.id)
        assertEquals(0, result.children.size)
    }

    @Test fun removeNode_nestedChild() {
        val filter = makeFilter()
        val inner = makeGroup(LogicOperator.AND, filter)
        val root = makeGroup(LogicOperator.OR, inner)
        val result = root.removeNode(filter.id)
        val innerResult = result.children[0] as QueryNode.GroupNode
        assertEquals(0, innerResult.children.size)
    }

    @Test fun removeNode_nonExistent_noChange() {
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, filter)
        val fakeId = NodeId("nonexistent")
        val result = root.removeNode(fakeId)
        assertEquals(1, result.children.size)
    }

    @Test fun removeNode_group() {
        val inner = makeGroup(LogicOperator.AND)
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, inner, filter)
        val result = root.removeNode(inner.id)
        assertEquals(1, result.children.size)
        assertEquals(filter.id, result.children[0].id)
    }

    // ── replaceNode ───────────────────────────────────────────────────────────

    @Test fun replaceNode_directChild() {
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, filter)
        val newFilter = makeFilter()
        val result = root.replaceNode(filter.id) { newFilter }
        assertEquals(newFilter.id, (result.children[0] as QueryNode.FilterNode).id)
    }

    @Test fun replaceNode_toggleNegation() {
        val filter = QueryNode.FilterNode(NodeId.generate(), FilterDefs.SHINY, FilterValue.BooleanPresent, negated = false)
        val root = makeGroup(LogicOperator.OR, filter)
        val result = root.replaceNode(filter.id) { node ->
            (node as QueryNode.FilterNode).copy(negated = !node.negated)
        }
        assertTrue((result.children[0] as QueryNode.FilterNode).negated)
    }

    @Test fun replaceNode_nestedChild() {
        val filter = makeFilter()
        val inner = makeGroup(LogicOperator.AND, filter)
        val root = makeGroup(LogicOperator.OR, inner)
        val replacement = makeFilter()
        val result = root.replaceNode(filter.id) { replacement }
        val innerResult = result.children[0] as QueryNode.GroupNode
        assertEquals(replacement.id, innerResult.children[0].id)
    }

    // ── findNode ──────────────────────────────────────────────────────────────

    @Test fun findNode_self() {
        val root = makeGroup(LogicOperator.OR)
        assertEquals(root, root.findNode(root.id))
    }

    @Test fun findNode_directChild() {
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, filter)
        assertEquals(filter, root.findNode(filter.id))
    }

    @Test fun findNode_nestedChild() {
        val deepFilter = makeFilter()
        val inner = makeGroup(LogicOperator.AND, deepFilter)
        val root = makeGroup(LogicOperator.OR, inner)
        assertEquals(deepFilter, root.findNode(deepFilter.id))
    }

    @Test fun findNode_notFound_returnsNull() {
        val root = makeGroup(LogicOperator.OR)
        assertNull(root.findNode(NodeId("missing")))
    }

    // ── Immutability ──────────────────────────────────────────────────────────

    @Test fun operations_areImmutable() {
        val filter = makeFilter()
        val root = makeGroup(LogicOperator.OR, filter)

        // All operations return new instances
        val after1 = root.addChild(root.id, makeFilter())
        val after2 = root.removeNode(filter.id)
        val after3 = root.replaceNode(filter.id) { makeFilter() }

        // Original unchanged
        assertEquals(1, root.children.size)
        assertEquals(2, after1.children.size)
        assertEquals(0, after2.children.size)
        assertEquals(1, after3.children.size)
    }
}
