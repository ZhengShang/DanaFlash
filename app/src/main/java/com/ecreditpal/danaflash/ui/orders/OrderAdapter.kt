package com.ecreditpal.danaflash.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
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
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.executePendingBindings()
        return BindingViewHolder(binding)
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