package com.ecreditpal.danaflash.model

import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.net.DanaException

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
            if (code == 502 || desc == "Invalid Token") {
                UserFace.clearData()
            }
            throw DanaException(code, desc)
        }
        return this
    }
}