package com.ecreditpal.danaflash.ui.contact

import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity

class ContactActivity : BaseNavActivity() {

    override fun navGraphId() = R.navigation.contact_navigation

    companion object {
        const val EXTRA_CHOSEN_RESULT = "extra_chosen_result"
        const val EXTRA_CHOSEN_CONTACT = "extra_chosen_contact"
    }
}