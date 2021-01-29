package com.ecreditpal.danaflash.base

import android.net.Uri
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.data.OSS_BUCKET
import com.ecreditpal.danaflash.data.OSS_ENDPOINT
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.OssStsRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageUploader {


    fun uploadImage(
        scope: CoroutineScope,
        uri: Uri?,
        uploadCallback: ((state: String) -> Unit)? = null
    ) {
        if (uri == null) {
            uploadCallback?.invoke("-1")
            return
        }
        scope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().ossSts()
            }
            LoadingTips.dismissLoading()

            val credentials = res?.credentials
            if (credentials == null) {
                uploadCallback?.invoke("-1")
                return@launch
            }
            uploadByAliyun(credentials, uri, uploadCallback)
        }
    }

    private fun uploadByAliyun(
        credentials: OssStsRes.Credentials,
        uri: Uri,
        uploadCallback: ((state: String) -> Unit)?
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

        // 构造上传请求。
        val objectKey = CommUtils.getOssObjectKey(uri)
        val put = PutObjectRequest(OSS_BUCKET, objectKey, uri)

        try {
            uploadCallback?.invoke("0")
            oss.putObject(put)
            uploadCallback?.invoke("1")
        } catch (e: Exception) {
            LogUtils.e("upload image oss failed.", e)
            uploadCallback?.invoke("-1")
        }
    }
}