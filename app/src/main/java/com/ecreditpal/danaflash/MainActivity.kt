package com.ecreditpal.danaflash

import DataStoreKeys
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.work.*
import com.ecreditpal.danaflash.base.BaseActivity
import com.ecreditpal.danaflash.data.AD_TITLE_APIPOP
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections
import com.ecreditpal.danaflash.ui.settings.VersionViewModel
import com.ecreditpal.danaflash.worker.UploadContactsWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        navView.setupWithNavController(navController)

        lifecycleScope.launch {
            val showTips = readDsData(DataStoreKeys.IS_SHOW_PERMISSION_TIPS, true)
            //First enter main page need to show permission tips
            if (showTips) {
                navController.navigate(R.id.action_global_permissionTipsDialog)
            } else {
                requestAllPermissions()
            }
        }

        val homeViewModel: HomeViewModel by viewModels()
        homeViewModel.adLiveData.observe(this) {
            if (it.first == AD_TITLE_APIPOP || it.first == AD_TITLE_PERSONALPOP) {
                navController.navigate(
                    MainFragmentDirections.actionGlobalAdDialog(it.second)
                )
            }
        }

        val versionViewModel: VersionViewModel by viewModels()
        versionViewModel.versionRes.observe(this) {
            navController.navigate(R.id.action_global_updateDialog2)
        }
        versionViewModel.checkVersion()
    }

    override fun onBackPressed() {
        finish()
    }

    fun requestAllPermissions() {
        val requestArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
        ).filter {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        requestPermissionsLauncher.launch(requestArray)

        //Try upload contacts if not upload
        if (Manifest.permission.READ_CONTACTS in requestArray) {
            return
        }
        lifecycleScope.launch {
            val upload = readDsData(DataStoreKeys.IS_UPLOAD_CONTACTS, false)
            if (upload.not()) {
                startUploadContactsWorker()
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            map.entries.forEach { entry ->
                when (entry.key) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    }
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    }
                    Manifest.permission.READ_CONTACTS -> if (entry.value) {
                        startUploadContactsWorker()
                    }
                }
            }
        }

    private fun startUploadContactsWorker() {
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<UploadContactsWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)
    }
}