package com.ecreditpal.danaflash.model

data class AppInfoModel(
    val appName: String?, // （app名称
    val packageName: String?, // （app包名
    val versionName: String?, // （app版本号
    val versionCode: Int, // (app版本号
    val firstInstall: Long, // （app安装时间
    val lastInstall: Long?, // （app最后更新时间
    val flag: Int?, // （app flag 值
)