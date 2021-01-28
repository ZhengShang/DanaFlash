package com.ecreditpal.danaflash.model

//0 未完成 1 已完成 2 去修正
data class UserInfoStatusRes(
    val bankInfo: Int?, // 1
    val basicInfo: Int?, // 1
    val emergencyInfo: Int?, // 1
    val faceRecognition: Int?, // 1
    val identityComplete: Int?, // 0
    val ocrComplete: Int?, // 1
    val otherInfo: Int? // 1
) {
    fun isBaseInfoCompleted() = basicInfo == 1 && faceRecognition == 1 && ocrComplete == 1

    fun isBaseToFix() = faceRecognition == 2

    fun isOtherInfoComplete() = emergencyInfo == 1 && bankInfo == 1 && otherInfo == 1

    fun isOtherToFix() = emergencyInfo == 2 || bankInfo == 2 || otherInfo == 2
}