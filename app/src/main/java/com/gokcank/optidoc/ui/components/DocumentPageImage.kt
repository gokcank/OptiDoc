package com.gokcank.optidoc.ui.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * URI formatından bağımsız resim yükleyici.
 *
 * Veritabanında kayıtlı URI'ler farklı formatlarda olabilir:
 * - Eski format: `file:/data/user/0/.../page_1.jpg`  (tek eğik çizgi — Java toURI())
 * - Yeni format: `file:///data/user/0/.../page_1.jpg` (üç eğik çizgi — Android Uri.fromFile())
 * - ML Kit çıktısı: `content://...` (içerik sağlayıcı)
 *
 * Coil `file:/` (tek eğik çizgi) biçimini desteklemez; bu yüzden
 * `file:` URI'lerini her zaman `File` nesnesine dönüştürüp veriyoruz.
 */
@Composable
fun DocumentPageImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current

    val imageData: Any = when {
        imageUri.startsWith("file:") -> {
            // file: URI'yi File nesnesine çevir — tek veya üç eğik çizgi fark etmeksizin
            val parsed = Uri.parse(imageUri)
            val path = parsed.path
            if (path != null) File(path) else imageUri
        }
        else -> imageUri // content:// veya diğer formatlar
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageData)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
