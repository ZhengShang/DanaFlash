package com.ecreditpal.danaflash.data

import DataStoreKeys
import android.accounts.AccountManager
import android.content.Context.ACCOUNT_SERVICE
import androidx.ads.identifier.AdvertisingIdClient
import androidx.ads.identifier.AdvertisingIdInfo
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
}