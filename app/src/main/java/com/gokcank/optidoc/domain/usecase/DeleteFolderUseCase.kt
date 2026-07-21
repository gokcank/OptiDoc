package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Klasör silen use case.
 */
class DeleteFolderUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteFolder(id)
    }
}
