package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.model.Folder
import com.gokcank.optidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Tüm klasörleri getiren use case.
 */
class GetFoldersUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Folder>> = repository.getAllFolders()
}
