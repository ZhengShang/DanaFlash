package com.ecreditpal.danaflash.model

import java.math.BigDecimal

data class AmountTrialRes(
    val actualAmount: Int, // 650000
    val adminAmount: Int, // 350000
    val applicationAmount: Int, // 1000000
    val applicationTerm: Int?, // 7
    val contractDesc: String?, // kontrak konfirmasi antar muka
    val contractUrl: String?, // http://test-api.fulus.xyz/fluapi/contract/getcontract?contractFileName=confirmLoan1&applicationAmount=1000000&applicationTerm=7&termUnit=1&contractPage=1&mobile=888899996636&userIdcard=3703281304890006&userName=GGGHHHTO&productId=40000&sign=2b0fbc9d8902991b3d0177ee3ad00060
    val interestAmount: Int?, // 0
    val interestMin: BigDecimal?, // 0.002
    val repayTotalAmount: Int, // 1000000
    val termUnit: Int? // 1
) {

    fun getMaxAmount(): Int {
        return maxOf(actualAmount, adminAmount, applicationAmount, repayTotalAmount)
    }
}