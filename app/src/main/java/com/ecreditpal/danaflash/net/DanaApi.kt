package com.ecreditpal.danaflash.net

import com.ecreditpal.danaflash.model.BaseResponse
import com.ecreditpal.danaflash.model.LoginRes
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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

}


fun dfApi() = RetrofitService.create(DanaApi::class.java)