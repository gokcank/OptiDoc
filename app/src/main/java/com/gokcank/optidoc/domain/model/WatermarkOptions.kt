package com.gokcank.optidoc.domain.model

/**
 * PDF dışa aktarımında sayfaya eklenecek metin tabanlı filigran ayarları.
 */
data class WatermarkOptions(
    val text: String = "GİZLİ",
    val opacity: Float = 0.55f,
    val rotationDegrees: Float = -45f,
    val textSizeSp: Float = 48f,
    val colorHex: String = "#444444"
)
