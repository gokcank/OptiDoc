package com.gokcank.optidoc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.usecase.DeleteDocumentUseCase
import com.gokcank.optidoc.domain.usecase.GetDocumentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

enum class SortOrder { DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC }

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val documents: List<ScannedDocument>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDocumentsUseCase: GetDocumentsUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    val uiState: StateFlow<HomeUiState> = combine(
        getDocumentsUseCase(),
        searchQuery,
        sortOrder
    ) { docs, query, sort ->
        var filteredDocs = docs
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filteredDocs = docs.filter { it.title.lowercase().contains(lowerQuery) }
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
}
