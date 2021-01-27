package com.ecreditpal.danaflash.base

import android.net.Uri
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageUploader {
    fun uploadImage(scope: CoroutineScope, uri: Uri) {
        scope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().ossSts()
            }
            LoadingTips.dismissLoading()
            if (res != null) {

            }
        }
    }

    private fun uploadByAliyun() {

    }
}