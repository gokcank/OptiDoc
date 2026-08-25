package com.gokcank.optidoc.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.gokcank.optidoc.BuildConfig

private const val GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

/**
 * Modern Compose AdMob Banner Bileşeni.
 * Debug ortamında veya boş ID durumunda zorunlu olarak resmi Google Test ID'sini kullanır.
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = if (BuildConfig.DEBUG) GOOGLE_TEST_BANNER_ID else BuildConfig.ADMOB_BANNER_ID.ifBlank { GOOGLE_TEST_BANNER_ID }
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
