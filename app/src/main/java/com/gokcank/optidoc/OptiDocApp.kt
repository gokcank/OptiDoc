package com.gokcank.optidoc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

/**
 * Uygulamanın giriş noktası.
 * @HiltAndroidApp, Hilt'in bağımlılık enjeksiyon grafiğini başlatır.
 * AndroidManifest.xml'de android:name=".OptiDocApp" olarak tanımlanmalıdır.
 */
@HiltAndroidApp
class OptiDocApp : Application() {
    override fun onCreate() {
        super.onCreate()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
                .build()
            com.google.android.gms.ads.MobileAds.setRequestConfiguration(configuration)
            com.google.android.gms.ads.MobileAds.initialize(this@OptiDocApp) {}
        }
    }
}
