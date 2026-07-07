package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.ExportRepository
import javax.inject.Inject

/**
 * Kullanıcı format seçip "Dışa Aktar" dediğinde çalışır.
 *
 * 1. Metni seçilen formatta dosyaya yazar ([ExportRepository]).
 * 2. Belge çıktısını günceller ([DocumentRepository.updateOutput]).
 */
class ExportDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val exportRepository: ExportRepository
) {
    /**
     * @param documentId  İşlenecek belgenin id'si.
     * @param text        Tüm sayfaların birleştirilmiş veya son düzenlenmiş metni.
     * @param format      Kullanıcının seçtiği format.
     * @param pageCount   Güncel sayfa sayısı (output update için).
     * @param title       Dışa aktarılacak dosyanın adı (belge başlığından türetilir).
     * @return Başarıda üretilen dosyanın Uri'si.
     */
    suspend operator fun invoke(
        documentId: Long,
        text: String,
        format: ExportFormat,
        pageCount: Int,
        title: String = "document_$documentId"
    ): Result<String> = runCatching {
        // 1. Dosyayı üret
        val uri = exportRepository.export(documentId, text, format, title).getOrThrow()

        // 2. DB'yi güncelle
        documentRepository.updateOutput(
            id = documentId,
            output = DocumentOutput.OcrExport(format = format, uri = uri),
            pageCount = pageCount
        )

        uri
    }
}
