package com.ecreditpal.danaflash.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog
import java.io.File

class CameraActivity : BaseNavActivity() {

    companion object {
        const val KEY_JSON = "json"
        const val KEY_MODE = "mode"
        const val KEY_PHOTO = "photo"
        const val MODE_OCR = 0
        const val MODE_FACE_RECOGNITION = 1
        private const val PERMISSION_DENIED = "-1"

        val outputDirectory = File(App.context.filesDir, "picture").also {
            if (it.exists().not()) {
                it.mkdir()
            }
        }
    }

    override fun navGraphId() = R.navigation.camera_navigation

    @SuppressLint("NewApi")
    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted.not()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showRational()
                } else {
                    ToastUtils.showLong(R.string.failed_to_open_camera_without_permission)
                    setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(LivenessFragment.EXTRA_RESULT, PERMISSION_DENIED)
                    )
                    finish()
                }
            }
        }

    override fun onBackPressed() {
        finish()
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //full status bar
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        hideToolbar()

        val mode = intent?.getIntExtra(KEY_MODE, MODE_OCR)
        if (mode == MODE_FACE_RECOGNITION) {
            Navigation.findNavController(this, R.id.nav_host_fragment)
                .navigate(R.id.livenessFragment)
        }

        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {

            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRational()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun showRational() {
        ConfirmDialog(
            contentStr = getString(R.string.failed_to_open_camera_without_permission),
            negativeClickListener = {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(LivenessFragment.EXTRA_RESULT, PERMISSION_DENIED)
                )
                finish()
            },
            positiveClickListener = {
                requestPermission()
            }
        ).apply {
            isCancelable = false
        }
            .show(supportFragmentManager)
    }

    private fun requestPermission() {
        permissionResult.launch(Manifest.permission.CAMERA)
    }
}