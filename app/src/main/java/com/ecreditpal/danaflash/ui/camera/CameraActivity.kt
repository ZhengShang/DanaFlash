package com.ecreditpal.danaflash.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseNavActivity
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog

@RequiresApi(Build.VERSION_CODES.M)
class CameraActivity : BaseNavActivity() {

    override fun navGraphId() = R.navigation.camera_navigation

    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted.not()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showRational()
                } else {
                    ToastUtils.showLong(R.string.failed_to_open_camera_without_permission)
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //full status bar
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        hideToolbar()

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