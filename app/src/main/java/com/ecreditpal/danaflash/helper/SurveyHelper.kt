package com.ecreditpal.danaflash.helper

import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.model.SurveyModel

object SurveyHelper {

    val surveyList = mutableListOf<SurveyModel>()

    var lastAct: String = "" // 上一个页面的最后一个埋点
    var lastP: String = ""// /上一个页面名称
    var prevAct: String = "" // 上一个埋点的动作
    var prevP: String = ""// 上一个埋点所在页面名
    var firstTramp = 0L//第一个埋点时间戳
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
        val survey = SurveyModel.create(p, act, code)
        LogUtils.d("ADD SURVEY >>> $survey")
        surveyList.add(survey)
        index++

        prevAct = act
        prevP = p
        if (lastP != p) {
            lastP = p
            lastAct = act
        }
    }
}