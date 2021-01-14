package com.ecreditpal.danaflash.data

import androidx.paging.PagingSource
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class OrderPagingSource(
    private val orderStatus: Int
) : PagingSource<Int, OrderRes>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OrderRes> {
        try {
            val nextPageNumber = params.key ?: PAGE_FIRST
            val response = withContext(Dispatchers.IO) {
                dfApi().getOrderList(
                    pageIndex = nextPageNumber,
                    pageSize = PAGE_SIZE,
                    orderStatus
                )
            }
            return LoadResult.Page(
                data = response.data ?: emptyList(),
                prevKey = null, // Only paging forward.
                nextKey = if (response.data.isNullOrEmpty()) null else nextPageNumber + 1
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