package com.ecreditpal.danaflash.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ecreditpal.danaflash.R
import retrofit2.HttpException
import java.io.IOException

@SuppressLint("Recycle")
class StatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var progressBar: ProgressBar
    private var message: TextView
    private var image: ImageView
    private var button: Button

    private var adapter: PagingDataAdapter<*, *>? = null

    init {
        inflate(context, R.layout.view_status, this).apply {
            progressBar = findViewById(R.id.progress_bar)
            message = findViewById(R.id.message)
            image = findViewById(R.id.image)
            button = findViewById(R.id.button)
        }
    }

    fun <T : PagingDataAdapter<*, *>> bindAdapter(
        pageAdapter: T,
        refreshLayout: SwipeRefreshLayout? = null
    ) {
        adapter = pageAdapter

        refreshLayout?.setOnRefreshListener {
            pageAdapter.refresh()
        }
        pageAdapter.addLoadStateListener {
            if (it.source.refresh !is LoadState.Loading) {
                refreshLayout?.isRefreshing = false
            }
            loadStatusChanged(it)
        }
    }

    private fun loadStatusChanged(combinedLoadStates: CombinedLoadStates) {
        if (adapter?.itemCount ?: 0 > 0) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE
        when (val loadState = combinedLoadStates.source.refresh) {
            is LoadState.Loading -> {
                progressBar.visibility = View.VISIBLE
                image.visibility = View.GONE
                button.visibility = View.GONE
                message.text = context.getText(R.string.loading)
            }
            is LoadState.Error -> {
                matchError(loadState.error)
            }
            is LoadState.NotLoading -> {
                //Show empty page
                progressBar.visibility = View.GONE
                button.visibility = View.GONE
                message.visibility = View.VISIBLE
                image.visibility = View.VISIBLE
                message.text = context.getString(R.string.content_empty)
                image.setImageResource(R.drawable.pic_empty)
            }
            else -> {
                visibility = View.GONE
            }
        }
    }

    /**
     * 显示错误.
     * @param function 点击按钮的行为
     */
    fun matchError(throwable: Throwable?, function: (() -> Unit)? = null) {
        visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        image.visibility = View.VISIBLE
        button.visibility = View.VISIBLE

        when (throwable) {
            is IOException -> {
                image.setImageResource(R.drawable.pic_failed_net)
                button.text = context.getString(R.string.retry)
                message.text = "Jaringan tidak berfungsi dengan baik, periksa jaringan dan coba lagi"
                button.setOnClickListener {
                    if (function != null) {
                        function.invoke()
                    } else {
                        adapter?.retry()
                    }
                }
            }
            is HttpException -> {
                image.setImageResource(R.drawable.pic_net_404)
                button.text = context.getString(R.string.back)
                message.text = "Tidak ditemukan"
                button.setOnClickListener {
                    if (function != null) {
                        function.invoke()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
            else -> {
                image.setImageResource(R.drawable.pic_net_buzy)
                button.text = context.getString(R.string.refresh)
                message.text = "Server sedang sibuk, coba lagi nanti"
                button.setOnClickListener {
                    if (function != null) {
                        function.invoke()
                    } else {
                        adapter?.refresh()
                    }
                }
            }
        }
    }

    fun showLoading() {
        visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        image.visibility = View.GONE
        button.visibility = View.GONE

        message.text = context.getText(R.string.loading)
    }

    fun hideStatus() {
        visibility = View.GONE
    }
}