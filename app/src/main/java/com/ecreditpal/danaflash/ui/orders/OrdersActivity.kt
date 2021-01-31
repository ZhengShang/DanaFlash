package com.ecreditpal.danaflash.ui.orders

import android.os.Bundle
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.helper.SurveyHelper

class OrdersActivity : BaseNavActivity() {
    override fun navGraphId() = R.navigation.orders_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SurveyHelper.addOneSurvey("/orderPage", "in")
    }
}