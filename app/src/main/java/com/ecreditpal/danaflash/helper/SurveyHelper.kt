package com.ecreditpal.danaflash.helper

import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.model.SurveyModel
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

object SurveyHelper {

//    val surveyList = mutableListOf<SurveyModel>()

    var lastAct: String = "" // 上一个页面的最后一个埋点
    var lastP: String = ""// /上一个页面名称
    var prevAct: String = "" // 上一个埋点的动作
    var prevP: String = ""// 上一个埋点所在页面名
    var firstTramp = 0L//第一个埋点时间戳
    var lastCode = "" // 如果下一个埋点没有code ,则需要回传上一个埋点的code
    var index = 0 //第几个埋点
        get() {
            if (field == 0) {
                firstTramp = System.currentTimeMillis()
            }
            return field
        }

    fun addOneSurvey(
        p: String,
        act: String,
        code: String = "",
    ) {
        if (code.isNotBlank()) {
            lastCode = code
        }
        val survey = SurveyModel.create(p, act, lastCode)
        LogUtils.d("ADD SURVEY >>> $survey")
//        surveyList.add(survey)
        index++

        prevAct = act
        prevP = p
        if (lastP != p) {
            lastP = p
            lastAct = act
        }

        uploadSurvey(survey)
    }

    private fun uploadSurvey(survey: SurveyModel) {
        val json = """
            {
                "__topic__": "survey",
                "__logs__": [$survey]
            }
        """.trimIndent()

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), json
        )

        GlobalScope.launch {
            kotlin.runCatching {
                dfApi().uploadSurvey(
                    "http://tropic.cn-hongkong.log.aliyuncs.com/logstores/staging/track",
                    body.contentLength(),
                    body
                )
            }
        }
    }
}