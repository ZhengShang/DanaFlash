package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.model.*
import okhttp3.RequestBody
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

    @POST("/adakita/contact_list")
    suspend fun uploadContacts(@Body body: RequestBody): BaseResponse<Void>

    @GET("/adakita/loan_order/list")
    suspend fun getOrderList(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: Int
    ): BaseResponse<List<OrderRes>>

    @GET("/adakita/ad/imgs")
    suspend fun getAds(
        @Query("title") title: String
    ): BaseResponse<AdRes>

    @FormUrlEncoded
    @POST("/adakita/feedback/add")
    suspend fun feedback(@Field("content") content: String): BaseResponse<Void>

}


fun dfApi() = RetrofitService.create(DanaApi::class.java)