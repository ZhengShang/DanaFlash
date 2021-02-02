package com.ecreditpal.danaflash.model

import java.math.BigDecimal

data class ProductDetailRes(
    val actualAmount: Int?, // 518400
    val adminAmount: Int?, // 230400
    val amountMax: BigDecimal?, // 20000000.00
    val contactInfo: Boolean?, // false
    val defaultAmount: Int?, // 800000
    val defaultTerm: Int?, // 8
    val extraInfo: Any?, // null
    val hasCollect: Boolean?, // false
    val image: String?, // /staging/MemberData/eb68400c2b404d8d91b5cf792500aaa0
    val infoStatus: UserInfoStatusRes?,
    val interestAmount: Int?, // 51200
    val interestMin: Double?, // 0.200
    val loanRange: String?,
    val name: String?, // *PandaAI对接api
    val repayTotalAmount: Int?, // 800000
    val termUnit: Int // 1
)