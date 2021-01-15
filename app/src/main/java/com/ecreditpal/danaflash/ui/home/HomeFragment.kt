package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.ui.comm.CommLoadStateAdapter
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_pull_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel: HomeViewModel by activityViewModels()
        val pageAdapter = ProductAdapter().apply {
            withLoadStateFooter(CommLoadStateAdapter(this::retry))
            clickListener = { viewId, product ->
                if (viewId == R.id.loan && product != null) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionNavigationHomeToProductActivity(product)
                    )
                }
            }
        }

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        refreshLayout.setOnRefreshListener { pageAdapter.refresh() }

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            setHasFixedSize(true)
            adapter = pageAdapter
        }

        view.findViewById<StatusView>(R.id.status_view).bindAdapter(pageAdapter)

        lifecycleScope.launch {
            homeViewModel.flow.collectLatest { pagingData ->
                pageAdapter.submitData(pagingData)
                refreshLayout.isRefreshing = false
            }
        }
    }
}