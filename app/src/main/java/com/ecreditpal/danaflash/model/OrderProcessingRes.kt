package com.ecreditpal.danaflash.model

data class OrderProcessingRes(
    val orderId: Int,
    val status: Int? // 1
) {
    fun statusString() = when (status) {
        0 -> "Pinjaman online"
        1 -> "Cap Limit"
        2 -> "Lihat pinjaman"
        else -> ""
    }
}