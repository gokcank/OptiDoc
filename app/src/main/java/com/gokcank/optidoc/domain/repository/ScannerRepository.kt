package com.gokcank.optidoc.domain.repository

import com.gokcank.optidoc.domain.model.ScanResult

/**
 * ML Kit Document Scanner ile tarama işleminin sözleşmesi.
 *
 * Activity Result kısmı Compose/Activity katmanında kalır (ML Kit Document Scanner,
 * IntentSenderRequest üzerinden çalışır). Bu arayüz yalnızca Activity Result'tan
 * parse edilen ham URI verilerini domain modeline dönüştürür.
 *
 * Implementasyon [data.scanner.ScannerRepositoryImpl] içindedir. Activity Result
 * tarafının UI'da nasıl başlatıldığı için bkz. [ui.scanner.rememberDocumentScannerLauncher].
 */
interface ScannerRepository {

    /**
     * Activity Result'tan gelen ham sayfa görseli URI'lerini ve
     * isteğe bağlı birleşik PDF URI'sini domain [ScanResult] modeline dönüştürür.
     *
     * @param pageImageUris ML Kit'in ürettiği sayfa JPEG URI listesi.
     * @param nativePdfUri  ML Kit'in ürettiği birleşik PDF URI'si (null olabilir).
     */
    suspend fun processScanResult(
        pageImageUris: List<String>,
        nativePdfUri: String?
    ): Result<ScanResult>
}
