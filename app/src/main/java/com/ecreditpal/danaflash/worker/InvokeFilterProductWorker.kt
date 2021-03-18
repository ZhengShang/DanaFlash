package com.ecreditpal.danaflash.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.TimeUtils
import com.ecreditpal.danaflash.data.DataStoreKeys
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InvokeFilterProductWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        if (UserFace.isLogin().not()) {
            return Result.failure()
        }

        val lastTramp = runBlocking {
            applicationContext.readDsData(DataStoreKeys.FILTER_PRODUCT_STAMP, 0)
        }
        if (TimeUtils.isToday(lastTramp)) {
            //ONE DAY
            return Result.failure()
        }

        val res = runBlocking {
            kotlin.runCatching {
                dfApi().filterProduct().throwIfNotSuccess()
            }.getOrNull()
        }

        return if (res?.isSuccess() == true) {
            GlobalScope.launch {
                applicationContext.writeDsData(
                    DataStoreKeys.FILTER_PRODUCT_STAMP,
                    System.currentTimeMillis()
                )
            }
            Result.success()
        } else {
            Result.failure()
        }
    }
}