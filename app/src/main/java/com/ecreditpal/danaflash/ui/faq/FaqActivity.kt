package com.ecreditpal.danaflash.ui.faq

import android.os.Bundle
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.helper.SurveyHelper

class FaqActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.faq_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SurveyHelper.addOneSurvey("/questions", "in")
    }
}