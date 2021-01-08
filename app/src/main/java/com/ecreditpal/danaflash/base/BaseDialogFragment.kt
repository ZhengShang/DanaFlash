package com.ecreditpal.danaflash.base

import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

open class BaseDialogFragment : AppCompatDialogFragment() {

    fun show(manager: FragmentManager) {
        show(manager, this::class.java.simpleName)
    }
}