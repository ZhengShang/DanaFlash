package com.ecreditpal.danaflash.model

import java.math.BigDecimal

data class OrderRes(
    val allowDelay: Int?, // 1
    val api: Boolean?, // true
    val bankList: List<Bank?>?,
    val cooperation: Int?, // 1
    val createTime: String?, // 2020-12-15
    val createTimeForOrder: String?, // 2020-12-15T02:51:20.000+0000
    val currentRepaymentAmount: BigDecimal?, // 1850000
    val debitBankCard: String?, // 349649494977
    val debitBankName: String?, // Bank BRI
    val debitOpenBankId: Int?, // 2
    val fillStatus: Int?, // 0
    val img: String?, // https://tropic-hongkong.oss-cn-hongkong.aliyuncs.com/kp/staging/MemberData/124005ce13574ac2aca36469649b9f0a
    val loanAmount: BigDecimal?, // 1000000
    val loanTerm: Int?, // 8
    val loanTermUnit: Int?, // 1
    val orderId: String?, // 202012151051206319
    val productId: Int?, // 292
    val productName: String?, // Butik Ajaib &Punya Duit对接
    val pstatus: Int?, // 1
    val repaymentTime: String?, // 2020-12-22
    val status: Int?, // 6
    val storeList: List<Store?>?
) {

    fun loanTermString(): String {
        // TODO: 2021/1/14 maybe need translate
        return loanTerm.toString().plus(
            if (loanTermUnit == 0) {
                "Month"
            } else {
                "Day"
            }
        )
    }

    data class Bank(
        val bankId: Int?, // 13
        val name: String? // Bank Permata
    )

    data class Store(
        val name: String?, // Alfamart
        val storeId: Int? // 2001
    )
}