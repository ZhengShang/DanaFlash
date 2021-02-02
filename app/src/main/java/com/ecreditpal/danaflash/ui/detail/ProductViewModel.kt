package com.ecreditpal.danaflash.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.OrderProcessingRes
import com.ecreditpal.danaflash.model.ProductDetailRes
import com.ecreditpal.danaflash.model.UserInfoStatusRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    val userInfoStatus = MutableLiveData<UserInfoStatusRes>()
    val orderProcessingRes = MutableLiveData<OrderProcessingRes>()
    val detailRes = MutableLiveData<ProductDetailRes>()

    fun getProductDetail(id: Int) {
        viewModelScope.launch {
            detailRes.value = danaRequestWithCatch {
                dfApi().getProductDetail(id)
            } ?: return@launch
        }
    }

    fun fetchUserInfoStatus() {
        viewModelScope.launch {
            userInfoStatus.value = danaRequestWithCatch {
                dfApi().getUserInfoStatus()
            } ?: return@launch
        }
    }

    fun fetchOrderProcessing(productId: Int) {
        viewModelScope.launch {
            orderProcessingRes.value = danaRequestWithCatch {
                dfApi().orderProcessing(productId)
            } ?: return@launch
        }
    }
}