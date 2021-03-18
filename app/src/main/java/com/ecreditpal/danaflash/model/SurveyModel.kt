package com.ecreditpal.danaflash.model

import com.alibaba.fastjson.JSON
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.SurveyHelper

class SurveyModel private constructor() {
    var p: String = ""// /orderPage
    var act: String = "" // in
    var code: String = "" // AR
    var pp: String = "" // 1609827248613;4
    var lastAct: String = "" // myOrder
    var lastP: String = ""// /my
    var prevAct: String = "" // myOrder
    var prevP: String = ""// /my
    var t: Long = 0// 1609827253786

    val referer: String = UserFace.referrerDetails?.installReferrer ?: "" // google_world
    val source: String = UserFace.mediaSource // google_world
    val deviceId: String = UserFace.gaid // 311b347a-2db0-479c-a368-11dfe282f88e

    //    val env: String = if (BuildConfig.DEBUG) "staging" else "production" // staging
    // FIXME: 2021/3/18 临时需要写死位staging
    val env: String = "staging"
    val m: String = UserFace.phone // 82113079928
    val type: String = "app" // app
    val v = 1 // 1
    val vestName: String = "DanaFlash" // TestApp

    companion object {
        //只能在SurveyHelper里面调用
        fun create(
            p: String,
            act: String,
            code: String,
        ): SurveyModel {
            return SurveyModel().apply {
                this.p = p
                this.act = act
                this.code = code

                pp = "${SurveyHelper.firstTramp};${SurveyHelper.index}"
                lastAct = SurveyHelper.lastAct
                lastP = SurveyHelper.lastP
                prevAct = SurveyHelper.prevAct
                prevP = SurveyHelper.prevP
                t = System.currentTimeMillis()
            }
        }
    }

    override fun toString(): String {
        return JSON.toJSONString(
            mapOf(
                "survey" to JSON.toJSONString(this)
            )
        )
    }


}