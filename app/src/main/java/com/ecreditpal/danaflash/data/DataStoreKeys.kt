import androidx.datastore.preferences.core.preferencesKey

object DataStoreKeys {
    //是否同意了隐私协议
    val IS_ACCEPT_PRIVACY = preferencesKey<Boolean>("is_accept_privacy")
    val IS_SHOW_PERMISSION_TIPS = preferencesKey<Boolean>("is_show_permission_tips")
    val TOKEN = preferencesKey<String>("token")
    val PHONE = preferencesKey<String>("phone")
    val DEVICE_ID = preferencesKey<String>("device_id")

    val LIVENESS_KEY = preferencesKey<String>("liveness_key")
    val LIVENESS_SECRET = preferencesKey<String>("liveness_secret")

    //广告上次显示的时间戳
    val AD_APIPOP_LAST_STAMP = preferencesKey<Long>("ad_apipop_last_stamp")
    val AD_POP_LAST_STAMP = preferencesKey<Long>("ad_pop_last_stamp")
    val AD_INDEX_LAST_STAMP = preferencesKey<Long>("ad_index_last_stamp")
    val AD_PERSONALPOP_LAST_STAMP = preferencesKey<Long>("ad_personalpop_last_stamp")

    val FILTER_PRODUCT_STAMP = preferencesKey<Long>("filter_product_stamp")
    val UPLOAD_ALL_DEVICE_INFO_STAMP = preferencesKey<Long>("upload_all_device_info_stamp")
}
