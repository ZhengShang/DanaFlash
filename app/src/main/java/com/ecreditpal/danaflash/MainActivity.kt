package com.ecreditpal.danaflash

import DataStoreKeys
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.work.*
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.base.BaseActivity
import com.ecreditpal.danaflash.data.AD_TITLE_APIPOP
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.data.AD_TITLE_POP
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections
import com.ecreditpal.danaflash.ui.settings.VersionViewModel
import com.ecreditpal.danaflash.worker.InvokeFilterProductWorker
import com.ecreditpal.danaflash.worker.UploadAllDeviceInfoWorker
import com.ecreditpal.danaflash.worker.UploadSurveyWorker
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var lastClickMills = 0L

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (now - lastClickMills < 2000) {
            super.onBackPressed()
        } else {
            lastClickMills = now
            ToastUtils.showLong("Tekan sekali lagi untuk keluar")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SurveyHelper.addOneSurvey("/", "in", "")

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        lifecycleScope.launch {
            val showTips = readDsData(DataStoreKeys.IS_SHOW_PERMISSION_TIPS, true)
            //First enter main page need to show permission tips
            if (showTips) {
                navController.navigate(R.id.action_global_permissionTipsDialog)
            } else {
                requestAllPermissions()
            }
        }

        homeViewModel.adLiveData.observe(this) {
            if (it.first == AD_TITLE_APIPOP
                || it.first == AD_TITLE_POP
                || it.first == AD_TITLE_PERSONALPOP
            ) {
                navController.navigate(
                    MainFragmentDirections.actionGlobalAdDialog(it.first, it.second)
                )
            }
        }

        val versionViewModel: VersionViewModel by viewModels()
        versionViewModel.versionRes.observe(this) {
            if (BuildConfig.DEBUG) {
                return@observe
            }
            if (it.updateStatus == 0) {
                return@observe
            }
            navController.navigate(
                MainFragmentDirections.actionGlobalUpdateDialog2(it)
            )
        }
        versionViewModel.checkVersion()

        startWorkers()
    }

    override fun onDestroy() {
        super.onDestroy()

        val uploadWorkRequest =
            OneTimeWorkRequest.Builder(UploadSurveyWorker::class.java)
                .build()

        WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)
    }

    private fun startWorkers() {
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(
            OneTimeWorkRequest.Builder(InvokeFilterProductWorker::class.java).build()
        )
        workManager.enqueue(
            OneTimeWorkRequest.Builder(UploadAllDeviceInfoWorker::class.java).build()
        )
    }

    fun isAllPermissionGranted() = PERMISSIONS
        .filter {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }.count() == 0

    fun requestAllPermissions() {
        val requestArray = PERMISSIONS
            .filter {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
            }.toTypedArray()

        requestPermissionsLauncher.launch(requestArray)
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.isEmpty()) {
                return@registerForActivityResult
            }
            //Invoke liveData
            val allGranted = map.values.all { true }
            homeViewModel.allPermissionGranted.value = allGranted

            map.entries.forEach { entry ->
                when (entry.key) {
                    Manifest.permission.READ_PHONE_STATE -> {
                        saveDeviceId()
                    }
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    }
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    }
                    Manifest.permission.READ_CONTACTS -> {

                    }
                }
            }
        }

    @SuppressLint("MissingPermission")
    private fun saveDeviceId() {
        val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PhoneUtils.getIMEI()
        } else {
            PhoneUtils.getDeviceId()
        }
        UserFace.deviceId = deviceId
        lifecycleScope.launch {
            writeDsData(DataStoreKeys.DEVICE_ID, deviceId)
        }
    }

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS
        )
    }
}