package com.ecreditpal.danaflash.data

import DataStoreKeys
import android.accounts.AccountManager
import android.content.Context.ACCOUNT_SERVICE
import android.util.Log
import androidx.ads.identifier.AdvertisingIdClient
import androidx.ads.identifier.AdvertisingIdInfo
import androidx.datastore.preferences.createDataStore
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


object UserFace {

    var token: String = ""
    var phone: String = ""
    var deviceId: String = ""
    var gaid: String = ""
    var mediaSource = ""
    var referrerDetails: ReferrerDetails? = null

    fun isLogin(): Boolean {
        return token.isNotEmpty()
    }

    fun getGoogleId(): String {
        val manager = App.context.getSystemService(ACCOUNT_SERVICE) as? AccountManager
        return manager?.getAccountsByType("com.google")
            ?.firstOrNull()?.name ?: ""
    }

    fun initData() {
        GlobalScope.launch(Dispatchers.IO) {
            token = App.context.readDsData(DataStoreKeys.TOKEN, "")
            phone = App.context.readDsData(DataStoreKeys.PHONE, "")
            deviceId = App.context.readDsData(DataStoreKeys.DEVICE_ID, "")
        }

        initGaid()
        initAf()
        initRefer()
    }

    private fun initGaid() {
        if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(App.context)) {
            val advertisingIdInfoListenableFuture =
                AdvertisingIdClient.getAdvertisingIdInfo(App.context)

            Futures.addCallback(
                advertisingIdInfoListenableFuture,
                object : FutureCallback<AdvertisingIdInfo> {
                    override fun onSuccess(adInfo: AdvertisingIdInfo?) {
                        gaid = adInfo?.id ?: ""
                    }

                    override fun onFailure(t: Throwable) {

                    }
                }, Executors.newSingleThreadExecutor()
            )
        } else {
            LogUtils.e("NOT available gaid")
        }
    }

    private fun initAf() {
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                mediaSource = data?.get("media_source")?.toString() ?: ""
            }

            override fun onConversionDataFail(error: String?) {
                Log.e("LOG_TAG", "error onConversionDataFail :  $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.map {
                    Log.d("LOG_TAG", "onAppOpen_attribute: ${it.key} = ${it.value}")
                }
            }

            override fun onAttributionFailure(error: String?) {
                Log.e("LOG_TAG", "error onAttributionFailure :  $error")
            }
        }

        AppsFlyerLib.getInstance().init(DEV_KEY, conversionDataListener, App.context)
        AppsFlyerLib.getInstance().start(App.context)
    }

    private fun initRefer() {
        val referrerClient = InstallReferrerClient.newBuilder(App.context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        referrerDetails = referrerClient.installReferrer
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    fun clearData() {
        GlobalScope.launch(Dispatchers.IO) {
            token = ""
            phone = ""
            deviceId = ""
            App.context.writeDsData(DataStoreKeys.TOKEN, "")
            App.context.writeDsData(DataStoreKeys.PHONE, "")
            App.context.writeDsData(DataStoreKeys.DEVICE_ID, "")
        }
    }

    fun getDs() = App.context.createDataStore(name = "settings")
}