package com.ecreditpal.danaflash.js

import android.webkit.JavascriptInterface
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.ui.comm.WebActivity

class AndroidAppInterface(private val webActivity: WebActivity) {

    @JavascriptInterface
    fun launchOCR(json: String?) {
        webActivity.startOcrPage(json)
    }

    @JavascriptInterface
    fun launchLiveness(json: String?) {
        webActivity.startLiveness(json)
    }

    @JavascriptInterface
    fun launchContact(json: String) {
        webActivity.startContact()
    }

    @JavascriptInterface
    fun uploadContactList() {

    }

    @JavascriptInterface
    fun getDeviceId(): String {
        return UserFace.deviceId
    }

    @JavascriptInterface
    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    @JavascriptInterface
    fun getVersionCode(): String {
        return BuildConfig.VERSION_CODE.toString()
    }

    @JavascriptInterface
    fun getAppList() {

    }

    @JavascriptInterface
    fun getLocation() {

    }

    @JavascriptInterface
    fun getAllDeviceInfo() {

    }

    @JavascriptInterface
    fun getReferer() {

    }

    @JavascriptInterface
    fun getAfInstallConversionData() {

    }

    @JavascriptInterface
    fun generalCheckPermission() {

    }

    @JavascriptInterface
    fun generalRequestPermission() {

    }

    @JavascriptInterface
    fun checkLocationService() {

    }

    @JavascriptInterface
    fun goToLocationService() {

    }
}