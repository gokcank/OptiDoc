package com.gokcank.optidoc.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.repository.ExportRepository
import com.gokcank.optidoc.domain.usecase.ExportDocumentUseCase
import com.gokcank.optidoc.domain.usecase.ExportPageImagesUseCase
import com.gokcank.optidoc.domain.usecase.GetDocumentDetailUseCase
import com.gokcank.optidoc.domain.usecase.ReorderPagesUseCase
import com.gokcank.optidoc.domain.usecase.UpdatePageTextUseCase
import com.gokcank.optidoc.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewEditUiState {
    data object Loading : ReviewEditUiState
    data class Success(val pages: List<DocumentPage>) : ReviewEditUiState
    data object Exporting : ReviewEditUiState
    data class Error(val message: String) : ReviewEditUiState
}

@HiltViewModel
class ReviewEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDocumentDetailUseCase: GetDocumentDetailUseCase,
    private val updatePageTextUseCase: UpdatePageTextUseCase,
    private val exportDocumentUseCase: ExportDocumentUseCase,
    private val exportPageImagesUseCase: ExportPageImagesUseCase,
    private val reorderPagesUseCase: ReorderPagesUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.ReviewEdit>()
    private val documentId = route.documentId

    private val _uiState = MutableStateFlow<ReviewEditUiState>(ReviewEditUiState.Loading)
    val uiState: StateFlow<ReviewEditUiState> = _uiState.asStateFlow()

    private val _exportCompleted = MutableSharedFlow<Unit>()
    val exportCompleted = _exportCompleted.asSharedFlow()

    private var currentPages: List<DocumentPage> = emptyList()
    private var documentTitle: String = ""

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            getDocumentDetailUseCase(documentId).collect { result ->
                if (result != null) {
                    documentTitle = result.first.title
                    currentPages = result.second
                    if (_uiState.value !is ReviewEditUiState.Exporting) {
                        _uiState.value = ReviewEditUiState.Success(currentPages)
                    }
                } else {
                    _uiState.value = ReviewEditUiState.Error("Document not found")
                }
            }
        }
    }

    fun updatePageText(pageId: Long, newText: String) {
        viewModelScope.launch {
            updatePageTextUseCase(pageId, newText)
        }
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex || fromIndex !in currentPages.indices || toIndex !in currentPages.indices) return
        val mutable = currentPages.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        currentPages = mutable
        _uiState.value = ReviewEditUiState.Success(currentPages)

        viewModelScope.launch {
            reorderPagesUseCase(mutable.map { it.id })
        }
    }

    fun exportDocument(format: ExportFormat) {
        if (currentPages.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = ReviewEditUiState.Exporting

            if (format == ExportFormat.JPEG) {
                exportPageImagesUseCase(documentId)
                    .onSuccess {
                        _exportCompleted.emit(Unit)
                    }
                    .onFailure { err ->
                        _uiState.value = ReviewEditUiState.Error(err.message ?: "Failed to export JPEGs")
                    }
                return@launch
            }

            val sortedPages = currentPages.sortedBy { it.pageNumber }
            val combinedText = sortedPages.joinToString(ExportRepository.PAGE_SEPARATOR) {
                it.ocrText ?: ""
            }

            exportDocumentUseCase(
                documentId = documentId,
                text = combinedText,
                format = format,
                pageCount = currentPages.size,
                title = documentTitle
            ).onSuccess {
                _exportCompleted.emit(Unit)
            }.onFailure { err ->
                _uiState.value = ReviewEditUiState.Error(err.message ?: "Failed to export")
            }
        }
    }
}
