package com.ecreditpal.danaflash.ui.detail

import android.os.Bundle
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.helper.SurveyHelper

class ProductActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.product_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SurveyHelper.addOneSurvey("/apiProductDetail", "in")

        backClickListener = {
            SurveyHelper.addOneSurvey("/apiProductDetail", "back")
        }
    }
}