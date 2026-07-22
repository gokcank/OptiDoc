package com.gokcank.optidoc.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File
import coil.request.ImageRequest
import com.gokcank.optidoc.R
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    documentId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val context = LocalContext.current
    var showFormatDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<IntentAction?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.actionEvent.collect { event ->
            when (event) {
                is ActionEvent.Success -> {
                    if (event.uris.size == 1) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = event.mimeType
                            val uriStr = event.uris.first()
                            val shareUri = getShareableUri(context, uriStr, event.documentTitle, event.mimeType, 0, 1)
                            putExtra(Intent.EXTRA_STREAM, shareUri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_file)))
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, context.getString(R.string.error_prefix, e.message ?: ""), android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = event.mimeType
                            val shareUris = arrayListOf<Uri>()
                            event.uris.forEachIndexed { index, uriStr ->
                                shareUris.add(getShareableUri(context, uriStr, event.documentTitle, event.mimeType, index, event.uris.size))
                            }
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_file)))
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, context.getString(R.string.error_prefix, e.message ?: ""), android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
                is ActionEvent.SaveSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ActionEvent.Error -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_prefix, event.message))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.detail_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            when (val state = uiState) {
                is DetailUiState.Success -> {
                    DetailBottomBar(
                        document = state.document,
                        onSaveClick = {
                            pendingAction = IntentAction.SAVE
                            showFormatDialog = true
                        },
                        onShareClick = {
                            pendingAction = IntentAction.SHARE
                            showFormatDialog = true
                        },
                        onEditClick = { onEdit(state.document.id) }
                    )
                    
                    if (showFormatDialog && pendingAction != null) {
                        FormatSelectionDialog(
                            document = state.document,
                            action = pendingAction!!,
                            onDismiss = { showFormatDialog = false },
                            onFormatSelected = { format ->
                                val action = pendingAction!!
                                if (format == com.gokcank.optidoc.domain.model.ExportFormat.JPEG) {
                                    viewModel.performActionAsJpeg(action)
                                } else {
                                    val uriStr = state.document.getOutputUri()
                                    if (uriStr != null) {
                                        val mimeType = if (format == com.gokcank.optidoc.domain.model.ExportFormat.TXT) "text/plain" else "application/pdf"
                                        viewModel.performExistingAction(uriStr, mimeType, action, state.document.title)
                                    }
                                }
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.NotFound -> {
                    Text(
                        text = stringResource(R.string.error_document_not_found),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DetailUiState.Success -> {
                    DocumentPagesPager(pages = state.pages)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentPagesPager(pages: List<DocumentPage>) {
    if (pages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_pages_available))
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface)
        ) { pageIndex ->
            val page = pages[pageIndex]
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(page.imageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.page_number, pageIndex + 1),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${pages.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun DetailBottomBar(
    document: ScannedDocument,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val isReady = document.output !is DocumentOutput.None

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isReady) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(stringResource(R.string.continue_edit), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                OutlinedButton(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.save_to_device), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.share), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun ScannedDocument.getOutputUri(): String? {
    return when (val out = output) {
        is DocumentOutput.None -> null
        is DocumentOutput.DirectPdf -> out.uri
        is DocumentOutput.OcrExport -> out.uri
    }
}

private fun getShareableUri(context: android.content.Context, uriStr: String, title: String, mimeType: String, index: Int, total: Int): Uri {
    val uri = Uri.parse(uriStr)
    if (uri.scheme != "file") return uri

    val extension = when (mimeType) {
        "application/pdf" -> ".pdf"
        "text/plain" -> ".txt"
        "image/jpeg" -> ".jpg"
        else -> ""
    }
    
    val safeTitle = title.replace(Regex("[^a-zA-Z0-9ğüşıöçĞÜŞİÖÇ _\\-]"), "_").trim()
    val fileName = if (total > 1) "${safeTitle}_s${index + 1}$extension" else "$safeTitle$extension"
    
    val shareDir = java.io.File(context.cacheDir, "shared_docs")
    if (!shareDir.exists()) shareDir.mkdirs()
    val destFile = java.io.File(shareDir, fileName)
    
    val sourceFile = java.io.File(uri.path!!)
    sourceFile.copyTo(destFile, overwrite = true)
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        destFile
    )
}

@Composable
private fun FormatSelectionDialog(
    document: ScannedDocument,
    action: IntentAction,
    onDismiss: () -> Unit,
    onFormatSelected: (com.gokcank.optidoc.domain.model.ExportFormat) -> Unit
) {
    val availableFormats = mutableListOf<com.gokcank.optidoc.domain.model.ExportFormat>()
    availableFormats.add(com.gokcank.optidoc.domain.model.ExportFormat.JPEG)
    
    when (val out = document.output) {
        is DocumentOutput.DirectPdf -> availableFormats.add(0, com.gokcank.optidoc.domain.model.ExportFormat.PDF)
        is DocumentOutput.OcrExport -> {
            if (out.format == com.gokcank.optidoc.domain.model.ExportFormat.TXT) {
                availableFormats.add(0, com.gokcank.optidoc.domain.model.ExportFormat.TXT)
            } else if (out.format == com.gokcank.optidoc.domain.model.ExportFormat.PDF) {
                availableFormats.add(0, com.gokcank.optidoc.domain.model.ExportFormat.PDF)
            }
        }
        else -> {}
    }

    val titleText = if (action == IntentAction.SAVE) {
        stringResource(R.string.choose_save_format)
    } else {
        stringResource(R.string.choose_share_format)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column {
                availableFormats.forEach { format ->
                    TextButton(
                        onClick = {
                            onFormatSelected(format)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(format.name)
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
