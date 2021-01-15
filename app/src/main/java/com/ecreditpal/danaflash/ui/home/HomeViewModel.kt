package com.ecreditpal.danaflash.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ecreditpal.danaflash.data.PAGE_SIZE
import com.ecreditpal.danaflash.data.ProductPagingSource
import com.ecreditpal.danaflash.model.ProductUiModel
import kotlinx.coroutines.flow.map

class HomeViewModel : ViewModel() {
    val flow = Pager(
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        ProductPagingSource(
            mutableMapOf(
                "type" to 1,
                "orderType" to 1
            )
        )
    }.flow
        .map { pagingData -> pagingData.map { ProductUiModel.ProductItem(it) } }
        .map {
            it.insertSeparators { before, after ->
                if (after == null) {
                    // we're at the end of the list
                    return@insertSeparators null
                }

                if (before == null) {
                    // we're at the beginning of the list
                    return@insertSeparators ProductUiModel.BannerItem("banner desc")
                } else {
                    null
                }
            }
        }
        .cachedIn(viewModelScope)
}