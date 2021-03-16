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
import com.ecreditpal.danaflash.base.PopManager
import com.ecreditpal.danaflash.data.AD_TITLE_APIPOP
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.data.AD_TITLE_POP
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.model.AdRes
import com.ecreditpal.danaflash.model.VersionRes
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections
import com.ecreditpal.danaflash.ui.settings.VersionViewModel
import com.ecreditpal.danaflash.worker.UploadAllDeviceInfoWorker
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var lastClickMills = 0L

    //是否需要等待权限请求结束才能显示Pop弹窗
    private var waitForPermission = true

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
                waitForPermission = false
                showPopDialogs()
            }
        }

        homeViewModel.adLiveData.observe(this) {
            if (it.first == AD_TITLE_PERSONALPOP) {
                val adRes = it.second ?: return@observe
                navController.navigate(
                    MainFragmentDirections.actionGlobalAdDialog(it.first, adRes)
                )
            } else if (it.first == AD_TITLE_APIPOP) {
                //放到PopManager里面装起来, 按照一定的次序再来显示
                PopManager.addPopToMap(PopManager.TYPE_API, it.second)
            } else if (it.first == AD_TITLE_POP) {
                //放到PopManager里面装起来, 按照一定的次序再来显示
                PopManager.addPopToMap(PopManager.TYPE_POP, it.second)
            }
            showPopDialogs()
        }

        homeViewModel.showPopLiveData.observe(this) { pair ->
            val key = pair.first
            val res = pair.second

            if (res is VersionRes) {
                navController.navigate(
                    MainFragmentDirections.actionGlobalUpdateDialog2(res)
                )
            } else if (res is AdRes) {
                val adTitle = if (key == PopManager.TYPE_API) AD_TITLE_APIPOP else AD_TITLE_POP
                navController.navigate(
                    MainFragmentDirections.actionGlobalAdDialog(adTitle, res)
                )
            }
        }

        val versionViewModel: VersionViewModel by viewModels()
        versionViewModel.versionRes.observe(this) {
            showPopDialogs()
        }

        versionViewModel.checkVersion(true)
        homeViewModel.getAd(AD_TITLE_APIPOP)
        homeViewModel.getAd(AD_TITLE_POP)

        startWorkers()
    }

    //此时意味着可以显示暂存在PopManager中的Dialog了
    private fun showPopDialogs() {
        if (waitForPermission) {
            return
        }
        if (homeViewModel.currentPageIndex == 1) {
            return
        }
        homeViewModel.tryShowPopDialog()
    }

    private fun startWorkers() {
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(
            OneTimeWorkRequest.Builder(UploadAllDeviceInfoWorker::class.java).build()
        )
        CommUtils.startGetLocationWorker(this)
    }

    fun isAllPermissionGranted() = PERMISSIONS
        .filter {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }.count() == 0

    fun requestLocationPermission(request: Boolean) {
        if (request) {
            requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            waitForPermission = false
            showPopDialogs()
        }
    }

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

            map.entries.forEach { entry ->
                when (entry.key) {
                    Manifest.permission.READ_PHONE_STATE -> {
                        saveDeviceId()
                    }
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        CommUtils.startGetLocationWorker(this)
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

    private val requestLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            waitForPermission = false
            showPopDialogs()
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