package com.ecreditpal.danaflash.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.ui.login.LoginActivity

object CommUtils {

    fun navLogin() {
        ActivityUtils.startActivity(
            Intent(
                ActivityUtils.getTopActivity(),
                LoginActivity::class.java
            )
        )
    }

    /**
     * 跳转到google商店下载页
     */
    fun navGoogleDownload(context: Context?, link: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
            setPackage(context?.packageName)
        }
        try {
            context?.startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.showLong(R.string.failed_to_donwload_in_google_store)
        }
    }
}
