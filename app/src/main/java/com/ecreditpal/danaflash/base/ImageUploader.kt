package com.ecreditpal.danaflash.base

import android.graphics.Bitmap
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.data.OSS_BUCKET
import com.ecreditpal.danaflash.data.OSS_ENDPOINT
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.OssStsRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImageUploader {


    fun uploadImage(
        scope: CoroutineScope,
        bitmap: Bitmap?,
        objectKey: String,
        ocr: Boolean,
        uploadCallback: ((state: String, imageBytes: ByteArray?) -> Unit)? = null
    ) {
        if (bitmap == null) {
            uploadCallback?.invoke("-1", null)
            return
        }
        scope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().ossSts()
            }
            val compressBytes = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val c = ImageUtils.rotate(bitmap, if (ocr) 0 else 90, 0f, 0f, true)
                    ImageUtils.compressByQuality(c, 500_000L, true)
                }.getOrNull()
            }
            LoadingTips.dismissLoading()

            val credentials = res?.credentials
            if (credentials == null || compressBytes == null) {
                uploadCallback?.invoke("-1", null)
                return@launch
            }
            withContext(Dispatchers.IO) {
                uploadByAliyun(credentials, compressBytes, objectKey, uploadCallback)
            }
        }
    }

    private fun uploadByAliyun(
        credentials: OssStsRes.Credentials,
        byteArray: ByteArray,
        objectKey: String,
        uploadCallback: ((state: String, imageBytes: ByteArray?) -> Unit)?
    ) {
        val credetialProvider: OSSCredentialProvider = object : OSSFederationCredentialProvider() {
            override fun getFederationToken(): OSSFederationToken {
                return OSSFederationToken(
                    credentials.accessKeyId,
                    credentials.accessKeySecret,
                    credentials.securityToken,
                    credentials.expiration
                )
            }
        }
        val oss: OSS = OSSClient(App.context, OSS_ENDPOINT, credetialProvider)

        val put = PutObjectRequest(OSS_BUCKET, objectKey, byteArray)

        try {
            uploadCallback?.invoke("0", null)
            oss.putObject(put)
            uploadCallback?.invoke("1", byteArray)
        } catch (e: Exception) {
            LogUtils.e("upload image oss failed.", e)
            uploadCallback?.invoke("-1", null)
        }
    }
}