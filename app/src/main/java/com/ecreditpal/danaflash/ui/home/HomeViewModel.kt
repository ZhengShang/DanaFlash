package com.ecreditpal.danaflash.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.ecreditpal.danaflash.data.PAGE_SIZE
import com.ecreditpal.danaflash.data.ProductPagingSource

class HomeViewModel : ViewModel() {
    val flow = Pager(
        // Configure how data is loaded by passing additional properties to
        // PagingConfig, such as prefetchDistance.
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        ProductPagingSource(
            mutableMapOf(
                "type" to 1,
                "orderType" to 1,
                "selectIds" to emptyArray<Int>()
            )
        )
    }.flow
        .cachedIn(viewModelScope)
}