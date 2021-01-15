package com.ecreditpal.danaflash.data

import androidx.paging.PagingSource
import com.ecreditpal.danaflash.helper.danaRequest
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.net.dfApi
import retrofit2.HttpException
import java.io.IOException

class OrderPagingSource(
    private val orderStatus: Int
) : PagingSource<Int, OrderRes>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OrderRes> {
        try {
            val nextPageNumber = params.key ?: PAGE_FIRST
            val orderList = danaRequest {
                dfApi().getOrderList(
                    pageIndex = nextPageNumber,
                    pageSize = PAGE_SIZE,
                    orderStatus
                )
            }
            return LoadResult.Page(
                data = orderList ?: emptyList(),
                prevKey = null, // Only paging forward.
                nextKey = if (orderList.isNullOrEmpty()) null else nextPageNumber + 1
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