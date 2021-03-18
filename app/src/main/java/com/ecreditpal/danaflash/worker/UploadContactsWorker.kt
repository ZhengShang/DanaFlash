package com.ecreditpal.danaflash.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.GsonUtils
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.RequestBody

@Deprecated("需要强相关回调的时候, 用这个可能会造成回调不及时")
class UploadContactsWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return Result.failure()
        }

        val json = CommUtils.getAllContacts(applicationContext)?.joinToString {
            GsonUtils.toJson(
                mapOf(
                    "name" to it.name,
                    "number" to it.phone
                )
            )
        }


        val jsonString = """
            {
               "contactList" : [$json]
            }
        """.trimIndent()

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), jsonString
        )

        val res = runBlocking {
            kotlin.runCatching {
                dfApi().uploadContacts(body).throwIfNotSuccess()
            }.getOrNull()
        } ?: return Result.failure()

        return if (res.isSuccess()) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}