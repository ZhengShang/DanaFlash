package com.ecreditpal.danaflash.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.blankj.utilcode.util.TimeUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.base.PopManager
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.helper.readDsData
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.model.AdRes
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    /**
     * 首页支持的产品数量, 分为3个数:
     * 0)表示只支持api产品,  默认打开的是api产品
     * 1)表示只支持gp产品
     * 2)表示两者都支持, 此时需要显示顶部的tab栏
     */
    val productSupportIndex = MutableLiveData<Int>()

    /**
     * 广告
     * 第一部分string是指请求的title, 需要根据这个区分, 因为api和gp产品不需要弹窗, 只需要显示对应图片就行
     * 第二部分是具体的数据model
     */
    val adLiveData = MutableLiveData<Pair<String, AdRes?>>()

    /**
     * 首页通过监听这个来显示不同的弹窗. 主要是更新弹窗, API和POP这3个
     */
    val showPopLiveData = MutableLiveData<Pair<Int, Any>>()

    /**
     * 首页的viewPager的index, 主要是为了避免在非HomeFragment里面显示弹窗
     */
    var currentPageIndex = 0

    val apiFlow = buildFlow(PRODUCT_TYPE_API)
    val gpFlow = buildFlow(PRODUCT_TYPE_GP)
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

    init {
        detectProductSupported()
    }

    /**
     * 检查API产品和GP产品是否都存在, 都存在就需要在顶部显示tab栏
     */
    fun detectProductSupported() {
        viewModelScope.launch {
            val apiSupported = async { singleRequest(PRODUCT_TYPE_API) }
            val gpSupported = async { singleRequest(PRODUCT_TYPE_GP) }

            productSupportIndex.value = if (apiSupported.await() && gpSupported.await()) {
                2
            } else if (gpSupported.await()) {
                1
            } else {
                0
            }
        }
    }

    /**
     * 是否支持当前产品, 即当前的请求是否有大于一个的item返回
     */
    private suspend fun singleRequest(api: Int): Boolean {
        return kotlin.runCatching {
            dfApi().product(
                mapOf(
                    "pageIndex" to PAGE_FIRST,
                    "pageSize" to 1,
                    "selectIds" to "",
                    "type" to 1,
                    "orderType" to 1,
                    "api" to api
                )
            )
        }.getOrNull()?.data?.list.isNullOrEmpty().not()
    }

    fun getAd(title: String) {
        viewModelScope.launch {
            if (!checkRequestAdValid(title)) {
                if (title == AD_TITLE_APIPOP) {
                    PopManager.addPopToMap(PopManager.TYPE_API, null)
                    adLiveData.value = Pair(title, null)
                } else if (title == AD_TITLE_POP) {
                    PopManager.addPopToMap(PopManager.TYPE_POP, null)
                    adLiveData.value = Pair(title, null)
                }
                return@launch
            }

            val res = danaRequestWithCatch {
                dfApi().getAds(title)
            }
            adLiveData.value = Pair(title, res)
            writeLastAdShowStamp(title)
        }
    }

    fun tryShowPopDialog() {
        showPopLiveData.value = PopManager.getNextShowPopValue() ?: return
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
//            AD_TITLE_INDEX -> DataStoreKeys.AD_INDEX_LAST_STAMP
            AD_TITLE_PERSONALPOP -> DataStoreKeys.AD_PERSONALPOP_LAST_STAMP
            else -> return true
        }

        val lastTramp = App.context.readDsData(key, 0)
        return !TimeUtils.isToday(lastTramp)
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