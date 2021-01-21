package com.ecreditpal.danaflash.model

import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.data.OrderStatus
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

    fun statusStringRes() = when (status) {
        OrderStatus.ALL -> R.string.OrderStatus.STATUS_PUSHING
        -> R.string.OrderStatus.STATUS_AUDIT_FAILED
        -> R.string.OrderStatus.STATUS_APPLY_FAILED
        -> R.string.OrderStatus.STATUS_LOAN_SUCCESS
        -> R.string.OrderStatus.STATUS_REPAYMENTING
        -> R.string.OrderStatus.STATUS_OVERDUE
        -> R.string.OrderStatus.STATUS_REPAYMENTED
        -> R.string.OrderStatus.STATUS_CHECKED
        -> R.string.OrderStatus.STATUS_AUDIT_SUCCESS
        -> R.string.OrderStatus.STATUS_CANCELED
        -> R.string.OrderStatus.STATUS_VERIFICATION_FAILED
        -> R.string.OrderStatus.STATUS_LOAN_FAILED
        -> R.string.OrderStatus.STATUS_MANUAL_AUDIT
        -> R.string.OrderStatus.APP_STATUS_ORDER_CANCEL
        -> R.string.
        else -> R.string.empty_string
    }

    /**
     * 到账银行卡, 应还金额, 到期日等是否显示
     */
    fun moreInfoVisible() = status == 5 || status == 6 || status == 7

    fun lineBtnVisible(): Boolean {
        return (status == 1 && fillStatus == 0)
                || status == 8
                || (status == 5 && allowDelay == 0)
                || (status == 6 && allowDelay == 0)
                || (status == 7 && cooperation == 1)
    }

    fun lineBtnTextRes(): Int {
        return when {
            (status == 1 && fillStatus == 0) -> R.string.rebind_card
            status == 8 -> R.string.derating_confirm
            (status == 5 && allowDelay == 0) ||
                    (status == 6 && allowDelay == 0) -> R.string.i_want_to_repay
            (status == 7 && cooperation == 1) -> R.string.borrow_another_order
            else -> R.string.empty_string
        }
    }

    fun twoBtnVisible(): Boolean {
        return (status == 5 && allowDelay == 1)
                || (status == 5 && allowDelay == 1)
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