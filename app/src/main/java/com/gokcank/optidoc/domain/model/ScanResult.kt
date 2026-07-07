package com.gokcank.optidoc.domain.model

/**
 * ML Kit Document Scanner'dan gelen ham tarama sonucu.
 *
 * @property pageImageUris Kullanıcının kenar ayarı yaptığı her sayfa için
 *                         ML Kit'in ürettiği JPEG görsellerinin Uri listesi.
 * @property nativePdfUri  ML Kit'in tüm sayfaları birleştirdiği PDF'in Uri'si;
 *                         yalnızca direkt kaydetme yolunda kullanılır.
 *                         Null olabilir (yapılandırmaya bağlı).
 */
data class ScanResult(
    val pageImageUris: List<String>,
    val nativePdfUri: String?
)
