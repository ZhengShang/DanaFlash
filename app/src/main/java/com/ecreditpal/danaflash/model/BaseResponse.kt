package com.ecreditpal.danaflash.model

data class BaseResponse<T>(
    val code: Int = 0,
    val desc: String? = "",
    val data: T?
) {
    fun isSuccess(): Boolean {
        return code == 200 && desc == "Success"
    }
}