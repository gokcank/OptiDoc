package com.gokcank.optidoc.domain.repository

/**
 * ML Kit Text Recognition ile görsellerden metin çıkarma işleminin sözleşmesi.
 *
 * Implementasyon [data.ocr.OcrRepositoryImpl] içindedir; Task tabanlı ML Kit
 * API'si `.await()` ile suspend fonksiyona dönüştürülür.
 */
interface OcrRepository {

    /**
     * Verilen görsel URI'sindeki metni tanır.
     *
     * @param imageUri Sayfanın JPEG görselinin Uri'si.
     * @return Başarıda tanınan metin; hata durumunda [Result.failure].
     *         Görselde metin bulunamazsa boş String döner.
     */
    suspend fun recognizeText(imageUri: String): Result<String>
}
