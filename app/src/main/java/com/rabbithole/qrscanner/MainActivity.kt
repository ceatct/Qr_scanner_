package com.rabbithole.qrscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.rabbithole.qrscanner.databinding.ActivityMainBinding
import com.rabbithole.qrscanner.fragments.CreateFragment
import com.rabbithole.qrscanner.fragments.ScanFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private companion object{
        private const val TAG = "BANNER_AD_TAG"
    }

    private var adView : AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(ScanFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.create -> replaceFragment(CreateFragment())
                R.id.scan -> replaceFragment(ScanFragment())
                else -> {}
            }
            true
        }

        MobileAds.initialize(this){
            Log.d(TAG, "onInitializeComplete:")
        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("", ""))
                .build()
        )

        adView = findViewById(R.id.bannerAd)

        val adRequest = AdRequest.Builder().build()

        adView?.loadAd(adRequest)

        adView?.adListener = object : AdListener(){
            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked: ")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG, "onAdClosed: ")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(TAG, "onAdImpression: ")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded: ")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Log.d(TAG, "onAdOpened: ")
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                Log.d(TAG, "onAdSwipeGestureClicked: ")
            }
        }

    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()

    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        adView?.resume()
        super.onResume()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

}