package com.ecreditpal.danaflash.ui.settings

import DataStoreKeys.CHECK_UPDATE_LAST_STAMP
import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.PopManager
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.model.VersionRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

class VersionViewModel(application: Application) : AndroidViewModel(application) {
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

    fun checkVersion(formHome: Boolean) {
        viewModelScope.launch {
            val res = danaRequestWithCatch {
                dfApi().versionMange(channel = "google")
            }
            if (formHome) {
                if (res?.updateStatus == 1) {
                    //引导更新每7天才会弹出一次
                    val lastTramp = App.context.readDsData(CHECK_UPDATE_LAST_STAMP, 0)
                    val overSevenDay = System.currentTimeMillis() - lastTramp > 604_800_000 //7 DAY
                    if (overSevenDay.not()) {
                        PopManager.addPopToMap(PopManager.TYPE_UPDATE, null)
                        return@launch
                    }
                    App.context.writeDsData(CHECK_UPDATE_LAST_STAMP, System.currentTimeMillis())
                }
                PopManager.addPopToMap(PopManager.TYPE_UPDATE, res)
            } else {
                res?.let {
                    versionRes.value = it
                }
            }
        }
    }
}