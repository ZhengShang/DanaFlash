import androidx.datastore.preferences.core.preferencesKey

object DataStoreKeys {
    //是否同意了隐私协议
    val IS_ACCEPT_PRIVACY = preferencesKey<Boolean>("is_accept_privacy")
    val IS_SHOW_PERMISSION_TIPS = preferencesKey<Boolean>("is_show_permission_tips")
    val IS_UPLOAD_CONTACTS = preferencesKey<Boolean>("is_upload_contacts")
    val TOKEN = preferencesKey<String>("token")
    val PHONE = preferencesKey<String>("phone")
}
