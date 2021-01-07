package com.ecreditpal.danaflash.helper

import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.Navigation

fun View?.nav(@IdRes id: Int) {
    if (this == null) return
    setOnClickListener {
        Navigation.findNavController(this)
            .navigate(id)
    }
}