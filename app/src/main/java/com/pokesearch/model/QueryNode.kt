package com.pokesearch.model

import java.util.UUID

/** Stable identity for a node in the query tree. */
data class NodeId(val value: String) {
    companion object {
        fun generate(): NodeId = NodeId(UUID.randomUUID().toString().takeLast(8))
        val ROOT = NodeId("root")
    }
}

/**
 * A node in the query tree. Either a leaf [FilterNode] or an interior [GroupNode].
 * All nodes are immutable; mutations produce new tree copies via [GroupNode.replaceNode],
 * [GroupNode.addChild], and [GroupNode.removeNode].
 */
sealed class QueryNode {
    abstract val id: NodeId
    abstract val negated: Boolean

    /** A single search filter (e.g. `shiny`, `cp-1500`, `@legacy`). */
    data class FilterNode(
        override val id: NodeId,
        val filterDef: FilterDef,
        val value: FilterValue,
        override val negated: Boolean = false
    ) : QueryNode()

    /** A logical group that combines its children with AND or OR. */
    data class GroupNode(
        override val id: NodeId,
        val operator: LogicOperator,
        val children: List<QueryNode>,
        override val negated: Boolean = false
    ) : QueryNode() {

        /** Returns a new tree with [child] appended under the group whose id == [parentId]. */
        fun addChild(parentId: NodeId, child: QueryNode): GroupNode =
            if (id == parentId) copy(children = children + child)
            else copy(children = children.map {
                if (it is GroupNode) it.addChild(parentId, child) else it
            })

        /** Returns a new tree with the node matching [targetId] removed. */
        fun removeNode(targetId: NodeId): GroupNode =
            copy(children = children
                .filter { it.id != targetId }
                .map { if (it is GroupNode) it.removeNode(targetId) else it })

        /**
         * Returns a new tree where the node whose id == [targetId] is replaced by
         * the result of applying [transform] to it.
         */
        fun replaceNode(targetId: NodeId, transform: (QueryNode) -> QueryNode): GroupNode =
            copy(children = children.map { child ->
                when {
                    child.id == targetId -> transform(child)
                    child is GroupNode -> child.replaceNode(targetId, transform)
                    else -> child
                }
            })

        /** Finds any node in the tree by id (including this node itself). */
        fun findNode(targetId: NodeId): QueryNode? {
            if (id == targetId) return this
            for (child in children) {
                val found: QueryNode? = when (child) {
                    is GroupNode -> child.findNode(targetId)
                    else -> if (child.id == targetId) child else null
                }
                if (found != null) return found
            }
            return null
        }
    }
}
