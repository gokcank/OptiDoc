package com.gokcank.optidoc

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.gokcank.optidoc.ui.components.InterstitialAdManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Uygulamanın giriş noktası.
 * @HiltAndroidApp, Hilt'in bağımlılık enjeksiyon grafiğini başlatır.
 */
@HiltAndroidApp
class OptiDocApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Emülatörleri resmi test cihazı olarak kaydet
        val requestConfig = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
            .build()
        MobileAds.setRequestConfiguration(requestConfig)

        // Arka planda AdMob'u başlat ve ilk geçiş reklamını önceden yükle
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@OptiDocApp) {
                InterstitialAdManager.load(this@OptiDocApp)
            }
        }
    }
}
