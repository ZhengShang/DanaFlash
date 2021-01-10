package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.model.BaseResponse
import com.ecreditpal.danaflash.model.LoginRes
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.model.VersionRes
import retrofit2.http.*

interface DanaApi {

    @FormUrlEncoded
    @POST("/adakita/sms_login/vcode")
    suspend fun getVCode(
        @Field("phone") phone: String,
        @Field("code") code: String = "62"
    ): BaseResponse<Void>

    @FormUrlEncoded
    @POST("/adakita//sms_login")
    suspend fun login(
        @Field("phone") phone: String,
        @Field("deviceId") deviceId: String,
        @Field("vcode") vcode: String,
        @Field("code") code: String = "62"
    ): BaseResponse<LoginRes>

    @GET("/adakita/version/manage")
    suspend fun versionMange(
        @Query("channel") channel: String,
        @Query("version") version: String = BuildConfig.VERSION_NAME,
        @Query("versionCode") versionCode: Int = BuildConfig.VERSION_CODE
    ): BaseResponse<VersionRes>

    @GET("/adakita/product")
    suspend fun product(@QueryMap map: Map<String, @JvmSuppressWildcards Any>): BaseResponse<ProductRes>
}


fun dfApi() = RetrofitService.create(DanaApi::class.java)