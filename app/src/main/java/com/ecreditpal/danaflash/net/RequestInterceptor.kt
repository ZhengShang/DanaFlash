package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.CommUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation
import java.io.IOException

class RequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        //验证token
        //如果请求的接口未标记@NoNeedToken注解, 那么当token为空时, 需要跳转到登录页面
        val needToken = request.tag(Invocation::class.java)?.method()
            ?.getAnnotation(NoNeedToken::class.java) == null
        if (needToken && UserFace.token.isEmpty()) {
            CommUtils.navLogin()
            return chain.proceed(request)
        }
        return addHeaders(chain, request)
    }

    /**
     * 配置token
     * @return response
     */
    @Throws(IOException::class)
    private fun addHeaders(chain: Interceptor.Chain, request: Request): Response {
        val q = request.newBuilder()
            .addHeader("phone", UserFace.phone)
            .addHeader("token", UserFace.token)
            .addHeader("appName", "DanaFlash")
            .addHeader("deviceId", UserFace.deviceId)
            .build()
        return chain.proceed(q)
    }
}
