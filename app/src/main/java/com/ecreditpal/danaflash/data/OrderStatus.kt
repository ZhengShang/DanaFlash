package com.ecreditpal.danaflash.data

object OrderStatus {
    const val ALL = 0 // 所有状态
    const val STATUS_PUSHING = 1 // 提交成功
    const val STATUS_AUDIT_FAILED = 2 // 审批不通过
    const val STATUS_APPLY_FAILED = 3 // 申请失败
    const val STATUS_LOAN_SUCCESS = 4 // 放款成功
    const val STATUS_REPAYMENTING = 5 // 还款中
    const val STATUS_OVERDUE = 6 // 已逾期
    const val STATUS_REPAYMENTED = 7 // 已结清
    const val STATUS_CHECKED = 8 // 待确认
    const val STATUS_AUDIT_SUCCESS = 9 // 审批成功
    const val STATUS_CANCELED = 10 // 贷款取消
    const val STATUS_VERIFICATION_FAILED = 12 // 验证失败
    const val STATUS_LOAN_FAILED = 13 // 放款失败
    const val STATUS_MANUAL_AUDIT = 14 // 人工审核中
    const val APP_STATUS_ORDER_CANCEL = 15 // 订单取消
}