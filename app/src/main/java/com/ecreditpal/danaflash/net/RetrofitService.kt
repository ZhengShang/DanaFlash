package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.data.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(ApiLogInterceptor())
                }
            }
            .build()

        service = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(FastJsonConverterFactory.create())
            .build()
    }

    fun <T> create(apiService: Class<T>): T {
        return service.create(apiService)
    }
}