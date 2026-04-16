package com.pokesearch.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesearch.model.*
import com.pokesearch.parser.SearchSerializer

/**
 * Recursively renders the entire query tree.
 * [onAddFilter], [onAddGroup], [onRemove], [onToggleNegation],
 * [onChangeOperator], and [onEditFilter] are forwarded to the ViewModel.
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
    modifier: Modifier = Modifier
) {
    val indentStart: Dp = if (!isRoot) 12.dp else 0.dp
    val borderColor = groupBorderColor(depth)

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
                    negated  = group.negated,
                    onClick  = { onToggleNegation(group.id) }
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
                            onEditFilter = onEditFilter
                        )
                    is QueryNode.FilterNode ->
                        FilterNodeRow(
                            filter = child,
                            onEdit = { onEditFilter(child) },
                            onRemove = { onRemove(child.id) },
                            onToggleNegation = { onToggleNegation(child.id) }
                        )
                }
                // Operator separator label between children
                if (idx < group.children.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = group.operator.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
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
        IconButton(
            onClick = onToggleNegation,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = if (filter.negated) Icons.Default.Block else Icons.Default.RemoveCircleOutline,
                contentDescription = "Toggle NOT",
                tint = if (filter.negated) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Delete
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(30.dp)
        ) {
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
private fun AndOrToggle(
    selected: LogicOperator,
    onChange: (LogicOperator) -> Unit
) {
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
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
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

// Depth-based group colours for visual nesting clarity
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
    val baseColor = depthColors.getOrElse(depth - 1) { depthColors.last() }
    return baseColor
}

@Composable
private fun groupBorderColor(depth: Int): Color {
    return when (depth % 4) {
        0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }
}
