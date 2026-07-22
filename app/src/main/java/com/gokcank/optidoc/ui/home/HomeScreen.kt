package com.gokcank.optidoc.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.optidoc.R
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.model.Folder
import com.gokcank.optidoc.domain.model.ScannedDocument
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScanNewDocument: () -> Unit,
    onDocumentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()

    // Çoklu seçim ve dialog durumları
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var documentToMove by remember { mutableStateOf<ScannedDocument?>(null) }
    var documentToDelete by remember { mutableStateOf<ScannedDocument?>(null) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    val isSelectionMode = selectedIds.isNotEmpty()

    fun exitSelection() { selectedIds = emptySet() }

    BackHandler(enabled = isSelectionMode) {
        exitSelection()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topbar"
            ) { inSelectionMode ->
                if (inSelectionMode) {
                    val allIds = (uiState as? HomeUiState.Success)?.documents?.map { it.id }.orEmpty()
                    val allSelected = selectedIds.containsAll(allIds) && allIds.isNotEmpty()
                    CenterAlignedTopAppBar(
                        title = { Text("${selectedIds.size} ${stringResource(R.string.selected)}", style = MaterialTheme.typography.titleMedium) },
                        navigationIcon = {
                            IconButton(onClick = { exitSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_selection))
                            }
                        },
                        actions = {
                            TextButton(onClick = {
                                selectedIds = if (allSelected) emptySet() else allIds.toSet()
                            }) {
                                Text(if (allSelected) stringResource(R.string.clear_all) else stringResource(R.string.select_all))
                            }
                            IconButton(
                                onClick = { showDeleteSelectedDialog = true }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_document),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                } else {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort_by))
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.sort_date_desc)) },
                                        onClick = { viewModel.updateSortOrder(SortOrder.DATE_DESC); showSortMenu = false },
                                        trailingIcon = if (sortOrder == SortOrder.DATE_DESC) { { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) } } else null
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.sort_date_asc)) },
                                        onClick = { viewModel.updateSortOrder(SortOrder.DATE_ASC); showSortMenu = false },
                                        trailingIcon = if (sortOrder == SortOrder.DATE_ASC) { { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) } } else null
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.sort_title_asc)) },
                                        onClick = { viewModel.updateSortOrder(SortOrder.TITLE_ASC); showSortMenu = false },
                                        trailingIcon = if (sortOrder == SortOrder.TITLE_ASC) { { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) } } else null
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.sort_title_desc)) },
                                        onClick = { viewModel.updateSortOrder(SortOrder.TITLE_DESC); showSortMenu = false },
                                        trailingIcon = if (sortOrder == SortOrder.TITLE_DESC) { { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) } } else null
                                    )
                                }
                            }
                            IconButton(onClick = { showAboutDialog = true }) {
                                Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.about))
                            }
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onScanNewDocument,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.new_scan)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                )
            }
        },
        bottomBar = {
            com.gokcank.optidoc.ui.components.AdBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Text(
                        text = stringResource(R.string.error_occurred, state.message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text(stringResource(R.string.search_documents)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        // Folder Chips Bar
                        FolderChipsRow(
                            folders = folders,
                            selectedFolderId = selectedFolderId,
                            onSelectFolder = { viewModel.selectFolder(it) },
                            onCreateFolderClick = { showCreateFolderDialog = true },
                            onFolderLongClick = { folderToDelete = it }
                        )

                        if (state.documents.isEmpty()) {
                            if (searchQuery.isNotBlank()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_results_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    EmptyState()
                                }
                            }
                        } else {
                            DocumentList(
                                documents = state.documents,
                                selectedIds = selectedIds,
                                isSelectionMode = isSelectionMode,
                                onDocumentClick = { doc ->
                                    if (isSelectionMode) {
                                        selectedIds = if (selectedIds.contains(doc.id))
                                            selectedIds - doc.id
                                        else
                                            selectedIds + doc.id
                                    } else {
                                        onDocumentClick(doc.id)
                                    }
                                },
                                onDocumentLongClick = { doc ->
                                    selectedIds = selectedIds + doc.id
                                },
                                onDelete = { doc -> documentToDelete = doc },
                                onMoveToFolder = { doc -> documentToMove = doc }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }

    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text(stringResource(R.string.delete_document_title)) },
            text = { Text(stringResource(R.string.delete_document_confirm, doc.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        documentToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_document), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text(stringResource(R.string.delete_selected_title)) },
            text = { Text(stringResource(R.string.delete_selected_confirm, selectedIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedIds.forEach { viewModel.deleteDocument(it) }
                        exitSelection()
                        showDeleteSelectedDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete_document), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text(stringResource(R.string.delete_folder_title)) },
            text = { Text(stringResource(R.string.delete_folder_confirm, folder.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folder.id)
                        folderToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_document), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    documentToMove?.let { doc ->
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = doc.folderId,
            onDismiss = { documentToMove = null },
            onSelectFolder = { targetFolderId ->
                viewModel.moveDocumentToFolder(doc.id, targetFolderId)
                documentToMove = null
            }
        )
    }
}

@Composable
private fun FolderChipsRow(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onSelectFolder: (Long?) -> Unit,
    onCreateFolderClick: () -> Unit,
    onFolderLongClick: (Folder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tümü Chip
        FilterChip(
            selected = selectedFolderId == null,
            onClick = { onSelectFolder(null) },
            label = { Text(stringResource(R.string.folder_all)) },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedFolderId == null) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Klasörler
        folders.forEach { folder ->
            val isSelected = selectedFolderId == folder.id
            OptiDocFolderChip(
                folder = folder,
                isSelected = isSelected,
                onClick = { onSelectFolder(folder.id) },
                onLongClick = { onFolderLongClick(folder) }
            )
        }

        // Yeni Klasör Ekle Chip
        AssistChip(
            onClick = onCreateFolderClick,
            label = { Text(stringResource(R.string.folder_new)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OptiDocFolderChip(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(folder.name) },
        leadingIcon = {
            Icon(
                imageVector = if (isSelected) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = if (isSelected) {
            {
                IconButton(
                    onClick = onLongClick,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete_folder_title),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        } else null,
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    )
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_folder_title)) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text(stringResource(R.string.folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (folderName.isNotBlank()) onCreate(folderName.trim()) },
                enabled = folderName.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onSelectFolder: (Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.move_to_folder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onSelectFolder(null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (currentFolderId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.folder_none), modifier = Modifier.weight(1f))
                }
                folders.forEach { folder ->
                    TextButton(
                        onClick = { onSelectFolder(folder.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (currentFolderId == folder.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, Modifier.padding(end = 8.dp))
                        Text(folder.name, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun BoxScope.EmptyState() {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_optidoc_logo_detailed),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
            .align(Alignment.Center),
        alpha = 0.15f,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.welcome_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        FeatureItem(icon = "📄", text = stringResource(R.string.feature_scan))
        Spacer(modifier = Modifier.height(16.dp))
        FeatureItem(icon = "🔍", text = stringResource(R.string.feature_ocr))
        Spacer(modifier = Modifier.height(16.dp))
        FeatureItem(icon = "📁", text = stringResource(R.string.feature_folder))

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun FeatureItem(icon: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DocumentList(
    documents: List<ScannedDocument>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    onDocumentClick: (ScannedDocument) -> Unit,
    onDocumentLongClick: (ScannedDocument) -> Unit,
    onDelete: (ScannedDocument) -> Unit,
    onMoveToFolder: (ScannedDocument) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(documents, key = { it.id }) { document ->
            val isSelected = selectedIds.contains(document.id)

            if (isSelectionMode) {
                DocumentCard(
                    document = document,
                    isSelected = isSelected,
                    onClick = { onDocumentClick(document) },
                    onLongClick = { onDocumentLongClick(document) },
                    onMoveToFolder = { onMoveToFolder(document) },
                    onDelete = { onDelete(document) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null
                    )
                )
            } else {
                val dismissState = rememberSwipeToDismissBoxState()
                val isDismissed = dismissState.currentValue == SwipeToDismissBoxValue.EndToStart

                LaunchedEffect(isDismissed) {
                    if (isDismissed) onDelete(document)
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val fraction = dismissState.progress.coerceIn(0f, 1f)
                        val containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = fraction)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = containerColor,
                                shape = MaterialTheme.shapes.large
                            ) {
                                Box(contentAlignment = Alignment.CenterEnd) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_document),
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(end = 24.dp)
                                    )
                                }
                            }
                        }
                    },
                    enableDismissFromStartToEnd = false,
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null
                    )
                ) {
                    DocumentCard(
                        document = document,
                        isSelected = false,
                        onClick = { onDocumentClick(document) },
                        onLongClick = { onDocumentLongClick(document) },
                        onMoveToFolder = { onMoveToFolder(document) },
                        onDelete = { onDelete(document) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentCard(
    document: ScannedDocument,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                val formattedDate = remember(document.createdAt) {
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(document.createdAt))
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.pages_count, document.pageCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (document.output is DocumentOutput.OcrExport) {
                        val out = document.output as DocumentOutput.OcrExport
                        val outputText = if (out.format == ExportFormat.TXT) stringResource(R.string.format_txt_ocr) else stringResource(R.string.format_pdf_ocr)
                        Text(
                            text = stringResource(R.string.output_format, outputText),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.move_to_folder)) },
                        onClick = {
                            showMenu = false
                            onMoveToFolder()
                        },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_document), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}
