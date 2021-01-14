package com.ecreditpal.danaflash.ui.orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ecreditpal.danaflash.data.OrderPagingSource
import com.ecreditpal.danaflash.data.PAGE_SIZE
import com.ecreditpal.danaflash.model.OrderRes
import kotlinx.coroutines.flow.Flow

class OrderViewModel : ViewModel() {
    val status = MutableLiveData<Int>()

    fun getOrderByStatus(status: Int): Flow<PagingData<OrderRes>> {
        return Pager(
            PagingConfig(pageSize = PAGE_SIZE)
        ) {
            OrderPagingSource(status)
        }.flow
            .cachedIn(viewModelScope)
    }

    fun changeOrderStatus(status: Int) {
        this.status.value = status
    }
}