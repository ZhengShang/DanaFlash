package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.model.*
import okhttp3.RequestBody
import retrofit2.http.*

interface DanaApi {

    @NoNeedToken
    @FormUrlEncoded
    @POST("/adakita/sms_login/vcode")
    suspend fun getVCode(
        @Field("phone") phone: String,
        @Field("code") code: String = "62"
    ): BaseResponse<Void>

    @NoNeedToken
    @FormUrlEncoded
    @POST("/adakita//sms_login")
    suspend fun login(
        @Field("phone") phone: String,
        @Field("deviceId") deviceId: String,
        @Field("vcode") vcode: String,
        @Field("code") code: String = "62"
    ): BaseResponse<LoginRes>

    @NoNeedToken
    @GET("/adakita/version/manage")
    suspend fun versionMange(
        @Query("channel") channel: String,
        @Query("version") version: String = BuildConfig.VERSION_NAME,
        @Query("versionCode") versionCode: Int = BuildConfig.VERSION_CODE
    ): BaseResponse<VersionRes>

    @NoNeedToken
    @GET("/adakita/product")
    suspend fun product(@QueryMap map: Map<String, @JvmSuppressWildcards Any>): BaseResponse<ProductRes>

    @POST("/adakita/contact_list")
    suspend fun uploadContacts(@Body body: RequestBody): BaseResponse<Void>

    @NoNeedToken
    @GET("/adakita/loan_order/list")
    suspend fun getOrderList(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: Int
    ): BaseResponse<List<OrderRes>>

    @NoNeedToken
    @GET("/adakita/ad/imgs")
    suspend fun getAds(
        @Query("title") title: String
    ): BaseResponse<AdRes>

    @FormUrlEncoded
    @POST("/adakita/feedback/add")
    suspend fun feedback(@Field("content") content: String): BaseResponse<Void>

    @FormUrlEncoded
    @POST("/adakita/loan_order/amount/drop")
    suspend fun amountDrop(@Field("orderId") orderId: String): BaseResponse<Void>

    @NoNeedToken
    @GET("/adakita/faq")
    suspend fun getFaq(): BaseResponse<FaqRes>

    @GET("/adakita/user/info/status")
    suspend fun getUserInfoStatus(): BaseResponse<UserInfoStatusRes>

    @GET("/adakita/loan_order/order_processing")
    suspend fun orderProcessing(
        @Query("productId") productId: Int
    ): BaseResponse<OrderProcessingRes>

    @GET("/adakita/oss/sts")
    suspend fun ossSts(): BaseResponse<OssStsRes>

    @POST("/adakita/face_recognition")
    suspend fun getFaceCheckResult(): BaseResponse<FaceCheckRes>

    @GET("/adakita/product/api/trial")
    suspend fun amountTrial(
        @Query("id") id: Int,
        @Query("termUnit") termUnit: Int?,
        @Query("applicationAmount") applicationAmount: Int?,
        @Query("applicationTerm") applicationTerm: Int?
    ): BaseResponse<AmountTrialRes>

    @NoNeedToken
    @GET("/adakita/secret_key/get")
    suspend fun getLivenessSecretKey(): BaseResponse<LivenessSecretKeyRes>
}


fun dfApi() = RetrofitService.create(DanaApi::class.java)