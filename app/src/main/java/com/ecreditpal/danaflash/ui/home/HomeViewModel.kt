package com.ecreditpal.danaflash.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ecreditpal.danaflash.data.PAGE_SIZE
import com.ecreditpal.danaflash.data.ProductPagingSource
import com.ecreditpal.danaflash.helper.danaRequest
import com.ecreditpal.danaflash.model.AdRes
import com.ecreditpal.danaflash.model.ProductUiModel
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val adLiveData = MutableLiveData<AdRes>()

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

    fun getAd(title: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                danaRequest {
                    dfApi().getAds(title)
                }
            }.getOrNull()?.let {
                adLiveData.value = it
            }
        }
    }
}