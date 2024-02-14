package com.lyricsnigeria.lyricsnigeria

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.firebase.database.FirebaseDatabase

class MyFirebasePersistence : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}