package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.fastjson.FastJsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

object RetrofitService {

    private const val TIMEOUT = 15_000L
    private var service: Retrofit

    init {

        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(RequestInterceptor())
            .proxy(Proxy.NO_PROXY)
            .apply {
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(ApiLogInterceptor())
                }
            }
            .build()

        service = Retrofit.Builder()
            .client(client)
            .baseUrl("http://www.pdd66in.xyz:7092/")
            .addConverterFactory(FastJsonConverterFactory.create())
            .build()
    }

    fun <T> create(apiService: Class<T>): T {
        return service.create(apiService)
    }
}