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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gokcank.optidoc.domain.model.ExportFormat
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
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
                        FormatSelectionBottomSheet(
                            document = state.document,
                            action = pendingAction!!,
                            onDismiss = { showFormatDialog = false },
                            onFormatSelected = { format ->
                                val action = pendingAction!!
                                if (format == ExportFormat.JPEG) {
                                    viewModel.performActionAsJpeg(action)
                                } else {
                                    val uriStr = state.document.getOutputUri()
                                    if (uriStr != null) {
                                        val mimeType = if (format == ExportFormat.TXT) "text/plain" else "application/pdf"
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
                    val pagerState = rememberPagerState(pageCount = { state.pages.size })

                    Column(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { pageIndex ->
                            val page = state.pages[pageIndex]
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.5f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(Uri.parse(page.imageUri))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = stringResource(R.string.page_number, pageIndex + 1),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (!page.ocrText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ocr_text_label),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = page.ocrText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (state.pages.size > 1) {
                            Text(
                                text = stringResource(R.string.page_indicator, pagerState.currentPage + 1, state.pages.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            if (isExporting) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBottomBar(
    document: ScannedDocument,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onSaveClick) {
                Text(stringResource(R.string.save_file))
            }
            Button(onClick = onShareClick) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.share_file))
            }
            if (document.output is DocumentOutput.OcrExport) {
                TextButton(onClick = onEditClick) {
                    Text(stringResource(R.string.continue_edit))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatSelectionBottomSheet(
    document: ScannedDocument,
    action: IntentAction,
    onDismiss: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val availableFormats = mutableListOf<ExportFormat>()
    availableFormats.add(ExportFormat.JPEG)
    
    when (val out = document.output) {
        is DocumentOutput.DirectPdf -> availableFormats.add(0, ExportFormat.PDF)
        is DocumentOutput.OcrExport -> {
            if (out.format == ExportFormat.TXT) {
                availableFormats.add(0, ExportFormat.TXT)
            } else if (out.format == ExportFormat.PDF) {
                availableFormats.add(0, ExportFormat.PDF)
            }
        }
        else -> {}
    }

    val titleText = if (action == IntentAction.SAVE) {
        stringResource(R.string.choose_save_format)
    } else {
        stringResource(R.string.choose_share_format)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            availableFormats.forEach { format ->
                val icon: ImageVector = when (format) {
                    ExportFormat.PDF -> Icons.Default.PictureAsPdf
                    ExportFormat.JPEG -> Icons.Default.Image
                    ExportFormat.TXT -> Icons.Default.Description
                }
                val formatLabel = when (format) {
                    ExportFormat.PDF -> stringResource(R.string.export_format_pdf)
                    ExportFormat.JPEG -> "JPEG Image"
                    ExportFormat.TXT -> stringResource(R.string.export_format_txt)
                }
                Surface(
                    onClick = {
                        onFormatSelected(format)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = formatLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
