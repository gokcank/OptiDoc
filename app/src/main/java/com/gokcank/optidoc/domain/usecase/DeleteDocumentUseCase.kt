package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.data.storage.FileStorageManager
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.FileStorageRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Home ekranında tek bir belgeyi siler.
 *
 * Akış:
 * 1. Belgenin sayfa görsellerinin bulunduğu kalıcı dizini siler ([FileStorageManager.deleteDocumentFiles]).
 * 2. Dışa aktarılan çıktı dosyasını (TXT / PDF) siler ([FileStorageRepository]).
 * 3. Veritabanı kaydını siler.
 *
 * Dosyalardan biri silinemese bile (izin sorunu, dosya zaten yok vb.)
 * işlem duraksatılmaz — best-effort.
 */
class DeleteDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val fileStorageRepository: FileStorageRepository,
    private val fileStorageManager: FileStorageManager
) {
    suspend operator fun invoke(documentId: Long) {
        // 1. Sayfa görsellerini içeren kalıcı klasörü sil (filesDir/documents/<id>/)
        fileStorageManager.deleteDocumentFiles(documentId)

        // 2. Varsa dışa aktarılmış çıktı dosyasını sil (externalFilesDir/exports/*)
        documentRepository.getDocumentWithPages(documentId).first()?.let { (document, _) ->
            val exportUri = when (val output = document.output) {
                is DocumentOutput.DirectPdf -> output.uri
                is DocumentOutput.OcrExport -> output.uri
                is DocumentOutput.None -> null
            }
            if (exportUri != null) {
                fileStorageRepository.deleteFiles(listOf(exportUri))
            }
        }

        // 3. Veritabanı kaydını sil
        documentRepository.deleteDocument(documentId)
    }
}
