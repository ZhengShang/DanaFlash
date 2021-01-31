package com.ecreditpal.danaflash.model

import android.graphics.Color
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.data.OrderStatus.ALL
import com.ecreditpal.danaflash.data.OrderStatus.APP_STATUS_ORDER_CANCEL
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_APPLY_FAILED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_AUDIT_FAILED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_AUDIT_SUCCESS
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_CANCELED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_CHECKED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_LOAN_FAILED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_LOAN_SUCCESS
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_MANUAL_AUDIT
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_OVERDUE
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_PUSHING
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_REPAYMENTED
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_REPAYMENTING
import com.ecreditpal.danaflash.data.OrderStatus.STATUS_VERIFICATION_FAILED
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
    val productId: Int, // 292
    val productName: String?, // Butik Ajaib &Punya Duit对接
    val pstatus: Int?, // 1
    val repaymentTime: String?, // 2020-12-22
    val status: Int?, // 6
    val storeList: List<Store?>?,
    var repayLink: String?,
    var delayLink: String?
) {

    fun loanTermString(): String {
        return loanTerm.toString().plus(
            if (loanTermUnit == 0) {
                App.context.getString(R.string.month)
            } else {
                App.context.getString(R.string.day)
            }
        )
    }

    fun statusStringRes() = when (status) {
        ALL -> R.string.order_status_all
        STATUS_PUSHING -> R.string.order_status_pushing
        STATUS_AUDIT_FAILED -> R.string.order_status_audit_failed
        STATUS_APPLY_FAILED -> R.string.order_status_apply_failed
        STATUS_LOAN_SUCCESS -> R.string.order_status_loan_success
        STATUS_REPAYMENTING -> R.string.order_status_repaymenting
        STATUS_OVERDUE -> R.string.order_status_overdue
        STATUS_REPAYMENTED -> R.string.order_status_repaymented
        STATUS_CHECKED -> R.string.order_status_checked
        STATUS_AUDIT_SUCCESS -> R.string.order_status_audit_success
        STATUS_CANCELED -> R.string.order_status_canceled
        STATUS_VERIFICATION_FAILED -> R.string.order_status_verification_failed
        STATUS_LOAN_FAILED -> R.string.order_status_loan_failed
        STATUS_MANUAL_AUDIT -> R.string.order_status_manual_audit
        APP_STATUS_ORDER_CANCEL -> R.string.order_app_status_order_cancel
        else -> R.string.empty_string
    }

    fun statusColor() = when (status) {
        STATUS_MANUAL_AUDIT,
        STATUS_AUDIT_SUCCESS -> Color.parseColor("##7ED321")
        STATUS_CHECKED,
        STATUS_OVERDUE -> Color.parseColor("#E59A37")
        STATUS_REPAYMENTING -> Color.parseColor("#E84F4F")
        STATUS_REPAYMENTED -> Color.parseColor("#333333")
        else -> Color.parseColor("#999999")
    }

    /**
     * 到账银行卡, 应还金额, 到期日等是否显示
     */
    fun moreInfoVisible() = status == 5 || status == 6 || status == 7

    fun lineBtnVisible(): Boolean {
        return (status == 1 && fillStatus == 1)
                || status == 8
                || (status == 5 && allowDelay == 0)
                || (status == 6 && allowDelay == 0)
                || (status == 7 && cooperation == 1)
    }

    fun lineBtnTextRes(): Int {
        return when {
            (status == 1 && fillStatus == 1) -> R.string.rebind_card
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

    fun showBankName(): String {
        val last = debitBankCard?.takeLast(4) ?: ""
        return "$debitBankName($last)"
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