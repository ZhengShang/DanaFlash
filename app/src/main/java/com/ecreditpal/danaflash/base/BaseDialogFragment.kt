package com.ecreditpal.danaflash.base

import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

open class BaseDialogFragment : AppCompatDialogFragment() {

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, this::class.java.simpleName)
    }
}