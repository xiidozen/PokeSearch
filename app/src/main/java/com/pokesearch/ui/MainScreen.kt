package com.pokesearch.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokesearch.ui.components.FilterEditorSheet
import com.pokesearch.ui.components.QueryTreeView
import com.pokesearch.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val context  = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Snackbar messages ─────────────────────────────────────────────────────
    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            vm.clearMessage()
        }
    }

    // ── Filter editor sheet ───────────────────────────────────────────────────
    val showSheet = uiState.editingNode != null || uiState.addingToGroupId != null
    if (showSheet) {
        FilterEditorSheet(
            initial   = uiState.editingNode,
            onConfirm = { def, value, negated -> vm.confirmFilter(def, value, negated) },
            onDismiss = { vm.dismissSheet() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PokeSearch", fontWeight = FontWeight.Bold)
                        Text(
                            "Pokémon GO Search Builder",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { vm.clearAll() }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Paste / parse section ─────────────────────────────────────────
            PasteParseSection(onParse = { vm.parseSearchString(it) })

            HorizontalDivider()

            // ── Query builder ─────────────────────────────────────────────────
            Text(
                "Query Builder",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            QueryTreeView(
                group            = uiState.root,
                isRoot           = true,
                depth            = 0,
                onAddFilter      = { vm.openAddFilterSheet(it) },
                onAddGroup       = { vm.addGroup(it) },
                onRemove         = { vm.removeNode(it) },
                onToggleNegation = { vm.toggleNegation(it) },
                onChangeOperator = { id, op -> vm.changeGroupOperator(id, op) },
                onEditFilter     = { vm.openEditFilterSheet(it) },
                onReorder        = { nodeId, toIndex -> vm.reorderNode(nodeId, toIndex) },
                modifier         = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // ── Result / output section ───────────────────────────────────────
            ResultSection(
                searchString = uiState.searchString,
                onCopy       = { copyToClipboard(context, uiState.searchString) }
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ── Paste / parse card ────────────────────────────────────────────────────────

@Composable
private fun PasteParseSection(onParse: (String) -> Unit) {
    var pasteText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Paste Existing Search",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Paste a Pokémon GO search string to load it into the builder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = pasteText,
                onValueChange = { pasteText = it },
                placeholder   = { Text("e.g. shiny&cp-1500,4*") },
                singleLine    = true,
                trailingIcon  = {
                    if (pasteText.isNotEmpty()) {
                        IconButton(onClick = { pasteText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick  = { if (pasteText.isNotBlank()) onParse(pasteText) },
                enabled  = pasteText.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Parse")
            }
        }
    }
}

// ── Result card ───────────────────────────────────────────────────────────────

@Composable
private fun ResultSection(searchString: String, onCopy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Search String",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick  = onCopy,
                enabled  = searchString.isNotEmpty(),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy", fontSize = 14.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(12.dp)
            ) {
                if (searchString.isEmpty()) {
                    Text(
                        "Add filters above to build your search string.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    SelectionContainer {
                        Text(
                            text     = searchString,
                            style    = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color    = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (searchString.isNotEmpty()) {
            Text(
                "${searchString.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Copy to clipboard ─────────────────────────────────────────────────────────

private fun copyToClipboard(context: Context, text: String) {
    if (text.isEmpty()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Pokemon GO Search", text))
}
