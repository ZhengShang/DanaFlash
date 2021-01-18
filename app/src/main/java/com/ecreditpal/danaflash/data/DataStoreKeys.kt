import androidx.datastore.preferences.core.preferencesKey

object DataStoreKeys {
    //是否同意了隐私协议
    val IS_ACCEPT_PRIVACY = preferencesKey<Boolean>("is_accept_privacy")
    val IS_SHOW_PERMISSION_TIPS = preferencesKey<Boolean>("is_show_permission_tips")
    val IS_UPLOAD_CONTACTS = preferencesKey<Boolean>("is_upload_contacts")
    val TOKEN = preferencesKey<String>("token")
    val PHONE = preferencesKey<String>("phone")

    //广告上次显示的时间戳
    val AD_APIPOP_LAST_STAMP = preferencesKey<Long>("ad_apipop_last_stamp")
    val AD_POP_LAST_STAMP = preferencesKey<Long>("ad_pop_last_stamp")
    val AD_INDEX_LAST_STAMP = preferencesKey<Long>("ad_index_last_stamp")
    val AD_PERSONALPOP_LAST_STAMP = preferencesKey<Long>("ad_personalpop_last_stamp")
}
