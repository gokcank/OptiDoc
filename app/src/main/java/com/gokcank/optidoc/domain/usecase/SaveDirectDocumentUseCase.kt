package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.data.storage.FileStorageManager
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.model.ScanResult
import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Kullanıcı "OCR uygulamadan kaydet" yolunu seçtiğinde çalışır.
 *
 * Akış:
 * 1. Belgeyi kaydeder (başlık + createdAt).
 * 2. ML Kit cache URI'lerini kalıcı depolamaya kopyalar.
 * 3. Sayfaları (kalıcı görsel URI'leriyle) kaydeder.
 * 4. ML Kit'in native PDF URI'sini kalıcıya kopyalar ve belge çıktısı olarak işler.
 */
class SaveDirectDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
    private val fileStorageManager: FileStorageManager
) {
    suspend operator fun invoke(
        title: String,
        scanResult: ScanResult,
        createdAt: Long = System.currentTimeMillis()
    ): Result<Long> = runCatching {
        // 1. Belgeyi kaydet
        val documentId = repository.saveDocument(
            ScannedDocument(
                title = title,
                createdAt = createdAt,
                pageCount = scanResult.pageImageUris.size
            )
        )

        // 2. Sayfa görsellerini kalıcı depolamaya kopyala
        val permanentImageUris = fileStorageManager.copyPageImages(documentId, scanResult.pageImageUris)

        // 3. Sayfaları kaydet
        val pages = permanentImageUris.mapIndexed { index, uri ->
            DocumentPage(documentId = documentId, pageNumber = index, imageUri = uri)
        }
        repository.savePages(pages)

        // 4. Native PDF'i kalıcıya kopyala ve çıktıyı güncelle
        val pdfUri = requireNotNull(scanResult.nativePdfUri) {
            "Direkt kaydetme yolu için nativePdfUri zorunludur."
        }
        val permanentPdfUri = fileStorageManager.copyNativePdf(documentId, pdfUri)
        repository.updateOutput(
            id = documentId,
            output = DocumentOutput.DirectPdf(permanentPdfUri),
            pageCount = pages.size
        )

        documentId
    }
}
