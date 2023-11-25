package com.rabbithole.qrscanner

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class MyApplication: Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var appOpenAdManager: AppOpenAdManager? = null
    private var currentActivity: Activity? = null

    private companion object{
        private const val TAG = "MY_APPLICATION_TAG"
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        MobileAds.initialize(this){

        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("", ""))
                .build()
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()

    }

    interface OnShowAdCompleteListener{
        fun onShowAdComplete()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onMoveToForeGround(){
        appOpenAdManager!!.showAdIfAvailable(currentActivity!!)
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        Log.d(TAG, "onActivityCreated: ")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, "onActivityStarted: ")
        if(!appOpenAdManager!!.isShowingAd){
            currentActivity = activity
        }
    }

    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener){
        appOpenAdManager!!.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "onActivityResumed: ")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "onActivityPaused: ")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d(TAG, "onActivityStopped: ")
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        Log.d(TAG, "onActivitySaveInstanceState: ")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "onActivityDestroyed: ")
    }

    private inner class AppOpenAdManager{
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        private var loadTime: Long = 0

        private fun loadAd(context: Context){
            if(isLoadingAd || isAdAvailable()){
                return
            }
            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                "",
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object: AppOpenAd.AppOpenAdLoadCallback(){
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                    }

                    override fun onAdLoaded(ad: AppOpenAd) {
                        super.onAdLoaded(ad)
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }
                }
            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long):Boolean{
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable():Boolean{
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(activity:Activity){
            showAdIfAvailable(activity,
                object: OnShowAdCompleteListener{
                    override fun onShowAdComplete() {
                        Log.d(TAG, "onShowAdComplete: ")
                    }
                })
        }

        fun showAdIfAvailable(activity:Activity, onShowAdCompleteListener: OnShowAdCompleteListener){
            if(isShowingAd){return}

            if(!isAdAvailable()){
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }
            }
            isShowingAd = true
            appOpenAd!!.show(activity)
        }

    }

}
