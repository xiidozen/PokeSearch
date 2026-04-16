package com.pokesearch.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pokesearch.model.*
import com.pokesearch.parser.SearchSerializer
import kotlin.math.abs

/**
 * Recursively renders the entire query tree.
 *
 * Each child item has a drag handle (≡). Long-press + drag the handle to reorder items
 * within their parent group. The AND/OR separator between items is a tappable chip
 * that toggles the group's operator when tapped.
 */
@Composable
fun QueryTreeView(
    group: QueryNode.GroupNode,
    isRoot: Boolean,
    depth: Int = 0,
    onAddFilter: (NodeId) -> Unit,
    onAddGroup: (NodeId) -> Unit,
    onRemove: (NodeId) -> Unit,
    onToggleNegation: (NodeId) -> Unit,
    onChangeOperator: (NodeId, LogicOperator) -> Unit,
    onEditFilter: (QueryNode.FilterNode) -> Unit,
    onReorder: (nodeId: NodeId, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indentStart: Dp = if (!isRoot) 12.dp else 0.dp
    val borderColor = groupBorderColor(depth)

    // ── Drag state ──────────────────────────────────────────────────────────
    var draggedId by remember { mutableStateOf<NodeId?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    // Track pixel height of each child so we can compute the target drop index
    val itemHeights = remember { mutableStateMapOf<String, Int>() }

    Column(
        modifier = modifier
            .padding(start = indentStart)
            .then(
                if (!isRoot) Modifier
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                else Modifier
            )
            .background(groupBackgroundColor(depth, isRoot))
            .padding(if (!isRoot) 10.dp else 0.dp)
            .animateContentSize()
    ) {
        // ── Group header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // NOT badge (non-root only)
            if (!isRoot) {
                NotToggleButton(
                    negated = group.negated,
                    onClick = { onToggleNegation(group.id) }
                )
            }

            // AND / OR segmented toggle
            AndOrToggle(
                selected = group.operator,
                onChange = { onChangeOperator(group.id, it) }
            )

            Spacer(Modifier.weight(1f))

            // Delete group (non-root only)
            if (!isRoot) {
                IconButton(
                    onClick = { onRemove(group.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove group",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ── Children ──────────────────────────────────────────────────────────
        if (group.children.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            group.children.forEachIndexed { idx, child ->
                val isDragged = child.id == draggedId

                // Compute where this item will land while a drag is active
                val targetIdx = if (draggedId != null) {
                    calculateTargetIndex(
                        originalIdx  = group.children.indexOfFirst { it.id == draggedId },
                        dragOffsetY  = dragOffsetY,
                        childIds     = group.children.map { it.id.value },
                        itemHeights  = itemHeights
                    )
                } else -1

                // ── Drop indicator above this slot ──────────────────────────
                if (draggedId != null && !isDragged && targetIdx == idx) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // ── Item row (handle + content) ─────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragged) 10f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffsetY else 0f
                            shadowElevation = if (isDragged) 8.dp.toPx() else 0f
                            alpha = if (isDragged) 0.9f else 1f
                        }
                        .onSizeChanged { size ->
                            itemHeights[child.id.value] = size.height
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ── Drag handle ─────────────────────────────────────────
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(4.dp)
                            .pointerInput(child.id, group.children.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedId = child.id
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        val orig = group.children.indexOfFirst { it.id == draggedId }
                                        val target = calculateTargetIndex(
                                            originalIdx = orig,
                                            dragOffsetY = dragOffsetY,
                                            childIds    = group.children.map { it.id.value },
                                            itemHeights = itemHeights
                                        )
                                        if (target != orig) onReorder(child.id, target)
                                        draggedId = null
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggedId = null
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                    )

                    // ── Child content ───────────────────────────────────────
                    Box(modifier = Modifier.weight(1f)) {
                        when (child) {
                            is QueryNode.GroupNode ->
                                QueryTreeView(
                                    group = child,
                                    isRoot = false,
                                    depth = depth + 1,
                                    onAddFilter = onAddFilter,
                                    onAddGroup = onAddGroup,
                                    onRemove = onRemove,
                                    onToggleNegation = onToggleNegation,
                                    onChangeOperator = onChangeOperator,
                                    onEditFilter = onEditFilter,
                                    onReorder = onReorder
                                )
                            is QueryNode.FilterNode ->
                                FilterNodeRow(
                                    filter = child,
                                    onEdit = { onEditFilter(child) },
                                    onRemove = { onRemove(child.id) },
                                    onToggleNegation = { onToggleNegation(child.id) }
                                )
                        }
                    }
                }

                // ── Drop indicator below the last item ──────────────────────
                if (idx == group.children.lastIndex && draggedId != null && targetIdx > group.children.lastIndex - 1) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // ── AND/OR separator (tappable) ─────────────────────────────
                if (idx < group.children.lastIndex) {
                    OperatorSeparator(
                        operator = group.operator,
                        onToggle = {
                            onChangeOperator(group.id, group.operator.opposite())
                        }
                    )
                }
            }
        }

        // ── Add buttons ───────────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onAddFilter(group.id) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filter", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onAddGroup(group.id) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Group", fontSize = 12.sp)
            }
        }
    }
}

// ── AND/OR operator separator ─────────────────────────────────────────────────

/**
 * A centered, tappable chip displayed between sibling items.
 * Shows the current operator (AND/OR) and toggles it when tapped.
 */
@Composable
private fun OperatorSeparator(operator: LogicOperator, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable(onClick = onToggle)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = operator.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "· tap to switch to ${operator.opposite().displayName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ── Single filter row ─────────────────────────────────────────────────────────

@Composable
fun FilterNodeRow(
    filter: QueryNode.FilterNode,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onToggleNegation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .clickable { onEdit() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // NOT indicator
        if (filter.negated) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    "NOT",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = filter.filterDef.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = SearchSerializer.filterValueSummary(filter.filterDef, filter.value),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            Text(
                text = SearchSerializer.filterToSearchToken(filter.filterDef, filter.value)
                    .let { if (filter.negated) "!$it" else it },
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // NOT toggle
        IconButton(onClick = onToggleNegation, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = if (filter.negated) Icons.Default.Block else Icons.Default.RemoveCircleOutline,
                contentDescription = "Toggle NOT",
                tint = if (filter.negated) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Delete
        IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
private fun AndOrToggle(selected: LogicOperator, onChange: (LogicOperator) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogicOperator.entries.forEach { op ->
            val isSelected = op == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onChange(op) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = op.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotToggleButton(negated: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (negated) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "NOT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (negated) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Drag-to-reorder helper ────────────────────────────────────────────────────

/**
 * Given that the item at [originalIdx] has been dragged by [dragOffsetY] pixels,
 * calculate the index where it should be dropped.
 *
 * The algorithm finds the sibling item whose center Y is closest to the dragged
 * item's new center Y and returns that item's index.
 */
internal fun calculateTargetIndex(
    originalIdx: Int,
    dragOffsetY: Float,
    childIds: List<String>,
    itemHeights: Map<String, Int>
): Int {
    val n = childIds.size
    if (n <= 1) return 0

    // Build per-item top positions and centers
    val tops   = FloatArray(n)
    val heights = FloatArray(n)
    var y = 0f
    for (i in childIds.indices) {
        tops[i]   = y
        heights[i] = (itemHeights[childIds[i]] ?: 72).toFloat()
        y += heights[i]
    }

    // Center of dragged item in its new position
    val draggedNewCenter = tops[originalIdx] + dragOffsetY + heights[originalIdx] / 2f

    // Find sibling whose center is closest
    var bestIdx  = originalIdx
    var bestDist = Float.MAX_VALUE
    for (i in childIds.indices) {
        val center = tops[i] + heights[i] / 2f
        val dist   = abs(draggedNewCenter - center)
        if (dist < bestDist) {
            bestDist = dist
            bestIdx  = i
        }
    }
    return bestIdx.coerceIn(0, n - 1)
}

// ── Extensions ────────────────────────────────────────────────────────────────

/** Returns the opposite operator (AND ↔ OR). */
fun LogicOperator.opposite(): LogicOperator =
    if (this == LogicOperator.AND) LogicOperator.OR else LogicOperator.AND

// ── Depth-based visual theming ────────────────────────────────────────────────

private val depthColors = listOf(
    Color(0x0D3B4CCA),   // blue tint  depth 1
    Color(0x0DEE1515),   // red tint   depth 2
    Color(0x0DFFCB05),   // yellow     depth 3
    Color(0x0D4CAF50),   // green      depth 4
    Color(0x0D9C27B0),   // purple     depth 5+
)

@Composable
private fun groupBackgroundColor(depth: Int, isRoot: Boolean): Color {
    if (isRoot) return Color.Transparent
    return depthColors.getOrElse(depth - 1) { depthColors.last() }
}

@Composable
private fun groupBorderColor(depth: Int): Color = when (depth % 4) {
    0    -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    1    -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
    2    -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
}
