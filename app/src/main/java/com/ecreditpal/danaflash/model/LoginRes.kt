package com.ecreditpal.danaflash.model

data class LoginRes(
    val contact: Boolean?, // false
    val countryPrefix: String?, // 62
    val isReg: Boolean?, // true
    val nationalNumber: String?, // 888899991234
    val number: String?, // +62888899991234
    val sign: String?, // xxxxxx
    val token: String? // xxxx
)