package com.ecreditpal.danaflash.ui.orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.OrderPagingSource
import com.ecreditpal.danaflash.data.PAGE_SIZE
import com.ecreditpal.danaflash.helper.danaRequestResult
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    val status = MutableLiveData<Int>()
    val amountDropResult = MutableLiveData<Boolean>()

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

    fun requestDrop(orderId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestResult {
                dfApi().amountDrop(orderId)
            }
            LoadingTips.dismissLoading()
            amountDropResult.value = res
        }
    }
}