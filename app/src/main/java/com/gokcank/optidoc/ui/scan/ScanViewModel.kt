package com.gokcank.optidoc.ui.scan

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.gokcank.optidoc.data.storage.FileStorageManager

@HiltViewModel
class ScanViewModel @Inject constructor(
    val documentScanner: GmsDocumentScanner,
    private val fileStorageManager: FileStorageManager
) : ViewModel() {
    
    suspend fun copyToTemp(uris: List<String>, pdfUri: String?): Pair<List<String>, String?> {
        return fileStorageManager.copyToTemp(uris, pdfUri)
    }
}
