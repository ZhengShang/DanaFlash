package com.ecreditpal.danaflash.model

data class ProductRes(
    val isHomeOrder: Boolean?, // true
    val list: List<Product>?,
    val max: Int?, // 296
    val selectIds: List<Int?>?
) {
    data class Product(
        val amountMax: Double?, // 1000000.00
        val amountMin: Double?, // 600000.00
        val api: Boolean?, // false
        val applyNum: Int?, // 2226
        val auditScore: Any?, // null
        val canApply: Boolean?, // true
        val id: Int?, // 32
        val image: String?, // /staging/MemberData/4919bf58c4a94d77b0ef7b8cf663b29d
        val interestMax: Any?, // null
        val interestMin: Double?, // 0.330
        val link: String?, // https://app.appsflyer.com/com.julofinance.juloapp?c={campaign_name}&af_siteid={af_siteid}&pid=kspid_int&clickid={clickid}&android_id={android_id}&advertising_id={advertising_id}
        val name: String?, // Julo
        val packageName: Any?, // null
        val periodMax: Any?, // null
        val periodMin: Any?, // null
        val periodUnit: Any?, // null
        val processTime: Double?, // 24.0
        val score: Double?, // 4.6
        val spareLink: String?,
        val summary: String?,
        val tag: Any?, // null
        val xinghe: Any? // null
    )
}