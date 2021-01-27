package com.ecreditpal.danaflash.model

data class FaceCheckRes(
    val faceCheckBean: FaceCheckBean?
) {
    data class FaceCheckBean(
        val handle: String?, // SIMILARITY_NOT_PASS
        val livenessScore: Int?, // 99
        val msg: String?, // OK
        val similarity: String? // 25.000000
    )
}