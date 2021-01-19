package com.ecreditpal.danaflash.ui.home

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class ProductAdapter(
    private val productType: Int
) :
    PagingDataAdapter<ProductRes.Product, BindingViewHolder<ItemProductBinding>>(PRODUCT_COMPARATOR) {

    var productClick: ((Int, product: ProductRes.Product?) -> Unit)? = null


    override fun onBindViewHolder(holder: BindingViewHolder<ItemProductBinding>, position: Int) {
        holder.binding.apply {
            type = productType
            info = getItem(position)
            loan.setOnClickListener {
                productClick?.invoke(it.id, getItem(position))
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ItemProductBinding> {
        return BindingViewHolder.create(parent, R.layout.item_product)
    }


    companion object {
        const val PRODUCT_TYPE_API = 1
        const val PRODUCT_TYPE_GP = 0

        private val PRODUCT_COMPARATOR = object : DiffUtil.ItemCallback<ProductRes.Product>() {
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
    }
}
