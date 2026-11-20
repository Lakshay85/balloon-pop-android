package com.midnighttales.balloonpop

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Single-activity WebView wrapper for the Balloon Pop game.
 *
 * The game (assets/www/index.html) talks to this class through the
 * "AndroidAds" JavascriptInterface:
 *   AndroidAds.isReady()        -> "true"/"false"
 *   AndroidAds.preload()        -> start loading the rewarded interstitial
 *   AndroidAds.showRewardedAd() -> show it; the result goes back to JS via
 *                                  window.BalloonAds.onRewarded(json)
 * json is one of:
 *   {"status":"earned"}         user finished the ad, grant the revive
 *   {"status":"dismissed"}      closed before the reward
 *   {"status":"not_available"}  no ad loaded yet
 *   {"status":"error"}          load/show failure
 */
class MainActivity : Activity() {

    private lateinit var webView: WebView
    private var rewardedAd: RewardedInterstitialAd? = null
    private var adLoading = false
    private var rewardEarned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient() // keep navigation inside the WebView
        webView.addJavascriptInterface(AdsJsInterface(), "AndroidAds")
        webView.loadUrl("file:///android_asset/www/index.html")

        // Google recommends initializing the SDK off the main thread.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            launch(Dispatchers.Main) { loadAd() }
        }
    }

    private fun loadAd() {
        if (adLoading || rewardedAd != null) return
        adLoading = true
        RewardedInterstitialAd.load(
            this,
            BuildConfig.ADMOB_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    adLoading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    adLoading = false
                    rewardedAd = null
                }
            }
        )
    }

    private fun showAd() {
        val ad = rewardedAd
        if (ad == null) {
            sendAdStatus("not_available")
            loadAd() // kick off a load so the next tap can succeed
            return
        }
        rewardedAd = null
        rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                sendAdStatus(if (rewardEarned) "earned" else "dismissed")
                loadAd() // preload the next one
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                sendAdStatus("error")
                loadAd()
            }
        }
        ad.show(this) {
            // User earned the reward. We report it when the ad closes so the
            // game only resumes once it is visible again.
            rewardEarned = true
        }
    }

    private fun sendAdStatus(status: String) {
        val js = "window.BalloonAds && window.BalloonAds.onRewarded('{\"status\":\"$status\"}')"
        webView.evaluateJavascript(js, null)
    }

    inner class AdsJsInterface {
        @JavascriptInterface
        fun isReady(): String = (rewardedAd != null).toString()

        @JavascriptInterface
        fun preload() {
            runOnUiThread { loadAd() }
        }

        @JavascriptInterface
        fun showRewardedAd() {
            runOnUiThread { showAd() }
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
