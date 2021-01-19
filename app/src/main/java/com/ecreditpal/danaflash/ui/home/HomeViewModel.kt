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
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    /**
     * 广告
     * 第一部分string是指请求的title, 需要根据这个区分, 因为api和gp产品不需要弹窗, 只需要显示对应图片就行
     * 第二部分是具体的数据model
     */
    val adLiveData = MutableLiveData<Pair<String, AdRes>>()

    val apiFlow = buildFlow(ProductAdapter.PRODUCT_TYPE_API)
    val gpFlow = buildFlow(ProductAdapter.PRODUCT_TYPE_GP)
    private fun buildFlow(api: Int): Flow<PagingData<ProductRes.Product>> {
        return Pager(
            PagingConfig(pageSize = PAGE_SIZE)
        ) {
            ProductPagingSource(
                mutableMapOf(
                    "type" to 1,
                    "orderType" to 1,
                    "api" to api
                )
            )
        }.flow
            .cachedIn(viewModelScope)
    }

    /**
     * 检查是否有GP产品
     * @return true表示有GP产品
     *          false表示没有GP产品
     */
    suspend fun isGpSupported(): Boolean {
        return kotlin.runCatching {
            dfApi().product(
                mapOf(
                    "pageIndex" to PAGE_FIRST,
                    "pageSize" to 1,
                    "selectIds" to "",
                    "type" to 1,
                    "orderType" to 1,
                    "api" to 0
                )
            )
        }.getOrNull()?.data?.list.isNullOrEmpty().not()
    }

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
                adLiveData.value = Pair(title, it)
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
//            AD_TITLE_POP -> DataStoreKeys.AD_POP_LAST_STAMP
//            AD_TITLE_INDEX -> DataStoreKeys.AD_INDEX_LAST_STAMP
            AD_TITLE_PERSONALPOP -> DataStoreKeys.AD_PERSONALPOP_LAST_STAMP
            else -> return true // FIXME: 2021/1/19 暂时其他类型不检测, 允许请求
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