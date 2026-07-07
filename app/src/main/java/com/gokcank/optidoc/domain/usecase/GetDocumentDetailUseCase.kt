package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * İnceleme ekranı: tek bir belgeyi sayfalarıyla birlikte Flow olarak sağlar.
 * Review ekranındaki ViewModel bu use case'i gözlemler.
 */
class GetDocumentDetailUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(
        documentId: Long
    ): Flow<Pair<ScannedDocument, List<DocumentPage>>?> =
        repository.getDocumentWithPages(documentId)
}
