package com.ecreditpal.danaflash.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdRes(
    val contentId: Int?,  //广告内容 id,
    val imgs: List<Img?>?,
    val middleBanner: Int? // 中部 banner
) : Parcelable {
    @Parcelize
    data class Img(
        val img: String?, // 半链接,拼接前缀为 oss 地址，
        val url: String?  // 半链接,拼接前缀为前端页面地址，/apiPro ductDetail 跳到 api 产品详情页
    ) : Parcelable
}