package com.ecreditpal.danaflash.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.DataStoreKeys
import com.ecreditpal.danaflash.data.H5_ORDER_CONFIRM
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.model.ContactRes
import com.ecreditpal.danaflash.model.SurveyModel
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.ui.comm.WebActivity
import com.ecreditpal.danaflash.ui.login.LoginActivity
import com.ecreditpal.danaflash.worker.GetLocationWorker
import com.ecreditpal.danaflash.worker.UploadSurveyWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object CommUtils {

    fun navLogin() {
        LoadingTips.dismissLoading()
        ActivityUtils.startActivity(
            Intent(
                ActivityUtils.getTopActivity(),
                LoginActivity::class.java
            )
        )
    }

    /**
     * 跳转到google商店下载页
     */
    fun navGoogleDownload(context: Context?, link: String?) {
        SurveyHelper.addOneSurvey("/", "gogg")
        LogUtils.d("open link in play store => $link")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
            setPackage("com.android.vending")
        }
        try {
            context?.startActivity(intent)
            SurveyHelper.addOneSurvey("任意p", "gogg")
        } catch (e: Exception) {
            ToastUtils.showLong(R.string.failed_to_donwload_in_google_store)
        }
    }

    /**
     * 从Uri中获取文件名, 然后拼接后返回oss上传所需要的object名字
     */
    fun getOssObjectKey(uri: Uri): String {
        val fileName = uri.toString().substringAfter("picture/")
        return "danaflash/staging/MemberData/$fileName"
    }

    /**
     * 活体检测结束后的操作, 需要重新从后台拉取接口判断是否活体检测成功了, 并因此显示不同的结果
     */
    fun stepAfterLiveness(
        scope: LifecycleCoroutineScope,
        context: Context?,
        livenessResult: String?
    ) {
        if (livenessResult.isNullOrEmpty() || livenessResult == "0" || livenessResult == "-1") {

            return
        }

        //check api
        scope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().getFaceCheckResult(livenessResult)
            }
            LoadingTips.dismissLoading()
            when (res?.faceCheckBean?.handle) {
                "SIMILARITY_NOT_PASS" -> {
                    ToastUtils.showLong("Identifikasi Gagal, Mohon Pastikan KTP sesuai dengan data Pribadi")
                }
                "LIVE_SECORE_NOT_PASS" -> {
                    ToastUtils.showLong("Tes Gagal, Mohon Pastikan Foto Terang dan tidak Buram")
                }
                else -> {
                    //进入订单确认页
                    WebActivity.loadUrl(context, H5_ORDER_CONFIRM.combineH5Url())
                }
            }
        }
    }

    /**
     * 获取所有联系人
     */
    fun getAllContacts(context: Context?): MutableList<ContactRes>? {

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val contactList = mutableListOf<ContactRes>()
        val cr = context?.contentResolver
        val cur = cr?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        ) ?: return null

        if (cur.count <= 0) {
            return contactList
        }

        try {
            while (cur.moveToNext()) {
                val name =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY))
                val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if (number.isNotBlank()) {
                    contactList.add(ContactRes(name, number))
                }
            }
        } catch (e: Exception) {
            LogUtils.e("get phone contacts failed.", e)
            return null
        } finally {
            cur.close()
        }

        LogUtils.d(contactList)
        return contactList
    }

    fun startGetLocationWorker(context: Context?) {
        context?.let {
            WorkManager.getInstance(it)
                .enqueueUniqueWork(
                    "Get location",
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequest.Builder(GetLocationWorker::class.java).build()
                )
        }
    }

    fun startSurveyWorker(surveyModel: SurveyModel) {
        val uploadWorkRequest =
            OneTimeWorkRequest.Builder(UploadSurveyWorker::class.java)
                .setInputData(
                    workDataOf(
                        UploadSurveyWorker.EXTRA_SURVEY_STRING to surveyModel.toString()
                    )
                )
                .build()

        WorkManager
            .getInstance(App.context)
            .enqueue(uploadWorkRequest)
    }

    fun saveDeviceId(context: Context?, scope: CoroutineScope) {
        context ?: return
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (UserFace.deviceId.isNotBlank()) {
            return
        }
        val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PhoneUtils.getIMEI()
        } else {
            PhoneUtils.getDeviceId()
        }
        UserFace.deviceId = deviceId
        scope.launch {
            context.writeDsData(DataStoreKeys.DEVICE_ID, deviceId)
        }
    }
}
