package com.ecreditpal.danaflash.worker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.preferencesKey
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ecreditpal.danaflash.MainActivity
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.DeviceInfoUtil
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UploadAllDeviceInfoWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        if (UserFace.isLogin().not()) {
            return Result.failure()
        }

        MainActivity.PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_DENIED
        }.let { denied ->
            if (denied) {
                return Result.failure()
            }
        }

        val dsKey = preferencesKey<Long>("upload_all_device_info_stamp_${UserFace.phone}")

        val lastTramp = runBlocking {
            context.readDsData(dsKey, 0)
        }
        if (System.currentTimeMillis() - lastTramp < 86_400_000) {
            //ONE DAY
            return Result.failure()
        }

        val res = runBlocking {
            kotlin.runCatching {
                Looper.prepare()
                val info = DeviceInfoUtil().getAllDeviceInfo(context) ?: ""
                dfApi().uploadAllDeviceInfo(1, info).throwIfNotSuccess()
            }.getOrNull()
        }

        return if (res?.isSuccess() == true) {
            GlobalScope.launch {
                context.writeDsData(
                    dsKey,
                    System.currentTimeMillis()
                )
            }
            Result.success()
        } else {
            Result.failure()
        }
    }
}