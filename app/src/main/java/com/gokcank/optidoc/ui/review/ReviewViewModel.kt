package com.gokcank.optidoc.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gokcank.optidoc.domain.usecase.RunOcrUseCase
import com.gokcank.optidoc.domain.usecase.SaveDirectDocumentUseCase
import com.gokcank.optidoc.domain.model.ScanResult
import com.gokcank.optidoc.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

sealed interface ReviewUiState {
    data object Idle : ReviewUiState
    data object Loading : ReviewUiState
    data class SuccessDirect(val documentId: Long) : ReviewUiState
    data class SuccessOcr(val documentId: Long) : ReviewUiState
    data class Error(val message: String) : ReviewUiState
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveDirectDocumentUseCase: SaveDirectDocumentUseCase,
    private val runOcrUseCase: RunOcrUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Review>()
    val pageImageUris = route.pageImageUris
    private val nativePdfUri = route.nativePdfUri

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private fun generateDefaultTitle(): String {
        val prefix = "Scan"
        val date = java.text.SimpleDateFormat("dd-MM-yyyy-HHmm", java.util.Locale.getDefault()).format(java.util.Date())
        return "${prefix}_$date"
    }

    fun saveDirectly() {
        if (nativePdfUri == null) {
            _uiState.value = ReviewUiState.Error("PDF is not available for direct save.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            val title = generateDefaultTitle()
            saveDirectDocumentUseCase(
                title = title,
                scanResult = ScanResult(pageImageUris, nativePdfUri)
            ).onSuccess { id ->
                _uiState.value = ReviewUiState.SuccessDirect(id)
            }.onFailure { err ->
                _uiState.value = ReviewUiState.Error(err.message ?: "Failed to save directly")
            }
        }
    }

    fun applyOcr() {
        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            val title = generateDefaultTitle()
            runOcrUseCase(
                title = title,
                pageImageUris = pageImageUris
            ).onSuccess { (id, _) ->
                _uiState.value = ReviewUiState.SuccessOcr(id)
            }.onFailure { err ->
                _uiState.value = ReviewUiState.Error(err.message ?: "Failed to apply OCR")
            }
        }
    }
}


