package com.gokcank.optidoc.ui.components

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.gokcank.optidoc.BuildConfig

private const val GOOGLE_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
private const val COOLDOWN_MILLIS = 120_000L // 2 dakika sıklık koruması

/**
 * AdMob Geçiş Reklamı (Interstitial) Yöneticisi.
 *
 * - Kullanıcı akışını asla kilitlemez; reklam yoksa veya cooldown içindeyse anında onDismiss çağrılır.
 * - Sıklık koruması (cooldown) ile reklam yorgunluğunu önler.
 * - Debug ve boş ID durumlarında resmi Google Test Reklam ID'sini kullanır.
 */
object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShownTime = 0L

    private val adUnitId: String
        get() = if (BuildConfig.DEBUG) {
            GOOGLE_TEST_INTERSTITIAL_ID
        } else {
            BuildConfig.ADMOB_INTERSTITIAL_ID.ifBlank { GOOGLE_TEST_INTERSTITIAL_ID }
        }

    fun load(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showIfReady(activity: Activity, onDismiss: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val ad = interstitialAd

        if (ad != null && (now - lastShownTime >= COOLDOWN_MILLIS)) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastShownTime = System.currentTimeMillis()
                    load(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    load(activity)
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            if (interstitialAd == null) {
                load(activity)
            }
            onDismiss()
        }
    }
}
