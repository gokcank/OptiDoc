package com.gokcank.optidoc.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.usecase.ExportPageImagesUseCase
import com.gokcank.optidoc.domain.usecase.GetDocumentDetailUseCase
import com.gokcank.optidoc.domain.repository.ExportRepository
import com.gokcank.optidoc.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val document: ScannedDocument, val pages: List<DocumentPage>) : DetailUiState
    data object NotFound : DetailUiState
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getDocumentDetailUseCase: GetDocumentDetailUseCase,
    private val exportPageImagesUseCase: ExportPageImagesUseCase,
    private val exportRepository: ExportRepository,
    private val documentRepository: com.gokcank.optidoc.domain.repository.DocumentRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Detail>()
    private val documentId = route.documentId

    val uiState: StateFlow<DetailUiState> = getDocumentDetailUseCase(documentId)
        .map { result ->
            if (result == null) DetailUiState.NotFound
            else DetailUiState.Success(result.first, result.second)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    private val _actionEvent = MutableSharedFlow<ActionEvent>()
    val actionEvent = _actionEvent.asSharedFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun performActionAsJpeg(action: IntentAction) {
        if (action == IntentAction.SAVE) {
            saveAsJpeg()
        } else {
            // SHARE - send internal URIs to avoid cluttering gallery
            viewModelScope.launch {
                documentRepository.getDocumentWithPages(documentId).firstOrNull()?.let { (doc, pages) ->
                    val uris = pages.sortedBy { it.pageNumber }.map { it.imageUri }
                    _actionEvent.emit(ActionEvent.Success(uris, "image/jpeg", action, doc.title))
                }
            }
        }
    }

    fun performExistingAction(uriStr: String, mimeType: String, action: IntentAction, title: String) {
        if (action == IntentAction.SAVE) {
            saveExistingAs(uriStr, mimeType, title)
        } else {
            // SHARE
            viewModelScope.launch {
                _actionEvent.emit(ActionEvent.Success(listOf(uriStr), mimeType, action, title))
            }
        }
    }

    private fun saveAsJpeg() {
        viewModelScope.launch {
            _isExporting.value = true
            exportPageImagesUseCase(documentId)
                .onSuccess { uris ->
                    _isExporting.value = false
                    _actionEvent.emit(ActionEvent.SaveSuccess("${uris.size} JPEG kaydedildi."))
                }
                .onFailure { err ->
                    _isExporting.value = false
                    _actionEvent.emit(ActionEvent.Error(err.message ?: "JPEG kaydetme başarısız"))
                }
        }
    }

    private fun saveExistingAs(uriStr: String, mimeType: String, title: String) {
        viewModelScope.launch {
            _isExporting.value = true
            exportRepository.exportExistingFile(documentId, uriStr, mimeType, title)
                .onSuccess { _ ->
                    _isExporting.value = false
                    _actionEvent.emit(ActionEvent.SaveSuccess("Dosya başarıyla kaydedildi."))
                }
                .onFailure { err ->
                    _isExporting.value = false
                    _actionEvent.emit(ActionEvent.Error(err.message ?: "Dosya kaydetme başarısız"))
                }
        }
    }
}

enum class IntentAction { SAVE, SHARE }

sealed interface ActionEvent {
    data class Success(val uris: List<String>, val mimeType: String, val action: IntentAction, val documentTitle: String) : ActionEvent
    data class SaveSuccess(val message: String) : ActionEvent
    data class Error(val message: String) : ActionEvent
}
