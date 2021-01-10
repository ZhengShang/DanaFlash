package com.ecreditpal.danaflash.ui.home

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.model.ProductRes

class ProductAdapter : PagingDataAdapter<ProductRes.Product, ProductHolder>(DiffCallback()) {
    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        ItemProductBinding.bind(holder.itemView).info = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        return ProductHolder(parent)
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

class ProductHolder(view: View) : RecyclerView.ViewHolder(view) {

}