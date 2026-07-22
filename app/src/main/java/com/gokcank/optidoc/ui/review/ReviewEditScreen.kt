package com.gokcank.optidoc.ui.review

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gokcank.optidoc.R
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewEditScreen(
    documentId: Long,
    onExportCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.exportCompleted.collect {
            onExportCompleted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_edit_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState is ReviewEditUiState.Success) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_and_continue))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ReviewEditUiState.Loading, is ReviewEditUiState.Exporting -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReviewEditUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ReviewEditUiState.Success -> {
                    EditPager(
                        pages = state.pages,
                        onTextChanged = { pageId, text -> viewModel.updatePageText(pageId, text) },
                        onMovePage = { from, to -> viewModel.movePage(from, to) }
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.review_export_title)) },
            text = { Text(stringResource(R.string.select_format_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    viewModel.exportDocument(ExportFormat.PDF)
                }) {
                    Text(stringResource(R.string.export_format_pdf))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showExportDialog = false
                        viewModel.exportDocument(ExportFormat.TXT)
                    }) {
                        Text(stringResource(R.string.export_format_txt))
                    }
                    TextButton(onClick = {
                        showExportDialog = false
                        viewModel.exportDocument(ExportFormat.JPEG)
                    }) {
                        Text("JPEG")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditPager(
    pages: List<DocumentPage>,
    onTextChanged: (Long, String) -> Unit,
    onMovePage: (Int, Int) -> Unit
) {
    if (pages.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Text field
                OutlinedTextField(
                    value = page.ocrText ?: "",
                    onValueChange = { onTextChanged(page.id, it) },
                    label = { Text(stringResource(R.string.ocr_text_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Sayfa yönlendirme ve sıralama kontrol alanı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onMovePage(pagerState.currentPage, pagerState.currentPage - 1) },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.move_left))
            }

            Text(
                text = stringResource(R.string.page_number, pagerState.currentPage + 1),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onMovePage(pagerState.currentPage, pagerState.currentPage + 1) },
                enabled = pagerState.currentPage < pages.size - 1
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.move_right))
            }
        }
    }
}
