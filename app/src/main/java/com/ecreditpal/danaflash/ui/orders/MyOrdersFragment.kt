package com.ecreditpal.danaflash.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.OrderStatus
import com.ecreditpal.danaflash.databinding.FragmentMyOrdersBinding
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyOrdersFragment : BaseFragment() {

    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyOrdersBinding.bind(view)
        binding.lifecycleOwner = this
        binding.vm = orderViewModel
        binding.viewFlipper.addView(generateList(OrderStatus.ALL))
        binding.viewFlipper.addView(generateList(OrderStatus.STATUS_REPAYMENTING))
        binding.viewFlipper.addView(generateList(OrderStatus.STATUS_REPAYMENTED))

        val status = activity?.intent?.extras?.let { OrdersActivityArgs.fromBundle(it).status } ?: 0
        orderViewModel.changeOrderStatus(status)
        orderViewModel.status.observe(viewLifecycleOwner) {
            binding.viewFlipper.displayedChild = when (it) {
                OrderStatus.ALL -> 0
                OrderStatus.STATUS_REPAYMENTING -> 1
                OrderStatus.STATUS_REPAYMENTED -> 2
                else -> 0
            }
        }
    }

    private fun generateList(status: Int): View {
        return View.inflate(context, R.layout.view_pull_list, null).apply {
            val adapter = OrderAdapter()
            val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
            findViewById<StatusView>(R.id.status_view).bindAdapter(adapter, swipeRefreshLayout)
            val recyclerView = findViewById<RecyclerView>(R.id.recycler)
            recyclerView.adapter = adapter
            lifecycleScope.launch {
                orderViewModel.getOrderByStatus(status).collectLatest {
                    adapter.submitData(it)
                }
            }
        } ?: View(context)
    }
}