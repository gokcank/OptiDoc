package com.gokcank.optidoc.ui.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.optidoc.ui.scanner.rememberDocumentScannerLauncher

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    onScanCompleted: (pageImageUris: List<String>, nativePdfUri: String?) -> Unit,
    onScanCancelled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    val startScan = rememberDocumentScannerLauncher(
        scanner = viewModel.documentScanner,
        onScanSuccess = { pageUris, pdfUri ->
            coroutineScope.launch {
                val (tempUris, tempPdf) = viewModel.copyToTemp(
                    pageUris.map { it.toString() },
                    pdfUri?.toString()
                )
                onScanCompleted(tempUris, tempPdf)
            }
        },
        onScanFailure = { 
            onScanCancelled()
        }
    )

    // Ekrana girer girmez tarayıcıyı başlat.
    LaunchedEffect(Unit) {
        startScan()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Tarayıcı activity'si açılana kadar kısa bir loading göster.
        CircularProgressIndicator()
    }
}
