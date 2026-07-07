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
            com.google.android.gms.ads.MobileAds.initialize(this@OptiDocApp) {}
        }
    }
}
