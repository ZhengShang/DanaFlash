package com.ecreditpal.danaflash.worker

import DataStoreKeys
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.RequestBody

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

        val cr = applicationContext.contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        ) ?: return Result.failure()

        if (cur.count <= 0) {
            return Result.failure()
        }

        val contactList = mutableListOf<Any>()
        try {
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    ) ?: continue
                    while (pCur.moveToNext()) {
                        val phoneNo =
                            pCur.getString(
                                pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )

                        contactList.add(
                            JSON.toJSONString(
                                mapOf(
                                    "name" to name,
                                    "number" to phoneNo
                                )
                            )
                        )
                    }
                    pCur.close()
                }
            }
        } catch (e: Exception) {
            LogUtils.e("get phone contacts failed.", e)
            return Result.retry()
        } finally {
            cur.close()
        }

        if (contactList.isEmpty()) {
            return Result.failure()
        }
        LogUtils.d(contactList)

        val body = RequestBody.create(
            MediaType.parse("Content-Type, application/json"), contactList.joinToString()
        )

        val res = runBlocking {
            kotlin.runCatching {
                dfApi().uploadContacts(body)
            }.getOrNull()
        } ?: return Result.retry()

        return if (res.isSuccess()) {
            GlobalScope.launch {
                applicationContext.writeDsData(DataStoreKeys.IS_UPLOAD_CONTACTS, true)
            }
            Result.success()
        } else {
            Result.retry()
        }
    }
}