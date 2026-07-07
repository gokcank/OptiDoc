package com.gokcank.optidoc.ui.review

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gokcank.optidoc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    pageImageUris: List<String>,
    nativePdfUri: String?,
    onSavedDirectly: () -> Unit,
    onOcrCompleted: (documentId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ReviewUiState.SuccessDirect -> onSavedDirectly()
            is ReviewUiState.SuccessOcr -> onOcrCompleted(state.documentId)
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.review_decision_title), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Preview Image
                if (pageImageUris.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(pageImageUris.first()))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error Message
                if (uiState is ReviewUiState.Error) {
                    Text(
                        text = (uiState as ReviewUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Actions
                if (uiState is ReviewUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.applyOcr() },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(20.dp),
                        shape = androidx.compose.foundation.shape.CircleShape // Pill shape
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.apply_ocr), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.apply_ocr_desc),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (nativePdfUri != null) {
                        OutlinedButton(
                            onClick = { viewModel.saveDirectly() },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp),
                            shape = androidx.compose.foundation.shape.CircleShape // Pill shape
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.save_direct), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.save_direct_desc),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
