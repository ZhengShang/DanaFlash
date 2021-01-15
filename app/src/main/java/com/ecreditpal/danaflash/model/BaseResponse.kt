package com.ecreditpal.danaflash.model

import java.io.IOException

data class BaseResponse<T>(
    val code: Int = 0,
    val desc: String? = "",
    val data: T?
) {
    fun isSuccess(): Boolean {
        return code == 200 && desc == "Success"
    }

    fun throwIfNotSuccess(): BaseResponse<T> {
        if (code != 200) {
            throw IOException(desc)
        }
        return this
    }
}