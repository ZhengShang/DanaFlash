package com.ecreditpal.danaflash.model

data class UserInfoStatusRes(
    val bankInfo: Int?, // 1
    val basicInfo: Int?, // 1
    val emergencyInfo: Int?, // 1
    val faceRecognition: Int?, // 1
    val identityComplete: Int?, // 0
    val ocrComplete: Int?, // 1
    val otherInfo: Int? // 1
) {
    fun isBaseInfoVerified() = basicInfo == 1 && faceRecognition == 1

    fun isOtherInfoVerified() = emergencyInfo == 1 && bankInfo == 1 && otherInfo == 1
}