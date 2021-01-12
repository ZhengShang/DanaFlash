package com.ecreditpal.danaflash.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.model.ProductRes

class ProductAdapter : PagingDataAdapter<ProductRes.Product, ProductHolder>(DiffCallback()) {

    var clickListener: ((Int, product: ProductRes.Product?) -> Unit)? = null

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        holder.binding.apply {
            info = getItem(position)
            loan.setOnClickListener {
                clickListener?.invoke(it.id, getItem(position))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.executePendingBindings()
        return ProductHolder(binding)
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

class ProductHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)