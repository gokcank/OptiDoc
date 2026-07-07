package com.gokcank.optidoc.ui.scanner

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * ML Kit Document Scanner'ın Activity Result sözleşmesini Compose'a bağlar.
 *
 * Bu parça kasıtlı olarak UI katmanında kalır: [androidx.activity.compose.rememberLauncherForActivityResult]
 * yalnızca bir Activity/Fragment'in (veya Compose karşılığının) yaşam
 * döngüsüne kayıtlı olarak çalışabilir — repository/use case gibi platformdan
 * bağımsız sınıflar içinde `registerForActivityResult` çağrılamaz.
 *
 * Akış: bu composable taranan sayfaların ham [Uri]'lerini ve varsa PDF
 * [Uri]'sini [onScanSuccess] ile çağırana (tipik olarak bir ViewModel
 * fonksiyonu) iletir. Çağıran taraf bunları `Uri.toString()` ile String'e
 * çevirip [com.gokcank.optidoc.domain.repository.ScannerRepository.processScanResult]'a
 * aktarır — yani ML Kit tipleri (`Uri`, `GmsDocumentScanningResult`) bu
 * dosyanın dışına, domain katmanına hiç sızmaz.
 *
 * Örnek kullanım (ileride bir ScannerViewModel'den çağrılacak şekilde):
 * ```
 * val startScan = rememberDocumentScannerLauncher(
 *     scanner = viewModel.documentScanner,
 *     onScanSuccess = { pageUris, pdfUri ->
 *         viewModel.onScanCompleted(
 *             pageImageUris = pageUris.map { it.toString() },
 *             nativePdfUri = pdfUri?.toString()
 *         )
 *     },
 *     onScanFailure = viewModel::onScanFailed
 * )
 * Button(onClick = startScan) { Text("Tara") }
 * ```
 *
 * @param scanner ML Kit istemcisi; [com.gokcank.optidoc.di.MlKitModule] içinde
 *   Hilt'ten `@Singleton` olarak sağlanır.
 * @param onScanSuccess Taranan sayfa URI'leri ve isteğe bağlı PDF URI'si ile çağrılır.
 * @param onScanFailure Kullanıcı taramayı iptal ettiğinde veya bir hata oluştuğunda çağrılır.
 * @return Taramayı başlatan fonksiyon (bir butonun `onClick`'ine bağlanmak için).
 */
@Composable
fun rememberDocumentScannerLauncher(
    scanner: GmsDocumentScanner,
    onScanSuccess: (pageUris: List<Uri>, pdfUri: Uri?) -> Unit,
    onScanFailure: (Exception) -> Unit
): () -> Unit {
    val activity = LocalContext.current as Activity
    val currentOnScanSuccess by rememberUpdatedState(onScanSuccess)
    val currentOnScanFailure by rememberUpdatedState(onScanFailure)

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        val data = activityResult.data
        if (activityResult.resultCode != Activity.RESULT_OK || data == null) {
            currentOnScanFailure(
                IllegalStateException(
                    "Tarama iptal edildi veya başarısız oldu (resultCode=${activityResult.resultCode})"
                )
            )
            return@rememberLauncherForActivityResult
        }

        val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
        if (scanningResult == null) {
            currentOnScanFailure(IllegalStateException(activity.getString(com.gokcank.optidoc.R.string.error_scan_failed)))
            return@rememberLauncherForActivityResult
        }
        val pageUris = scanningResult.pages?.map { it.imageUri } ?: emptyList()
        val pdfUri = scanningResult.pdf?.uri
        currentOnScanSuccess(pageUris, pdfUri)
    }

    return remember(scanner) {
        {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender ->
                    activityResultLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                .addOnFailureListener { exception ->
                    currentOnScanFailure(exception)
                }
        }
    }
}
