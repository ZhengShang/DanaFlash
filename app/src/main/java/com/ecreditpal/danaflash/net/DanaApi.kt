package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.model.*
import okhttp3.RequestBody
import retrofit2.http.*

interface DanaApi {

    @NoNeedToken
    @FormUrlEncoded
    @POST(" /danaflash/sms_login/vcode")
    suspend fun getVCode(
        @Field("phone") phone: String,
        @Field("code") code: String = "62"
    ): BaseResponse<Void>

    @NoNeedToken
    @FormUrlEncoded
    @POST(" /danaflash/sms_login")
    suspend fun login(
        @Field("phone") phone: String,
        @Field("deviceId") deviceId: String,
        @Field("vcode") vcode: String,
        @Field("code") code: String = "62"
    ): BaseResponse<LoginRes>

    @NoNeedToken
    @GET(" /danaflash/version/manage")
    suspend fun versionMange(
        @Query("channel") channel: String,
        @Query("version") version: String = BuildConfig.VERSION_NAME,
        @Query("versionCode") versionCode: Int = BuildConfig.VERSION_CODE
    ): BaseResponse<VersionRes>

    @NoNeedToken
    @GET(" /danaflash/product")
    suspend fun product(@QueryMap map: Map<String, @JvmSuppressWildcards Any>): BaseResponse<ProductRes>

    @POST(" /danaflash/contact_list")
    suspend fun uploadContacts(@Body body: RequestBody): BaseResponse<Void>

    @NoNeedToken
    @GET(" /danaflash/loan_order/list")
    suspend fun getOrderList(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String
    ): BaseResponse<List<OrderRes>>

    @NoNeedToken
    @GET(" /danaflash/ad/imgs")
    suspend fun getAds(
        @Query("title") title: String
    ): BaseResponse<AdRes>

    @FormUrlEncoded
    @POST(" /danaflash/feedback/add")
    suspend fun feedback(@Field("content") content: String): BaseResponse<Void>

    @FormUrlEncoded
    @POST(" /danaflash/loan_order/amount/drop")
    suspend fun amountDrop(@Field("orderId") orderId: String): BaseResponse<Void>

    @NoNeedToken
    @GET(" /danaflash/faq")
    suspend fun getFaq(): BaseResponse<FaqRes>

    @GET(" /danaflash/user/info/status")
    suspend fun getUserInfoStatus(): BaseResponse<UserInfoStatusRes>

    @GET(" /danaflash/loan_order/order_processing")
    suspend fun orderProcessing(
        @Query("productId") productId: Int
    ): BaseResponse<OrderProcessingRes>

    @GET(" /danaflash/oss/sts")
    suspend fun ossSts(): BaseResponse<OssStsRes>

    @FormUrlEncoded
    @POST(" /danaflash/face_recognition")
    suspend fun getFaceCheckResult(
        @Field("livenessId") livenessId: String
    ): BaseResponse<FaceCheckRes>

    @GET(" /danaflash/product/api/trial")
    suspend fun amountTrial(
        @Query("id") id: Int,
        @Query("termUnit") termUnit: Int?,
        @Query("applicationAmount") applicationAmount: Int?,
        @Query("applicationTerm") applicationTerm: Int?
    ): BaseResponse<AmountTrialRes>

    @NoNeedToken
    @GET(" /danaflash/secret_key/get")
    suspend fun getLivenessSecretKey(): BaseResponse<LivenessSecretKeyRes>

    @NoNeedToken
    @Headers("x-log-apiversion: 0.6.0")
    @POST(" /danaflash/survey")
    suspend fun uploadSurvey(
        @Header("x-log-bodyrawsize") bodyrawsize: Long,
        @Body body: RequestBody
    ): BaseResponse<Void>

    @POST(" /danaflash/user/filter_product")
    suspend fun filterProduct(): BaseResponse<Void>

    @FormUrlEncoded
    @POST(" /danaflash/device_detail")
    suspend fun uploadAllDeviceInfo(
        @Field("isEncrypt") isEncrypt: Int,
        @Field("deviceDetail") deviceDetail: String
    ): BaseResponse<Void>
}


fun dfApi() = RetrofitService.create(DanaApi::class.java)