package com.gokcank.optidoc.di

import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ML Kit istemcilerini Hilt grafiğine sağlayan modül.
 *
 * ## Scope kararları
 * - [TextRecognizer] → [Singleton]: Arka planda bir tanıma modeli tutar; ilk
 *   `process()` çağrısında model belleğe yüklenir ve nesne yaşadığı sürece
 *   orada kalır. Her OCR isteğinde yeniden oluşturmak modeli tekrar tekrar
 *   yüklemek anlamına gelir, bu yüzden uygulama boyunca tek örnek paylaşılır.
 *   NOT (varsayım/doğrulanmadı): Resmi dokümantasyon aynı [TextRecognizer]
 *   örneğine eşzamanlı (paralel) `process()` çağrıları için açık bir
 *   thread-safety garantisi vermiyor; yalnızca farklı dil seçenekli birden
 *   çok *örnek*'in eşzamanlı kullanımının performansı düşürebileceği
 *   belirtiliyor. Sıralı (sequential) yeniden kullanım kesin olarak
 *   desteklenen/önerilen kullanım şekli. Bu yüzden `RunOcrUseCase` sayfaları
 *   sırayla işliyor; birden çok sayfayı paralel OCR'lamak istenirse önce bu
 *   varsayım doğrulanmalı.
 * - [GmsDocumentScannerOptions] / [GmsDocumentScanner] → [Singleton]: İstemcinin
 *   kendisi yalnızca yapılandırmayı taşır; `getStartScanIntent(activity)`
 *   çağrısı her seferinde Activity referansını parametre olarak alır, bu
 *   yüzden istemcinin kendisini Activity'ye bağlı tutmaya gerek yoktur.
 */
@Module
@InstallIn(SingletonComponent::class)
object MlKitModule {

    @Provides
    @Singleton
    fun provideTextRecognizer(): TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @Provides
    @Singleton
    fun provideDocumentScannerOptions(): GmsDocumentScannerOptions =
        GmsDocumentScannerOptions.Builder()
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .build()

    @Provides
    @Singleton
    fun provideGmsDocumentScanner(
        options: GmsDocumentScannerOptions
    ): GmsDocumentScanner = GmsDocumentScanning.getClient(options)
}
