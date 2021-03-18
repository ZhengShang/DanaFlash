package com.ecreditpal.danaflash.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.base.PopManager
import com.ecreditpal.danaflash.data.DataStoreKeys.CHECK_UPDATE_LAST_STAMP
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.model.VersionRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

class VersionViewModel(application: Application) : AndroidViewModel(application) {
    var versionRes = MutableLiveData<VersionRes>()

    fun checkVersion(formHome: Boolean) {
        viewModelScope.launch {
            val res = danaRequestWithCatch {
                dfApi().versionMange(channel = "google")
            }
            if (formHome) {
                if (res?.updateStatus == 1) {
                    //引导更新每7天才会弹出一次
                    val lastTramp = App.context.readDsData(CHECK_UPDATE_LAST_STAMP, 0)
                    val overSevenDay = TimeUtils.getTimeSpanByNow(lastTramp, TimeConstants.DAY) <= -7
                    if (overSevenDay.not()) {
                        PopManager.addPopToMap(PopManager.TYPE_UPDATE, null)
                        return@launch
                    }
                    App.context.writeDsData(CHECK_UPDATE_LAST_STAMP, System.currentTimeMillis())
                }
                PopManager.addPopToMap(PopManager.TYPE_UPDATE, res)
            }

            res?.let {
                versionRes.value = it
            }
        }
    }
}