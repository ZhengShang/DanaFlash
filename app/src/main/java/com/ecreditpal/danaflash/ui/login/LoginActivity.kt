package com.ecreditpal.danaflash.ui.login

import android.os.Bundle
import android.view.MotionEvent
import com.blankj.utilcode.util.KeyboardUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.helper.SurveyHelper

class LoginActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.login_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideToolbar()

        backClickListener = {
            SurveyHelper.addOneSurvey("/login", "loginProtocolBack")
        }

        SurveyHelper.addOneSurvey("/login", "login")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        KeyboardUtils.hideSoftInput(this)
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {
        SurveyHelper.addOneSurvey("/login", "loginBack")
        super.onDestroy()
    }
}