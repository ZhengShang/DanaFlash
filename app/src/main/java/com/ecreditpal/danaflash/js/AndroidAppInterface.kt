package com.ecreditpal.danaflash.js

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.ui.camera.CameraActivity

class AndroidAppInterface(private val context: Context) {
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun launchOCR(json: String) {
        CameraActivity.start(context, CameraActivity.MODE_OCR)
    }

    @JavascriptInterface
    fun launchLiveness(json: String) {
        CameraActivity.start(context, CameraActivity.MODE_FACE_RECOGNITION)
    }

    @JavascriptInterface
    fun launchContact(json: String) {

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