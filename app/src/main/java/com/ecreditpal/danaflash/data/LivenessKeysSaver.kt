package com.ecreditpal.danaflash.data

import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object LivenessKeysSaver {
    var key = ""
        get() {
            if (field.isEmpty()) {
                GlobalScope.launch {
                    tryGetFromNet()
                }
            }
            return field
        }

    var secret = ""

    fun init() {
        GlobalScope.launch {
            val readKey = App.context.readDsData(DataStoreKeys.LIVENESS_KEY, "")
            if (readKey.isEmpty()) {
                tryGetFromNet()
            } else {
                key = readKey
                secret = App.context.readDsData(DataStoreKeys.LIVENESS_SECRET, "")
            }
        }
    }

    private suspend fun tryGetFromNet() {
        danaRequestWithCatch {
            dfApi().getLivenessSecretKey()
        }?.let {
            key = it.key ?: ""
            secret = it.secret ?: ""
            // Save in ds
            App.context.writeDsData(DataStoreKeys.LIVENESS_KEY, key)
            App.context.writeDsData(DataStoreKeys.LIVENESS_SECRET, secret)
        }
    }
}