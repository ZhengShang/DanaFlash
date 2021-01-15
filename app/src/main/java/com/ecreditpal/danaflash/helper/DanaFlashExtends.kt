package com.ecreditpal.danaflash.helper

import android.content.Context
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

fun String?.combineH5Url(): String {
    return H5_PREFIX.plus(this)
        .plus("&")
        .plus("phone=${UserFace.phone}")
        .plus("&")
        .plus("token=${UserFace.token}")
}