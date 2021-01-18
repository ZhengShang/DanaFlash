package com.ecreditpal.danaflash.ui.home

import DataStoreKeys
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.helper.danaRequest
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
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
            if (!checkRequestAdValid(title)) {
                return@launch
            }

            kotlin.runCatching {
                danaRequest {
                    dfApi().getAds(title).throwIfNotSuccess()
                }
            }.getOrNull()?.let {
                adLiveData.value = it
                writeLastAdShowStamp(title)
            }
        }
    }

    /**
     * 检查获取广告的请求是否合法, 如果距离上一次请求的日期不足一天, 则驳回请求
     * @return true表示此请求可以正常进行
     *          false表示不进行请求
     */
    private suspend fun checkRequestAdValid(title: String): Boolean {
        val key = when (title) {
            AD_TITLE_APIPOP -> DataStoreKeys.AD_APIPOP_LAST_STAMP
            AD_TITLE_POP -> DataStoreKeys.AD_POP_LAST_STAMP
            AD_TITLE_INDEX -> DataStoreKeys.AD_INDEX_LAST_STAMP
            AD_TITLE_PERSONALPOP -> DataStoreKeys.AD_PERSONALPOP_LAST_STAMP
            else -> throw IllegalArgumentException("No supported ad title")
        }

        val lastTramp = App.context.readDsData(key, 0)
        return System.currentTimeMillis() - lastTramp > 86_400_000 //ONE DAY
    }

    private suspend fun writeLastAdShowStamp(title: String) {
        val key = when (title) {
            AD_TITLE_APIPOP -> DataStoreKeys.AD_APIPOP_LAST_STAMP
            AD_TITLE_POP -> DataStoreKeys.AD_POP_LAST_STAMP
            AD_TITLE_INDEX -> DataStoreKeys.AD_INDEX_LAST_STAMP
            AD_TITLE_PERSONALPOP -> DataStoreKeys.AD_PERSONALPOP_LAST_STAMP
            else -> throw IllegalArgumentException("No supported ad title")
        }

        App.context.writeDsData(key, System.currentTimeMillis())
    }
}