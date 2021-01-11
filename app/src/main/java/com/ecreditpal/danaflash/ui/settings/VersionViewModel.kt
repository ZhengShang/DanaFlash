package com.ecreditpal.danaflash.ui.settings

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.data.DOWNLOAD_APK_WORK_NAME
import com.ecreditpal.danaflash.model.VersionRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

class VersionViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    private val mWorkInfo = workManager.getWorkInfosForUniqueWorkLiveData(DOWNLOAD_APK_WORK_NAME)

    var versionRes = MutableLiveData<VersionRes>()
    val versionEndText = Transformations.map(versionRes) {
        if (it.updateStatus == 0)
            application.getString(R.string.your_app_is_newest)
        else
            application.getString(R.string.new_version_detect_and_update)
    }
    val versionEndTextColor = Transformations.map(versionRes) {
        if (it.updateStatus == 0)
            ContextCompat.getColor(application, R.color.text)
        else
            ContextCompat.getColor(application, R.color.dana_orange)
    }

    fun checkVersion() {
        viewModelScope.launch {
            versionRes.value = kotlin.runCatching {
                dfApi().versionMange(channel = "google")
            }.getOrNull()?.data
        }
    }
}