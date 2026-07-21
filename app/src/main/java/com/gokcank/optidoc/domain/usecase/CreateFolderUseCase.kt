package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Yeni bir klasör oluşturan use case.
 */
class CreateFolderUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(name: String): Long {
        require(name.isNotBlank()) { "Klasör adı boş olamaz" }
        return repository.createFolder(name.trim())
    }
}
