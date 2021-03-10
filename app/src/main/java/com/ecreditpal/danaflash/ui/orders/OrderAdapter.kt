package com.ecreditpal.danaflash.ui.orders

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemOrderBinding
import com.ecreditpal.danaflash.helper.isEllipsized
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class OrderAdapter(
    private val clickListener: ((clickType: Int, orderRes: OrderRes) -> Unit)
) :
    PagingDataAdapter<OrderRes, BindingViewHolder<ItemOrderBinding>>(DiffCallback()) {

    companion object {
        const val CLICK_CARD = 0
        const val CLICK_LINE_BTN = 1
        const val CLICK_LEFT_BTN = 2
        const val CLICK_RIGHT_BTN = 3
    }

    override fun onBindViewHolder(holder: BindingViewHolder<ItemOrderBinding>, position: Int) {
        val orderRes = getItem(holder.layoutPosition) ?: return
        holder.binding.apply {
            info = orderRes
            bankAccountValue.text = if (bankAccount.isEllipsized())
                orderRes.showOmitBankName() else orderRes.showBankName()
            root.setOnClickListener { clickListener.invoke(CLICK_CARD, orderRes) }
            lineButton.setOnClickListener { clickListener.invoke(CLICK_LINE_BTN, orderRes) }
            leftButton.setOnClickListener { clickListener.invoke(CLICK_LEFT_BTN, orderRes) }
            rightButton.setOnClickListener { clickListener.invoke(CLICK_RIGHT_BTN, orderRes) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ItemOrderBinding> {
        return BindingViewHolder.create(parent, R.layout.item_order)
    }
}

internal class DiffCallback : DiffUtil.ItemCallback<OrderRes>() {
    override fun areItemsTheSame(
        oldItem: OrderRes,
        newItem: OrderRes
    ): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(
        oldItem: OrderRes,
        newItem: OrderRes
    ): Boolean {
        return oldItem == newItem
    }

}