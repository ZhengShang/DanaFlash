package com.ecreditpal.danaflash.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class ProductAdapter :
    PagingDataAdapter<ProductRes.Product, BindingViewHolder<ItemProductBinding>>(DiffCallback()) {

    var clickListener: ((Int, product: ProductRes.Product?) -> Unit)? = null

    override fun onBindViewHolder(holder: BindingViewHolder<ItemProductBinding>, position: Int) {
        holder.binding.apply {
            info = getItem(position)
            loan.setOnClickListener {
                clickListener?.invoke(it.id, getItem(position))
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ItemProductBinding> {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.executePendingBindings()
        return BindingViewHolder(binding)
    }
}

internal class DiffCallback : DiffUtil.ItemCallback<ProductRes.Product>() {
    override fun areItemsTheSame(
        oldItem: ProductRes.Product,
        newItem: ProductRes.Product
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ProductRes.Product,
        newItem: ProductRes.Product
    ): Boolean {
        return oldItem == newItem
    }
}