package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.data.storage.FileStorageManager
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.OcrRepository
import javax.inject.Inject

/**
 * Kullanıcı "OCR uygula" yolunu seçtiğinde çalışır.
 *
 * Her sayfada sırayla OCR çalıştırır ve sonuçları veritabanına yazar.
 * Kayıt öncesinde ML Kit cache URI'lerini kalıcı depolamaya kopyalar.
 *
 * Sayfalar kasıtlı olarak sırayla (paralel değil) işlenir — bkz. [OcrRepository]
 * implementasyonundaki [com.gokcank.optidoc.di.MlKitModule] scope notu.
 */
class RunOcrUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val ocrRepository: OcrRepository,
    private val fileStorageManager: FileStorageManager
) {
    /**
     * @param title       Belge başlığı.
     * @param pageImageUris Tarama use case'inden gelen ham sayfa URI listesi.
     * @param createdAt   Oluşturma zamanı (epoch millis).
     * @return Başarıda (documentId, sayfalar) çifti; hata durumunda [Result.failure].
     */
    suspend operator fun invoke(
        title: String,
        pageImageUris: List<String>,
        createdAt: Long = System.currentTimeMillis()
    ): Result<Pair<Long, List<DocumentPage>>> = runCatching {
        // 1. Belgeyi kaydet
        val documentId = documentRepository.saveDocument(
            ScannedDocument(
                title = title,
                createdAt = createdAt,
                pageCount = pageImageUris.size
            )
        )

        // 2. Sayfa görsellerini kalıcı depolamaya kopyala
        val permanentUris = fileStorageManager.copyPageImages(documentId, pageImageUris)

        // 3. Her sayfada OCR çalıştır, sonuçlarla birlikte kaydet
        val pages = permanentUris.mapIndexed { index, uri ->
            val ocrText = ocrRepository.recognizeText(uri).getOrNull()
            DocumentPage(
                documentId = documentId,
                pageNumber = index,
                imageUri = uri,
                ocrText = ocrText
            )
        }
        documentRepository.savePages(pages)

        documentId to pages
    }
}
