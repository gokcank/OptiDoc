package com.gokcank.optidoc.domain.model

/**
 * Uygulama genelinde tek ve tutarlı hata hiyerarşisi.
 *
 * [Result.failure] ile sarmalanarak repository'lerden use case'lere,
 * oradan ViewModel'lere taşınır. UI katmanı bu sınıfı switch/when
 * ile kullanıcıya uygun mesaj göstermek için kullanır.
 */
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Görsel dosyası okunamadı (URI geçersiz, izin yok, dosya silindi vb.).
     */
    class ImageNotReadable(
        imageUri: String,
        cause: Throwable? = null
    ) : AppError("Görsel okunamadı: $imageUri", cause)

    /**
     * ML Kit Text Recognition başarısız oldu (görsel formatı hatalı, model hatası vb.).
     * Görselde metin bulunamaması bu hata değil — boş String döner.
     */
    class OcrFailed(cause: Throwable) : AppError("OCR işlemi başarısız oldu", cause)

    /**
     * ML Kit Document Scanner başarısız oldu veya kullanıcı taramayı iptal etti.
     * İptal durumunu [cause] üzerinden ayırt edebilirsiniz.
     */
    class ScannerError(
        message: String = "Tarama başarısız oldu",
        cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * .txt veya .pdf dosyası üretimi başarısız oldu
     * (disk alanı yetersiz, izin hatası vb.).
     */
    class ExportFailed(cause: Throwable) : AppError("Dışa aktarma başarısız oldu", cause)

    /**
     * Room / veritabanı işlemi başarısız oldu.
     */
    class DatabaseError(cause: Throwable) : AppError("Veritabanı hatası", cause)
}
