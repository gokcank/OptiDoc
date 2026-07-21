package com.gokcank.optidoc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.optidoc.domain.model.Folder
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.usecase.CreateFolderUseCase
import com.gokcank.optidoc.domain.usecase.DeleteDocumentUseCase
import com.gokcank.optidoc.domain.usecase.DeleteFolderUseCase
import com.gokcank.optidoc.domain.usecase.GetDocumentsUseCase
import com.gokcank.optidoc.domain.usecase.GetFoldersUseCase
import com.gokcank.optidoc.domain.usecase.MoveDocumentToFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC }

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val documents: List<ScannedDocument>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDocumentsUseCase: GetDocumentsUseCase,
    getFoldersUseCase: GetFoldersUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val moveDocumentToFolderUseCase: MoveDocumentToFolderUseCase
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val selectedFolderId = MutableStateFlow<Long?>(null)

    val folders: StateFlow<List<Folder>> = getFoldersUseCase()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<HomeUiState> = combine(
        getDocumentsUseCase(),
        searchQuery,
        sortOrder,
        selectedFolderId
    ) { docs, query, sort, folderId ->
        var filteredDocs = if (folderId == null) {
            docs
        } else {
            docs.filter { it.folderId == folderId }
        }

        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filteredDocs = filteredDocs.filter { it.title.lowercase().contains(lowerQuery) }
        }

        filteredDocs = when (sort) {
            SortOrder.DATE_DESC -> filteredDocs.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> filteredDocs.sortedBy { it.createdAt }
            SortOrder.TITLE_ASC -> filteredDocs.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> filteredDocs.sortedByDescending { it.title.lowercase() }
        }

        HomeUiState.Success(filteredDocs) as HomeUiState
    }
    .catch { e -> emit(HomeUiState.Error(e.message ?: "Beklenmeyen bir hata oluştu")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            deleteDocumentUseCase(id)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    fun selectFolder(folderId: Long?) {
        selectedFolderId.value = folderId
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                createFolderUseCase(name)
            } catch (_: Exception) {}
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            if (selectedFolderId.value == folderId) {
                selectedFolderId.value = null
            }
            deleteFolderUseCase(folderId)
        }
    }

    fun moveDocumentToFolder(documentId: Long, folderId: Long?) {
        viewModelScope.launch {
            moveDocumentToFolderUseCase(documentId, folderId)
        }
    }
}
