import androidx.datastore.preferences.core.preferencesKey

object DataStoreKeys {
    //是否同意了隐私协议
    val IS_ACCEPT_PRIVACY = preferencesKey<Boolean>("is_accept_privacy")
    val TOKEN = preferencesKey<String>("token")
    val PHONE = preferencesKey<String>("phone")
}
