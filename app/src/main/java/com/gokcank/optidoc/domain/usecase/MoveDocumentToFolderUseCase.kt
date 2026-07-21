package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Belgeyi bir klasöre taşıyan veya klasörden çıkaran use case.
 */
class MoveDocumentToFolderUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: Long, folderId: Long?) {
        repository.moveToFolder(documentId, folderId)
    }
}
