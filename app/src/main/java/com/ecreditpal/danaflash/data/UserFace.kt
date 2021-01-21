package com.ecreditpal.danaflash.data

import DataStoreKeys
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserFace {

    var token: String = ""
    var phone: String = ""
    var deviceId: String = ""

    fun isLogin(): Boolean {
        return token.isNotEmpty()
    }

    fun getGoogleId(): String {
        return "1234"
    }

    fun initData() {
        GlobalScope.launch(Dispatchers.IO) {
            token = App.context.readDsData(DataStoreKeys.TOKEN, "")
            phone = App.context.readDsData(DataStoreKeys.PHONE, "")
            deviceId = App.context.readDsData(DataStoreKeys.DEVICE_ID, "")
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