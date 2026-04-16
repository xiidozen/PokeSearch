package com.pokesearch.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesearch.model.*

/**
 * Bottom sheet for adding or editing a single filter node.
 *
 * Shows a two-step flow:
 *  1. Filter type picker (categorised list).
 *  2. Value editor for the chosen filter type.
 *
 * @param initial  Existing filter to pre-populate (null = add new).
 * @param onConfirm  Called when the user taps "Add Filter" / "Update Filter".
 * @param onDismiss  Called when the sheet is dismissed without confirming.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterEditorSheet(
    initial: QueryNode.FilterNode?,
    onConfirm: (FilterDef, FilterValue, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(if (initial == null) Step.PICK_TYPE else Step.EDIT_VALUE) }
    var selectedDef by remember { mutableStateOf(initial?.filterDef ?: FilterDefs.SHINY) }
    var filterValue by remember { mutableStateOf<FilterValue>(initial?.value ?: FilterValue.BooleanPresent) }
    var negated by remember { mutableStateOf(initial?.negated ?: false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        when (step) {
            Step.PICK_TYPE ->
                FilterTypePicker(
                    onSelect = { def ->
                        selectedDef = def
                        filterValue = defaultValueFor(def)
                        step = Step.EDIT_VALUE
                    }
                )

            Step.EDIT_VALUE ->
                FilterValueEditor(
                    def = selectedDef,
                    value = filterValue,
                    negated = negated,
                    isNew = initial == null,
                    onChangeType = { step = Step.PICK_TYPE },
                    onValueChange = { filterValue = it },
                    onNegatedChange = { negated = it },
                    onConfirm = { onConfirm(selectedDef, filterValue, negated) },
                    onDismiss = onDismiss
                )
        }
    }
}

private enum class Step { PICK_TYPE, EDIT_VALUE }

// ── Filter type picker ────────────────────────────────────────────────────────

@Composable
private fun FilterTypePicker(onSelect: (FilterDef) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Choose Filter Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            FilterDefs.BY_CATEGORY.forEach { (category, defs) ->
                item {
                    Text(
                        text = category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                }
                items(defs) { def ->
                    ListItem(
                        headlineContent = { Text(def.displayName, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            if (def.description.isNotBlank()) {
                                Text(def.description, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { onSelect(def) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

// ── Filter value editor ───────────────────────────────────────────────────────

@Composable
private fun FilterValueEditor(
    def: FilterDef,
    value: FilterValue,
    negated: Boolean,
    isNew: Boolean,
    onChangeType: () -> Unit,
    onValueChange: (FilterValue) -> Unit,
    onNegatedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + back to type picker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onChangeType) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to filter picker")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(def.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (def.description.isNotBlank()) {
                    Text(def.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        HorizontalDivider()

        // Value input based on type
        when (def.valueType) {
            ValueType.BOOLEAN -> {
                BooleanValueInfo(def)
            }
            ValueType.TEXT -> {
                TextValueInput(
                    def = def,
                    value = value as? FilterValue.TextVal ?: FilterValue.TextVal(""),
                    onValueChange = onValueChange
                )
            }
            ValueType.NUMERIC_RANGE -> {
                NumericRangeInput(
                    def = def,
                    value = value as? FilterValue.NumericRange ?: FilterValue.NumericRange(null, null),
                    onValueChange = onValueChange
                )
            }
            ValueType.ENUM_SINGLE -> {
                EnumInput(
                    def = def,
                    value = value as? FilterValue.EnumVal,
                    onValueChange = onValueChange
                )
            }
            ValueType.POKEDEX_RANGE -> {
                PokedexRangeInput(
                    value = value as? FilterValue.PokedexRange ?: FilterValue.PokedexRange(1, null),
                    onValueChange = onValueChange
                )
            }
        }

        HorizontalDivider()

        // NOT toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNegatedChange(!negated) }
                .padding(vertical = 4.dp)
        ) {
            Switch(checked = negated, onCheckedChange = onNegatedChange)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Negate (NOT)", fontWeight = FontWeight.Medium)
                Text(
                    if (negated) "Excludes Pokémon matching this filter"
                    else "Includes Pokémon matching this filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = isValueValid(def, value)
        ) {
            Icon(
                if (isNew) Icons.Default.Add else Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isNew) "Add Filter" else "Update Filter")
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Individual value input composables ───────────────────────────────────────

@Composable
private fun BooleanValueInfo(def: FilterDef) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Text(
                "No value needed — this filter is present/absent only.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TextValueInput(
    def: FilterDef,
    value: FilterValue.TextVal,
    onValueChange: (FilterValue) -> Unit
) {
    val label = when (def.id) {
        "move" -> "Move name (e.g. \"ember\")"
        "tag"  -> "Tag name (without #)"
        "name" -> "Pokémon name (partial OK)"
        else   -> "Value"
    }
    val tokenPrefix = def.searchToken.takeIf { it.isNotBlank() && it != def.id }
    OutlinedTextField(
        value = value.text,
        onValueChange = { onValueChange(FilterValue.TextVal(it)) },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        prefix = if (tokenPrefix != null) {
            { Text(tokenPrefix, color = MaterialTheme.colorScheme.primary) }
        } else null
    )
}

@Composable
private fun NumericRangeInput(
    def: FilterDef,
    value: FilterValue.NumericRange,
    onValueChange: (FilterValue) -> Unit
) {
    Text("Range (leave either blank for open-ended, enter same value for exact):",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value.min?.toString() ?: "",
            onValueChange = { v ->
                onValueChange(value.copy(min = v.trim().toIntOrNull()))
            },
            label = { Text("Min") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value.max?.toString() ?: "",
            onValueChange = { v ->
                onValueChange(value.copy(max = v.trim().toIntOrNull()))
            },
            label = { Text("Max") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }

    // Preview
    val preview = com.pokesearch.parser.SearchSerializer.numericRangeString(def.searchToken, value)
    Text(
        "Preview: $preview",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnumInput(
    def: FilterDef,
    value: FilterValue.EnumVal?,
    onValueChange: (FilterValue) -> Unit
) {
    Text("Select one:", style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        def.enumOptions.forEach { option ->
            FilterChip(
                selected = value?.key == option.key,
                onClick = { onValueChange(FilterValue.EnumVal(option.key)) },
                label = { Text(option.displayName, fontSize = 13.sp) }
            )
        }
    }
}

@Composable
private fun PokedexRangeInput(
    value: FilterValue.PokedexRange,
    onValueChange: (FilterValue) -> Unit
) {
    Text("Single number or a range:", style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value.start.toString(),
            onValueChange = { v ->
                onValueChange(value.copy(start = v.trim().toIntOrNull() ?: 1))
            },
            label = { Text("From #") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("#") },
            modifier = Modifier.weight(1f)
        )
        Text("–")
        OutlinedTextField(
            value = value.end?.toString() ?: "",
            onValueChange = { v ->
                onValueChange(value.copy(end = v.trim().toIntOrNull()))
            },
            label = { Text("To # (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("#") },
            modifier = Modifier.weight(1f)
        )
    }
    val preview = if (value.end != null) "#${value.start}-${value.end}" else "#${value.start}"
    Text("Preview: $preview", style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun defaultValueFor(def: FilterDef): FilterValue = when (def.valueType) {
    ValueType.BOOLEAN       -> FilterValue.BooleanPresent
    ValueType.TEXT          -> FilterValue.TextVal("")
    ValueType.NUMERIC_RANGE -> FilterValue.NumericRange(null, null)
    ValueType.ENUM_SINGLE   -> FilterValue.EnumVal(def.enumOptions.firstOrNull()?.key ?: "")
    ValueType.POKEDEX_RANGE -> FilterValue.PokedexRange(1, null)
}

private fun isValueValid(def: FilterDef, value: FilterValue): Boolean = when (def.valueType) {
    ValueType.BOOLEAN       -> true
    ValueType.TEXT          -> (value as? FilterValue.TextVal)?.text?.isNotBlank() == true
    ValueType.NUMERIC_RANGE -> {
        val r = value as? FilterValue.NumericRange
        r != null && (r.min != null || r.max != null)
    }
    ValueType.ENUM_SINGLE   -> (value as? FilterValue.EnumVal)?.key?.isNotBlank() == true
    ValueType.POKEDEX_RANGE -> true
}
