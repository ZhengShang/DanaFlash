package com.ecreditpal.danaflash.ui.login

import android.os.Bundle
import android.view.MotionEvent
import com.blankj.utilcode.util.KeyboardUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity

class LoginActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.login_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideToolbar()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        KeyboardUtils.hideSoftInput(this)
        return super.onTouchEvent(event)
    }
}