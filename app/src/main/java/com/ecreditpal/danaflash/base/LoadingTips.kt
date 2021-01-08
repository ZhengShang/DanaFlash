package com.ecreditpal.danaflash.base

import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ActivityUtils

object LoadingTips {

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog.newInstance()
    }

    fun showLoading() {
        val fa = ActivityUtils.getTopActivity() as? FragmentActivity ?: return
        loadingDialog.show(fa.supportFragmentManager)
    }

    fun dismissLoading() {
        loadingDialog.dismiss()
    }

}

