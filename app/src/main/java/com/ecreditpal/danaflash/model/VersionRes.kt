package com.ecreditpal.danaflash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VersionRes(
    val content: String?, // 谷歌更新
    val link: String?, // https://tropic-jakarta.oss-ap-southeast-5.aliyuncs.com/apk/Kreditpedia-jiafang.apk
    val online: Int?, // 1
    val updateStatus: Int? // 1
) : Parcelable