package com.ecreditpal.danaflash.helper

import android.content.Intent
import com.blankj.utilcode.util.ActivityUtils
import com.ecreditpal.danaflash.ui.login.LoginActivity

object CommUtils {

    fun navLogin() {
        ActivityUtils.startActivity(
            Intent(
                ActivityUtils.getTopActivity(),
                LoginActivity::class.java
            )
        )
    }
}
