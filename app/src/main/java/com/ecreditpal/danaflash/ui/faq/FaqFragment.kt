package com.ecreditpal.danaflash.ui.faq

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.helper.danaRequest
import com.ecreditpal.danaflash.model.FaqRes
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FaqFragment : BaseFragment() {

    private lateinit var statusView: StatusView
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_recycler_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(Color.WHITE)
        statusView = view.findViewById(R.id.status_view)

        val concatAdapterConfig = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(false)
            .build()
        concatAdapter = ConcatAdapter(concatAdapterConfig)

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            itemAnimator = ExpandableItemAnimator()
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            adapter = concatAdapter
        }

        getFaqData()
    }

    private fun getFaqData() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                statusView.showLoading()
                val res: FaqRes? = danaRequest {
                    dfApi().getFaq()
                }
                res?.list
                    ?.asSequence()
                    ?.filter { true }
                    ?.filter { it.title.isNullOrEmpty().not() }
                    ?.mapIndexed { index, faq ->
                        FaqRes.Faq(faq.content, "${index + 1}.${faq.title}")
                    }
                    ?.map {
                        FaqAdapter.FaqGroup(it.title!!, listOf(it.content))
                    }
                    ?.map { FaqAdapter(it) }
                    ?.onEach { it.isExpanded = false }
                    ?.toList()
                    ?.forEach { concatAdapter.addAdapter(it) }

                statusView.hideStatus()
            } catch (e: Exception) {
                statusView.matchError(e) { getFaqData() }
            }
        }
    }
}