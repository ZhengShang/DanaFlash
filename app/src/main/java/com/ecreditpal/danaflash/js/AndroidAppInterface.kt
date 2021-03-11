package com.ecreditpal.danaflash.js

import DataStoreKeys
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.webkit.JavascriptInterface
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.*
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.DeviceInfoUtil
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.model.AppInfoModel
import com.ecreditpal.danaflash.ui.comm.WebActivity
import com.ecreditpal.danaflash.worker.UploadContactsWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


@Suppress("unused")
class AndroidAppInterface(private val webActivity: WebActivity) {

    @JavascriptInterface
    fun toast(str: String) {
        LogUtils.i("Toast: $str")
        ToastUtils.showShort(str)
    }

    @JavascriptInterface
    @JvmOverloads
    fun launchOCR(json: String? = null) {
        webActivity.startOcrPage(json)
    }

    @JavascriptInterface
    @JvmOverloads
    fun launchLiveness(json: String? = null) {
        webActivity.startLiveness(json)
    }

    @JavascriptInterface
    fun launchContact() {
        webActivity.startContact()
    }

    @JavascriptInterface
    fun uploadContactList() {
        val uploadWorkRequest =
            OneTimeWorkRequest.Builder(UploadContactsWorker::class.java)
                .build()

        webActivity.lifecycleScope.launch(Dispatchers.Main) {
            WorkManager.getInstance(webActivity).getWorkInfoByIdLiveData(uploadWorkRequest.id)
                .observe(webActivity) {
                    val result = if (it.state == WorkInfo.State.SUCCEEDED) "1" else "0"
                    webActivity.callbackInterface("uploadContactList", result)
                }
        }

        WorkManager
            .getInstance(webActivity)
            .enqueue(uploadWorkRequest)
    }

    @JavascriptInterface
    fun getDeviceId() {
        webActivity.callbackInterface("getDeviceId", UserFace.gaid)
    }

    @JavascriptInterface
    fun getVersionName() {
        webActivity.callbackInterface("getVersionName", BuildConfig.VERSION_NAME)
    }

    @JavascriptInterface
    fun getVersionCode() {
        webActivity.callbackInterface("getVersionCode", BuildConfig.VERSION_CODE.toString())
    }

    @JavascriptInterface
    fun getAppList() {
        val result = try {
            val pm = Utils.getApp().packageManager
            val list = pm.getInstalledPackages(0).map {
                AppInfoModel(
                    appName = it.applicationInfo.loadLabel(pm).toString(),
                    packageName = it.packageName,
                    versionName = it.versionName,
                    versionCode = it.versionCode,
                    firstInstall = it.firstInstallTime,
                    lastInstall = it.lastUpdateTime,
                    flag = it.applicationInfo.flags
                )
            }
            val json = """
                {
                    "packageInfo" : [${JSON.toJSONString(list)}]
                }
            """.trimIndent()
            EncodeUtils.base64Encode2String(json.toByteArray())
        } catch (e: Exception) {
            LogUtils.e(e)
            ""
        }
        webActivity.callbackInterface("getAppList", result)
    }

    @SuppressLint("MissingPermission")
    @JavascriptInterface
    fun getLocation() {

        val success = if (arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ).all { permission ->
                ContextCompat.checkSelfPermission(
                    webActivity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            1
        } else {
            0
        }

        val map = mapOf(
            "deviceId" to UserFace.deviceId,
            "imei" to PhoneUtils.getIMEI(),
            "latitude" to (UserFace.location?.latitude ?: 0.0),
            "longitude" to (UserFace.location?.longitude ?: 0.0),
            "mac" to DeviceUtils.getMacAddress(),
            "root" to if (AppUtils.isAppRoot()) 1 else 0,
            "debug" to if (AppUtils.isAppDebug()) 1 else 0,
            "gpsFake" to if (UserFace.location?.isFromMockProvider == true) 1 else 0,
            "success" to success
        )
        webActivity.callbackInterface("getLocation", JSON.toJSONString(map))
    }

    @JavascriptInterface
    fun getAllDeviceInfo() {
        val info = kotlin.runCatching {
            DeviceInfoUtil().getAllDeviceInfo(webActivity)
        }.getOrNull() ?: ""
        webActivity.callbackInterface("getAllDeviceInfo", info)
    }

    @JavascriptInterface
    fun getReferer() {
        UserFace.referrerDetails.let {
            val map = mapOf(
                "appInstallTime" to it?.installBeginTimestampSeconds,
                "instantExperienceLaunched" to it?.googlePlayInstantParam,
                "referrerClickTime" to it?.referrerClickTimestampSeconds,
                "referrerUrl" to it?.installReferrer
            )
            webActivity.callbackInterface("getReferer", JSON.toJSONString(map))
        }
    }

    @JavascriptInterface
    fun getAfInstallConversionData() {
        webActivity.callAfBack()
    }

    @JavascriptInterface
    fun generalCheckPermission(permissions: String? = null) {
        if (permissions.isNullOrEmpty()) {
            webActivity.callbackInterface("generalCheckPermission", "-1")
            return
        }
        val result = if (permissions.split(",")
                .any {
                    ContextCompat.checkSelfPermission(
                        webActivity,
                        it
                    ) == PackageManager.PERMISSION_DENIED
                }
        ) {
            "-1"
        } else {
            "1"
        }
        webActivity.callbackInterface("generalCheckPermission", result)
    }

    @JavascriptInterface
    fun generalRequestPermission(permissions: String? = null) {
        if (permissions.isNullOrEmpty()) {
            return
        }
        val p = kotlin.runCatching {
            val re = permissions.replace("\"", "")
            val sub = re.subSequence(1, re.length - 1)
            sub.split(",").toTypedArray()
        }.getOrNull() ?: return
        webActivity.startRequestPermissions(p)
    }

    @JavascriptInterface
    fun checkLocationService() {
        val locationManager =
            webActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val result = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager
                .isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
        ) {
            "1"
        } else {
            "0"
        }
        webActivity.callbackInterface("checkLocationService", result)
    }

    @JavascriptInterface
    fun goToLocationService() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        kotlin.runCatching {
            webActivity.startActivity(intent)
        }
    }

    @JavascriptInterface
    @JvmOverloads
    fun getPhoto(json: String? = null) {
        webActivity.startPhoto(json)
    }

    @JavascriptInterface
    fun closeWebView() {
        webActivity.finish()
    }

    @JavascriptInterface
    fun updateLoginInfo(json: String? = null) {
        if (json.isNullOrEmpty()) {
            return
        }
        JSONObject(json).let {
            UserFace.phone = it.optString("phone")
            UserFace.token = it.optString("token")

            webActivity.lifecycleScope.launch {
                webActivity.writeDsData(DataStoreKeys.PHONE, UserFace.phone)
                webActivity.writeDsData(DataStoreKeys.TOKEN, UserFace.token)
            }
        }
    }

    @JavascriptInterface
    fun browser(url: String) {
        WebActivity.loadUrl(webActivity, url)
    }

    @JavascriptInterface
    fun browserWithTitle(url: String) {
        WebActivity.loadUrl(webActivity, url, true)
    }
}