package com.gokcank.optidoc.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
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

    // Çoklu seçim durumu
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedIds.isNotEmpty()

    // Seçim modunu sonlandırır
    fun exitSelection() { selectedIds = emptySet() }

    // Seçim modu açıkken geri tuşu seçimi iptal etsin, uygulamadan çıkmasın
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
                            // Tümünü seç / seçimi kaldır
                            TextButton(onClick = {
                                selectedIds = if (allSelected) emptySet() else allIds.toSet()
                            }) {
                                Text(if (allSelected) stringResource(R.string.clear_all) else stringResource(R.string.select_all))
                            }
                            // Sil
                            IconButton(
                                onClick = {
                                    selectedIds.forEach { viewModel.deleteDocument(it) }
                                    exitSelection()
                                }
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
                    if (state.documents.isEmpty()) {
                        EmptyState()
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
                            onDelete = { viewModel.deleteDocument(it.id) }
                        )
                    }
                }
            } // Close when
        } // Close Box
} // Close Scaffold content lambda
    
    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
    
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun BoxScope.EmptyState() {
    // Watermark Logo
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
        FeatureItem(icon = "📤", text = stringResource(R.string.feature_export))
        
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
    onDelete: (ScannedDocument) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(documents, key = { it.id }) { document ->
            val isSelected = selectedIds.contains(document.id)

            // Seçim modunda swipe devre dışı; normal modda swipe-to-delete aktif
            if (isSelectionMode) {
                DocumentCard(
                    document = document,
                    isSelected = isSelected,
                    onClick = { onDocumentClick(document) },
                    onLongClick = { onDocumentLongClick(document) },
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
                            modifier = Modifier
                                .fillMaxSize(),
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
                        onLongClick = { onDocumentLongClick(document) }
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
    modifier: Modifier = Modifier
) {
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
            // Seçim ikonu
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
        }
    }
}
