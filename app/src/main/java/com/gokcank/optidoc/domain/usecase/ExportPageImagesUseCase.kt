package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.ExportRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Bir belgenin sayfa görsellerini JPEG olarak ortak depolamaya aktarır.
 *
 * Detail ekranından çağrılır; hem OCR uygulanmış hem de direkt kaydedilmiş
 * belgeler için geçerlidir — her zaman sayfa görselleri mevcuttur.
 *
 * @return Başarıda oluşturulan JPEG URI listesi.
 */
class ExportPageImagesUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val exportRepository: ExportRepository
) {
    suspend operator fun invoke(documentId: Long): Result<List<String>> {
        val pair = documentRepository.getDocumentWithPages(documentId).first()
            ?: return Result.failure(IllegalArgumentException("Belge bulunamadı: $documentId"))
        val (document, pages) = pair
        val sortedUris = pages.sortedBy { it.pageNumber }.map { it.imageUri }
        return exportRepository.exportImages(
            documentId = documentId,
            pageImageUris = sortedUris,
            title = document.title
        )
    }
}
