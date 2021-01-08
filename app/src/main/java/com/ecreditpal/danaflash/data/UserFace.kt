package com.ecreditpal.danaflash.data

object UserFace {

    var token: String? = ""

    fun isLogin(): Boolean {
        return token.isNullOrEmpty().not()
    }

    fun getPhone(): String {
        return ""
    }

    fun getGoogleId(): String {
        return "1234"
    }
}