package com.ecreditpal.danaflash.data

import androidx.paging.PagingSource
import com.ecreditpal.danaflash.helper.danaRequest
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.net.dfApi
import retrofit2.HttpException
import java.io.IOException

class ProductPagingSource(
    private val queryMap: MutableMap<String, Any>
) : PagingSource<Int, ProductRes.Product>() {

    private var _selectIds: List<Int?> = emptyList()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductRes.Product> {
        try {
            val nextPageNumber = params.key ?: PAGE_FIRST
            val productRes = danaRequest {
                dfApi().product(queryMap.apply {
                    put("pageIndex", nextPageNumber)
                    put("pageSize", PAGE_SIZE)
                    put("selectIds", _selectIds.joinToString())
                })
            }
            _selectIds = productRes?.selectIds ?: emptyList<Int>()
            return LoadResult.Page(
                data = productRes?.list ?: emptyList(),
                prevKey = null, // Only paging forward.
                nextKey = if (productRes?.list.isNullOrEmpty()) null else nextPageNumber + 1
            )
        } catch (e: IOException) {
            // IOException for network failures.
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            // HttpException for any non-2xx HTTP status codes.
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}