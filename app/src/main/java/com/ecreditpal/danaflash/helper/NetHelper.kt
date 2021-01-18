package com.ecreditpal.danaflash.helper

import com.ecreditpal.danaflash.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> danaRequest(block: suspend () -> BaseResponse<T>): T? {
    return withContext(Dispatchers.IO) {
        block.invoke().throwIfNotSuccess().data
    }

}

/**
 * api请求结果, 已经处理了异常情况
 */
suspend fun <T> danaRequestResult(block: suspend () -> BaseResponse<T>): Boolean {
    return runCatching {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    }.getOrNull()?.isSuccess() ?: false

}