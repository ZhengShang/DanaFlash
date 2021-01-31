package com.ecreditpal.danaflash.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * input参数有2个, 第一个表示前端发过来的参数, 到时候会原路返回,
 * 第二个参数标记是否为照片模式, 如果是照片模式,那么隐藏定位框
 */
class StartOcr : ActivityResultContract<Pair<String?, Boolean>, Pair<String?, Uri?>>() {
    override fun createIntent(context: Context, input: Pair<String?, Boolean>) =
        Intent(context, CameraActivity::class.java).apply {
            putExtra(CameraActivity.KEY_JSON, input.first)
            putExtra(CameraActivity.KEY_PHOTO, input.second)
            putExtra(CameraActivity.KEY_MODE, CameraActivity.MODE_OCR)
        }

    override fun parseResult(resultCode: Int, result: Intent?): Pair<String?, Uri?> {
        if (resultCode != Activity.RESULT_OK) {
            return Pair(null, null)
        }
        return Pair(
            result?.getStringExtra(OcrFragment.EXTRA_OCR_INPUT_STRING),
            result?.getParcelableExtra(OcrFragment.EXTRA_OCR_PHOTO_URI)
        )
    }
}