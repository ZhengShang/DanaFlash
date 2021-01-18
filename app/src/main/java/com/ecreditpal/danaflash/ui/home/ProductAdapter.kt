package com.ecreditpal.danaflash.ui.home

import android.view.ViewGroup
import androidx.databinding.ObservableInt
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemProductBinding
import com.ecreditpal.danaflash.databinding.ViewHomeBannerBinding
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.model.ProductUiModel
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class ProductAdapter :
    PagingDataAdapter<ProductUiModel, BindingViewHolder<*>>(DiffCallback()) {

    val productType = ObservableInt(PRODUCT_TYPE_API)
    var bannerClick: ((clickType: Int) -> Unit)? = null
    var productClick: ((Int, product: ProductRes.Product?) -> Unit)? = null

    companion object {
        const val PRODUCT_TYPE_ALL = 0
        const val PRODUCT_TYPE_API = 1
        const val PRODUCT_TYPE_GP = 2
        const val CLICK_TYPE_BANNER = 0
        const val CLICK_TYPE_API = 1
        const val CLICK_TYPE_GP = 2
    }


    override fun onBindViewHolder(holder: BindingViewHolder<*>, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is ProductUiModel.ProductItem -> {
                    (holder as BindingViewHolder<ItemProductBinding>).binding.apply {
                        info = uiModel.product
                        loan.setOnClickListener {
                            productClick?.invoke(it.id, uiModel.product)
                        }
                    }
                }
                is ProductUiModel.BannerItem -> {
                    (holder as BindingViewHolder<ViewHomeBannerBinding>).binding.apply {
                        type = productType
                        root.setOnClickListener { bannerClick?.invoke(CLICK_TYPE_BANNER) }
                        tabApi.setOnClickListener { bannerClick?.invoke(CLICK_TYPE_API) }
                        tabGp.setOnClickListener { bannerClick?.invoke(CLICK_TYPE_GP) }
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