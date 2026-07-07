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

    val uiState: StateFlow<HomeUiState> = getDocumentsUseCase()
        .map { docs -> HomeUiState.Success(docs) as HomeUiState }
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
}
