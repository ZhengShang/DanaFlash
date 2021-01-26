package com.ecreditpal.danaflash.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class StartLiveness : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?) =
        Intent(context, CameraActivity::class.java).apply {
            putExtra(CameraActivity.KEY_JSON, input)
            putExtra(CameraActivity.KEY_MODE, CameraActivity.MODE_FACE_RECOGNITION)
        }

    override fun parseResult(resultCode: Int, result: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return result?.getStringExtra(LivenessFragment.EXTRA_RESULT)
    }
}