package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.data.UserFace
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return addToken(chain, request)!!
    }

    /**
     * 配置token
     * @return response
     */
    @Throws(IOException::class)
    private fun addToken(chain: Interceptor.Chain, request: Request): Response? {
        val q = request.newBuilder()
            .addHeader("phone", UserFace.phone)
            .addHeader("token", UserFace.token)
            .addHeader("appName", "DanaFlash")
            .build()
        return chain.proceed(q)
    }
}
