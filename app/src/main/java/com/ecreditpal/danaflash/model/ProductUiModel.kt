package com.ecreditpal.danaflash.model

sealed class ProductUiModel {
    data class ProductItem(val product: ProductRes.Product) : ProductUiModel()
    data class BannerItem(val description: String) : ProductUiModel()
}