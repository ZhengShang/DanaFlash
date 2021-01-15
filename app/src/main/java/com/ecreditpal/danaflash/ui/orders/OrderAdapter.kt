package com.ecreditpal.danaflash.ui.orders

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemOrderBinding
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class OrderAdapter :
    PagingDataAdapter<OrderRes, BindingViewHolder<ItemOrderBinding>>(DiffCallback()) {

    override fun onBindViewHolder(holder: BindingViewHolder<ItemOrderBinding>, position: Int) {
        holder.binding.apply {
            info = getItem(position)
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