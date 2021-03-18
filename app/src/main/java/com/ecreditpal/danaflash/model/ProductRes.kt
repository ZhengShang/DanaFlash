package com.ecreditpal.danaflash.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class ProductRes(
    val isHomeOrder: Boolean?, // true
    val list: List<Product>?,
    val max: Int?, // 296
    val selectIds: List<Int?>?
) : Parcelable {
    @Parcelize
    data class Product(
        val amountMax: BigDecimal?, // 1000000.00
        val amountMin: BigDecimal?, // 600000.00
        val api: Boolean?, // false
        val applyNum: Int?, // 2226
        val auditScore: String?, // null
        val canApply: Boolean?, // true
        val id: Int?, // 32
        val image: String?, // /staging/MemberData/4919bf58c4a94d77b0ef7b8cf663b29d
        val interestMax: BigDecimal?, // null
        val interestMin: BigDecimal?, // 0.330
        val link: String?, // https://app.appsflyer.com/com.julofinance.juloapp?c={campaign_name}&af_siteid={af_siteid}&pid=kspid_int&clickid={clickid}&android_id={android_id}&advertising_id={advertising_id}
        val name: String?, // Julo
        val packageName: String?, // null
        val periodMax: Int?, // null
        val periodMin: Int?, // null
        val periodUnit: Int?, // null
        val processTime: BigDecimal?, // 24.0
        val score: BigDecimal?, // 4.6
        val spareLink: String?,
        val summary: String?,
        val tag: String?, // null
        val xinghe: Int? // null
    ) : Parcelable
}