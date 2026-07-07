package com.gokcank.optidoc.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.gokcank.optidoc.BuildConfig

object InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private val adUnitId = BuildConfig.ADMOB_INTERSTITIAL_ID
    private var isAdLoading = false

    fun loadAd(context: Context) {
        if (mInterstitialAd != null || isAdLoading) {
            return
        }
        
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdManager", adError.toString())
                    mInterstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAdManager", "Ad was dismissed.")
                    mInterstitialAd = null
                    onAdDismissed()
                    // Preload the next ad
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("InterstitialAdManager", "Ad failed to show.")
                    mInterstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("InterstitialAdManager", "Ad showed fullscreen content.")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "The interstitial ad wasn't ready yet.")
            onAdDismissed()
            // Try loading for next time
            loadAd(activity)
        }
    }
}
