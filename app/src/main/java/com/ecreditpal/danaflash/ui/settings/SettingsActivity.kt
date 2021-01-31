package com.ecreditpal.danaflash.ui.settings

import android.os.Bundle
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.helper.SurveyHelper

class SettingsActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.settings_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SurveyHelper.addOneSurvey("/setting", "in")
    }
}