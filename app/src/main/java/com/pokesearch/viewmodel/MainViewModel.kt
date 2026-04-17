package com.pokesearch.viewmodel

import androidx.lifecycle.ViewModel
import com.pokesearch.model.*
import com.pokesearch.parser.SearchParser
import com.pokesearch.parser.SearchSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class MainUiState(
    val root: QueryNode.GroupNode = QueryNode.GroupNode(
        id = NodeId.ROOT,
        operator = LogicOperator.OR,
        children = emptyList()
    ),
    val searchString: String = "",
    /** Node currently being edited in the filter editor sheet (null = sheet hidden). */
    val editingNode: QueryNode.FilterNode? = null,
    /** Parent group id for an add-filter action (null = not adding). */
    val addingToGroupId: NodeId? = null,
    /** True when the user is adding a brand-new filter (vs. editing existing). */
    val isAddingNew: Boolean = false,
    /** Snackbar / toast message. */
    val message: String? = null,
    /** User-chosen theme override. */
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class MainViewModel : ViewModel() {

    private val parser = SearchParser()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // ── Query tree mutations ──────────────────────────────────────────────────

    fun addFilter(parentId: NodeId, def: FilterDef, value: FilterValue, negated: Boolean) {
        val node = QueryNode.FilterNode(NodeId.generate(), def, value, negated)
        updateRoot { it.addChild(parentId, node) }
    }

    fun updateFilter(nodeId: NodeId, def: FilterDef, value: FilterValue, negated: Boolean) {
        updateRoot { root ->
            root.replaceNode(nodeId) { old ->
                (old as QueryNode.FilterNode).copy(filterDef = def, value = value, negated = negated)
            }
        }
    }

    fun addGroup(parentId: NodeId) {
        val group = QueryNode.GroupNode(
            id = NodeId.generate(),
            operator = LogicOperator.AND,
            children = emptyList()
        )
        updateRoot { it.addChild(parentId, group) }
    }

    fun removeNode(nodeId: NodeId) {
        updateRoot { it.removeNode(nodeId) }
    }

    fun toggleNegation(nodeId: NodeId) {
        updateRoot { root ->
            root.replaceNode(nodeId) { node ->
                when (node) {
                    is QueryNode.FilterNode -> node.copy(negated = !node.negated)
                    is QueryNode.GroupNode  -> node.copy(negated = !node.negated)
                }
            }
        }
    }

    /**
     * Moves the node with [nodeId] to [toIndex] within its parent group.
     * Silently ignored if the node isn't found or the index is unchanged.
     */
    fun reorderNode(nodeId: NodeId, toIndex: Int) {
        updateRoot { root ->
            reorderInTree(root, nodeId, toIndex)
        }
    }

    private fun reorderInTree(
        group: QueryNode.GroupNode,
        nodeId: NodeId,
        toIndex: Int
    ): QueryNode.GroupNode {
        val idx = group.children.indexOfFirst { it.id == nodeId }
        if (idx >= 0) {
            val clamped = toIndex.coerceIn(0, group.children.size - 1)
            if (clamped == idx) return group
            val list = group.children.toMutableList()
            val item = list.removeAt(idx)
            list.add(clamped, item)
            return group.copy(children = list)
        }
        return group.copy(children = group.children.map { child ->
            if (child is QueryNode.GroupNode) reorderInTree(child, nodeId, toIndex)
            else child
        })
    }

    fun changeGroupOperator(nodeId: NodeId, operator: LogicOperator) {
        updateRoot { root ->
            if (root.id == nodeId) {
                root.copy(operator = operator)
            } else {
                root.replaceNode(nodeId) { node ->
                    (node as QueryNode.GroupNode).copy(operator = operator)
                }
            }
        }
    }

    fun cycleTheme() {
        _uiState.update { state ->
            val next = when (state.themeMode) {
                ThemeMode.SYSTEM -> ThemeMode.DARK
                ThemeMode.DARK   -> ThemeMode.LIGHT
                ThemeMode.LIGHT  -> ThemeMode.SYSTEM
            }
            state.copy(themeMode = next)
        }
    }

    fun clearAll() {
        _uiState.update { state ->
            val empty = QueryNode.GroupNode(NodeId.ROOT, LogicOperator.OR, emptyList())
            state.copy(root = empty, searchString = "", editingNode = null, addingToGroupId = null)
        }
    }

    // ── Parse from pasted string ──────────────────────────────────────────────

    fun parseSearchString(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            clearAll()
            return
        }
        try {
            val parsed = parser.parse(trimmed)
            // Preserve ROOT id so UI doesn't flash
            val withRootId = parsed.copy(id = NodeId.ROOT)
            _uiState.update { state ->
                state.copy(
                    root = withRootId,
                    searchString = SearchSerializer.serializeRoot(withRootId),
                    message = "Parsed ${countFilters(withRootId)} filter(s)"
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "Parse error: ${e.message}") }
        }
    }

    // ── Filter editor sheet ───────────────────────────────────────────────────

    fun openAddFilterSheet(parentGroupId: NodeId) {
        _uiState.update { it.copy(addingToGroupId = parentGroupId, isAddingNew = true, editingNode = null) }
    }

    fun openEditFilterSheet(node: QueryNode.FilterNode) {
        _uiState.update { it.copy(editingNode = node, isAddingNew = false, addingToGroupId = null) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(editingNode = null, addingToGroupId = null) }
    }

    fun confirmFilter(def: FilterDef, value: FilterValue, negated: Boolean) {
        val state = _uiState.value
        when {
            state.isAddingNew && state.addingToGroupId != null ->
                addFilter(state.addingToGroupId, def, value, negated)
            !state.isAddingNew && state.editingNode != null ->
                updateFilter(state.editingNode.id, def, value, negated)
        }
        dismissSheet()
    }

    // ── Test helpers ─────────────────────────────────────────────────────────

    /** Directly replace the root node. Used in unit tests. */
    fun loadRoot(root: QueryNode.GroupNode) {
        _uiState.update { state ->
            state.copy(root = root, searchString = SearchSerializer.serializeRoot(root))
        }
    }

    // ── Message handling ──────────────────────────────────────────────────────

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun updateRoot(transform: (QueryNode.GroupNode) -> QueryNode.GroupNode) {
        _uiState.update { state ->
            val newRoot = transform(state.root)
            state.copy(root = newRoot, searchString = SearchSerializer.serializeRoot(newRoot))
        }
    }

    private fun countFilters(node: QueryNode): Int = when (node) {
        is QueryNode.FilterNode -> 1
        is QueryNode.GroupNode  -> node.children.sumOf { countFilters(it) }
    }
}
