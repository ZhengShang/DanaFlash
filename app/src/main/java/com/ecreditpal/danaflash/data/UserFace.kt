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
        }
    }

    fun clearData() {
        GlobalScope.launch(Dispatchers.IO) {
            token = ""
            phone = ""
            App.context.writeDsData(DataStoreKeys.TOKEN, "")
            App.context.writeDsData(DataStoreKeys.PHONE, "")
        }
    }
}