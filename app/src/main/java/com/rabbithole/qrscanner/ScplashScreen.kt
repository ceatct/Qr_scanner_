package com.rabbithole.qrscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer

class ScplashScreen : AppCompatActivity() {

    private var secondsRemaining: Long = 0

    private companion object{
        private const val TAG = "SPLASH_TAG"
        private const val COUNTER_TIMER: Long = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scplash_screen)

        createTimer(COUNTER_TIMER)

    }

    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000,1000){
            override fun onTick(millisecondsFinished: Long) {
                secondsRemaining = millisecondsFinished / 1000+1
            }

            override fun onFinish() {
                secondsRemaining = 0

                val application = application
                if(application !is MyApplication){
                    startMainActivity()
                    return
                }
                application.showAdIfAvailable(this@ScplashScreen,
                    object : MyApplication.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            startMainActivity()
                        }
                    })
            }
        }
        countDownTimer.start()
    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}