package com.ecreditpal.danaflash.ui.home

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.databinding.ViewHomeBannerBinding
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.model.ProductUiModel
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class ProductAdapter :
    PagingDataAdapter<ProductUiModel, BindingViewHolder<*>>(DiffCallback()) {

    var clickListener: ((Int, product: ProductRes.Product?) -> Unit)? = null

    override fun onBindViewHolder(holder: BindingViewHolder<*>, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is ProductUiModel.ProductItem -> {
                    (holder as BindingViewHolder<ItemProductBinding>).binding.apply {
                        info = uiModel.product
                        loan.setOnClickListener {
                            clickListener?.invoke(it.id, uiModel.product)
                        }
                    }
                }
                is ProductUiModel.BannerItem -> {
                    holder.itemView.setOnClickListener {
                        ToastUtils.showLong("navigate to h5 page")
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<*> {
        return if (viewType == R.layout.item_product) {
            BindingViewHolder.create<ItemProductBinding>(parent, R.layout.item_product)
        } else {
            BindingViewHolder.create<ViewHomeBannerBinding>(parent, R.layout.view_home_banner)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ProductUiModel.ProductItem -> R.layout.item_product
            is ProductUiModel.BannerItem -> R.layout.view_home_banner
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }
}

internal class DiffCallback : DiffUtil.ItemCallback<ProductUiModel>() {
    override fun areItemsTheSame(
        oldItem: ProductUiModel,
        newItem: ProductUiModel
    ): Boolean {
        return (oldItem is ProductUiModel.ProductItem && newItem is ProductUiModel.ProductItem &&
                oldItem.product.id == newItem.product.id) ||
                (oldItem is ProductUiModel.BannerItem && newItem is ProductUiModel.BannerItem &&
                        oldItem.description == newItem.description)
    }

    override fun areContentsTheSame(
        oldItem: ProductUiModel,
        newItem: ProductUiModel
    ): Boolean {
        return oldItem == newItem
    }
}