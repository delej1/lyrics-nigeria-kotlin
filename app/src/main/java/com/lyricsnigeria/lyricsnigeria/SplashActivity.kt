package com.lyricsnigeria.lyricsnigeria

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.FirebaseDatabase


class SplashActivity : AppCompatActivity() {
    private val mWaitHandler = Handler()//create value mWaitHandler

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
        } else {
            if(BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME){
                setTheme(R.style.NightTheme)
            }else{
                setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_logo_screen)

        mWaitHandler.postDelayed({

            try {
                val intent = Intent(this, BottomNavActivity::class.java)//to go to desired Activity
                startActivity(intent)//to start desired Activity
                finish() //to finish SplashActivity so it doesn't show onBackPress
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
        }, 3000)//3 seconds delay
    }
    override fun onDestroy() {
        super.onDestroy()

        mWaitHandler.removeCallbacksAndMessages(null)//removes callbacks so activity doesn't show after app close
    }
}
