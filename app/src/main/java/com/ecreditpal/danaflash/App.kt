package com.ecreditpal.danaflash

import android.app.Application
import android.content.Context
import com.ecreditpal.danaflash.data.UserFace

/**
 * Created by shang on 2021/1/9.
 *            🐳🐳🐳🍒           22:07 🥥
 */
class App : Application() {
    companion object {
        private val devKey = "qrdZGj123456789"

        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        UserFace.initData()
    }
}