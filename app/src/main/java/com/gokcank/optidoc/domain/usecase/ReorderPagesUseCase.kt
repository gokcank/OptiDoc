package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Belgenin sayfalarını yeniden sıralayan use case.
 */
class ReorderPagesUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(orderedPageIds: List<Long>) {
        orderedPageIds.forEachIndexed { index, pageId ->
            repository.updatePageNumber(pageId, index)
        }
    }
}
