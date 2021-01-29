package com.ecreditpal.danaflash.helper

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.IdRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.createDataStore
import androidx.navigation.Navigation
import com.ecreditpal.danaflash.data.H5_PREFIX
import com.ecreditpal.danaflash.data.UserFace
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

fun View?.nav(@IdRes id: Int) {
    if (this == null) return
    setOnClickListener {
        Navigation.findNavController(this).navigate(id)
    }
}

suspend fun <T> Context?.writeDsData(
    key: Preferences.Key<T>,
    value: T,
    dsName: String = "settings"
) {
    if (this == null) {
        return
    }
    val dataStore: DataStore<Preferences> = createDataStore(
        name = dsName
    )

    dataStore.edit { preferences ->
        preferences[key] = value
    }
}

suspend fun <T> Context?.readDsData(
    key: Preferences.Key<T>,
    defaultValue: T,
    dsName: String = "settings"
): T {
    if (this == null) {
        return defaultValue
    }
    val dataStore: DataStore<Preferences> = createDataStore(
        name = dsName
    )
    return dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[key] ?: defaultValue
        }
        .firstOrNull() ?: defaultValue
}

/**
 * 在给定的url的前后拼接特定的部分, 然后作为一个整体来加载
 * @param paramMap 参数map
 * @return 返回一个完整可以直接加载的url
 */
fun String?.combineH5Url(paramMap: Map<String, Any?>? = null): String {
    var paramStr = paramMap?.entries?.joinToString(separator = "&", transform = {
        it.key.plus("=").plus(it.value)
    })
    if (paramStr.isNullOrEmpty().not()) {
        paramStr = "?".plus(paramStr)
    }
    return H5_PREFIX.plus(this)
        .plus(paramStr)
        .plus("&")
        .plus("phone=${UserFace.phone}")
        .plus("&")
        .plus("token=${UserFace.token}")
}

fun Uri?.toBytes(context: Context?): ByteArray? {
    if (this == null) {
        return null
    }
    return context?.contentResolver?.openInputStream(this)?.buffered()?.use { it.readBytes() }
}