package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.ui.comm.CommLoadStateAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel: HomeViewModel by viewModels()
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

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            setHasFixedSize(true)
            adapter = pageAdapter
        }
        lifecycleScope.launch(Dispatchers.IO) {
            homeViewModel.flow.collectLatest { pagingData ->
                pageAdapter.submitData(pagingData)
            }
        }
    }
}