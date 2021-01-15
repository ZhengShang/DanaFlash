package com.ecreditpal.danaflash.helper

import com.ecreditpal.danaflash.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> danaRequest(block: suspend () -> BaseResponse<T>): T? {
    return withContext(Dispatchers.IO) {
        block.invoke().throwIfNotSuccess().data
    }

}