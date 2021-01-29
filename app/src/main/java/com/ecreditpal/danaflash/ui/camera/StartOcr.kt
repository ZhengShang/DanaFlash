package com.ecreditpal.danaflash.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class StartOcr : ActivityResultContract<String?, Pair<String?, Uri?>>() {
    override fun createIntent(context: Context, input: String?) =
        Intent(context, CameraActivity::class.java).apply {
            putExtra(CameraActivity.KEY_JSON, input)
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