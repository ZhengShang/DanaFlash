package com.ecreditpal.danaflash.ui.splash

import android.os.Bundle
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity

class SplashActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.splash_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideToolbar()
    }
}