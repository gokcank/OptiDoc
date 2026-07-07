package com.gokcank.optidoc.data.scanner

import com.gokcank.optidoc.domain.model.AppError
import com.gokcank.optidoc.domain.model.ScanResult
import com.gokcank.optidoc.domain.repository.ScannerRepository
import javax.inject.Inject

/**
 * [ScannerRepository] implementasyonu.
 *
 * ML Kit Document Scanner'ın Activity Result akışı (IntentSender başlatma,
 * `GmsDocumentScanningResult.fromActivityResultIntent` ile parse) yalnızca
 * Activity/Compose yaşam döngüsüne kayıtlı bir launcher üzerinden çalışabilir;
 * bkz. [com.gokcank.optidoc.ui.scanner.rememberDocumentScannerLauncher].
 * Bu sınıf, UI'dan zaten parse edilmiş halde gelen ham URI'leri doğrulayıp
 * domain modeline taşımaktan sorumludur.
 */
class ScannerRepositoryImpl @Inject constructor() : ScannerRepository {

    override suspend fun processScanResult(
        pageImageUris: List<String>,
        nativePdfUri: String?
    ): Result<ScanResult> {
        if (pageImageUris.isEmpty()) {
            return Result.failure(
                AppError.ScannerError(message = "Taramada hiç sayfa bulunamadı")
            )
        }

        return Result.success(
            ScanResult(
                pageImageUris = pageImageUris,
                nativePdfUri = nativePdfUri
            )
        )
    }
}
