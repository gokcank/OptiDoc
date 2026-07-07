package com.gokcank.optidoc.di

import com.gokcank.optidoc.data.export.ExportRepositoryImpl
import com.gokcank.optidoc.data.ocr.OcrRepositoryImpl
import com.gokcank.optidoc.data.repository.DocumentRepositoryImpl
import com.gokcank.optidoc.data.scanner.ScannerRepositoryImpl
import com.gokcank.optidoc.data.storage.FileStorageRepositoryImpl
import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.ExportRepository
import com.gokcank.optidoc.domain.repository.FileStorageRepository
import com.gokcank.optidoc.domain.repository.OcrRepository
import com.gokcank.optidoc.domain.repository.ScannerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Domain arayüzlerini implementasyonlarına bağlayan Hilt modülü.
 *
 * ## Neden tüm repository'ler burada (ayrı modüllere bölünmedi)?
 * Bu modüldeki implementasyonların hiçbiri kendi başına bir `@Provides`
 * fabrika fonksiyonuna ihtiyaç duymuyor — hepsi yalnızca constructor
 * injection + `@Binds` (arayüz → implementasyon) gerektiriyor.
 * [ExportRepositoryImpl] de dahil: kendine özgü bir istemci/yapılandırma
 * nesnesi yok, yalnızca `ApplicationContext` + [DispatcherProvider] alıyor.
 * Ayrı bir modül açmak (örn. ExportModule), tek bir `@Binds` satırı için
 * gereksiz bir dosya/paket olurdu. Ayrı modül gerektiren gerçek kurulum
 * mantığı olan bağımlılıklar zaten kendi modüllerinde:
 * Room → [DatabaseModule], ML Kit istemcileri → [MlKitModule],
 * dispatcher soyutlaması → [DispatcherModule].
 *
 * ## Scope kararları — neden hepsi [@Singleton]?
 * - [DocumentRepositoryImpl]: Kendisi durumsuz (yalnızca DAO referansları
 *   taşır); singleton olması "aynı DB bağlantısının paylaşılması" için
 *   ŞART DEĞİL (bu zaten [DatabaseModule]'deki [@Singleton] AppDatabase'den
 *   geliyor — DAO'lar unscoped olsa bile hepsi aynı AppDatabase'e delege
 *   eder). Singleton seçimi yalnızca tutarlılık/ucuzluk için.
 * - [ScannerRepositoryImpl]: Hiç bağımlılığı yok, tamamen durumsuz.
 * - [OcrRepositoryImpl]: [com.gokcank.optidoc.domain.repository.OcrRepository]
 *   pahalı [com.google.mlkit.vision.text.TextRecognizer]'ı sarmalıyor
 *   (bkz. [MlKitModule] — model yükü pahalı, tekrar oluşturulmamalı);
 *   bu yüzden onu saran repository'nin de singleton olması doğal.
 * - [ExportRepositoryImpl] / [FileStorageRepositoryImpl]: Durumsuz,
 *   yalnızca Context + dispatcher taşır.
 *
 * Hiçbiri Activity/ViewModel ömrüne bağlı bir kaynak tutmuyor (örn. bir
 * Activity referansı saklamıyor), bu yüzden [@ActivityScoped] veya
 * [@ViewModelScoped] gerektiren bir bileşen yok.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindScannerRepository(
        impl: ScannerRepositoryImpl
    ): ScannerRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        impl: OcrRepositoryImpl
    ): OcrRepository

    @Binds
    @Singleton
    abstract fun bindExportRepository(
        impl: ExportRepositoryImpl
    ): ExportRepository

    @Binds
    @Singleton
    abstract fun bindFileStorageRepository(
        impl: FileStorageRepositoryImpl
    ): FileStorageRepository
}
