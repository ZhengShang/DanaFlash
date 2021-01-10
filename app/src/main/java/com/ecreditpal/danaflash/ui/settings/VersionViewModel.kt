package com.ecreditpal.danaflash.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

/**
 * Created by shang on 2021/1/10.
 *            🐳🐳🐳🍒           20:28 🥥
 */
class VersionViewModel : ViewModel() {

    fun checkVersion() {
        viewModelScope.launch {
            kotlin.runCatching {
                dfApi().versionMange(channel = "default")
            }.getOrNull()?.let {
                println("Check version: $it")
            }
        }
    }
}