package com.gokcank.optidoc.domain.model

import android.graphics.Bitmap

enum class SignaturePosition {
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER
}

/**
 * Belgelere (PDF veya Görsel) eklenecek dijital imza konfigürasyonu.
 */
data class SignatureOptions(
    val bitmap: Bitmap? = null,
    val position: SignaturePosition = SignaturePosition.BOTTOM_RIGHT,
    val scale: Float = 0.45f,
    val opacity: Float = 1.0f
)
