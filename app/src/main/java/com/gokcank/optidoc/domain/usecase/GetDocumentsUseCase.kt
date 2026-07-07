package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Ana sayfa: geçmiş taramaların listesini Flow olarak sağlar.
 * ViewModel doğrudan bu use case'i gözlemler.
 */
class GetDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<ScannedDocument>> = repository.getDocuments()
}
