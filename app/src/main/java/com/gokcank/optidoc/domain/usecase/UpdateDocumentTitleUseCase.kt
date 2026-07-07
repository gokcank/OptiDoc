package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/** Home veya Review ekranında belge başlığını günceller. */
class UpdateDocumentTitleUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: Long, title: String) =
        repository.updateTitle(documentId, title)
}
