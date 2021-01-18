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

    fun <T : PagingDataAdapter<*, *>> bindAdapter(pageAdapter: T) {
        adapter = pageAdapter
        pageAdapter.addLoadStateListener { loadStatusChanged(it) }
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
                progressBar.visibility = View.GONE
                image.visibility = View.VISIBLE
                button.visibility = View.VISIBLE
                message.text = loadState.error.message
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

    private fun matchError(throwable: Throwable?) {
        when (throwable) {
            is IOException -> {
                image.setImageResource(R.drawable.pic_failed_net)
                button.text = context.getString(R.string.retry)
                button.setOnClickListener { adapter?.retry() }
            }
            is HttpException -> {
                image.setImageResource(R.drawable.pic_net_404)
                button.text = context.getString(R.string.back)
                button.setOnClickListener { findNavController().popBackStack() }
            }
            else -> {
                image.setImageResource(R.drawable.pic_net_buzy)
                button.text = context.getString(R.string.refresh)
                button.setOnClickListener { adapter?.refresh() }
            }
        }
    }
}