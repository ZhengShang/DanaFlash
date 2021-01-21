package com.ecreditpal.danaflash.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.databinding.FragmentMyOrdersBinding
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog
import com.ecreditpal.danaflash.ui.comm.WebActivity
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
        orderViewModel.amountDropResult.observe(viewLifecycleOwner) {
            if (it) {
                ToastUtils.showLong(R.string.derating_success)
//                val cv = binding.viewFlipper.currentView
//                (cv as? SwipeRefreshLayout)?.let { refreshLayout ->
//                    refreshLayout.isRefreshing = true
//                }
                // TODO: 2021/1/21 may need to refresh the list
            } else {
                ToastUtils.showLong(R.string.failed_to_derating)
            }
        }
    }

    private fun generateList(status: Int): View {
        return View.inflate(context, R.layout.view_pull_list, null).apply {
            val adapter = OrderAdapter(clickListener)
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

    private val clickListener: (clickType: Int, orderRes: OrderRes) -> Unit =
        { clickType, orderRes ->
            when (clickType) {
                OrderAdapter.CLICK_CARD -> {
                    WebActivity.loadUrl(
                        context, H5_ORDER_DETAIL.combineH5Url(
                            mapOf(
                                "from" to "%2ForderPage",
                                "orderId" to orderRes
                            )
                        )
                    )
                }
                OrderAdapter.CLICK_LINE_BTN -> {
                    navLineBtn(orderRes)
                }
                OrderAdapter.CLICK_LEFT_BTN -> {
                    navRepayChoose(orderRes, 2)
                }
                OrderAdapter.CLICK_RIGHT_BTN -> {
                    navRepayChoose(orderRes, 1)
                }
            }
        }

    private fun navLineBtn(orderRes: OrderRes) {
        when (orderRes.status) {
            OrderStatus.STATUS_PUSHING -> {
                //点击后进入修改银行卡页面（h5页面）
                WebActivity.loadUrl(
                    context, H5_EDIT_BANK.combineH5Url(
                        mapOf(
                            "card_id" to orderRes.debitOpenBankId, // 银行卡号
                            "orderId" to orderRes.orderId, // 订单ID
                            "trackCode" to "", // 入口埋点（取埋点里code字段）
                            // TODO: 2021/1/21 track code
                        )
                    )
                )
            }
            OrderStatus.STATUS_CHECKED -> {
                //点击后弹出降额确认弹窗
                ConfirmDialog(titleStr = getString(R.string.derating_confirm),
                    positiveClickListener = {
                        orderViewModel.requestDrop(orderRes.orderId!!)
                    })
                    .show(childFragmentManager)
            }
            OrderStatus.STATUS_REPAYMENTING,
            OrderStatus.STATUS_OVERDUE -> {
                //点击后进入还款方式选择页（h5页面）
                navRepayChoose(orderRes, 1)
            }
            OrderStatus.STATUS_REPAYMENTED -> {
                //点击后进入该产品的订单确认页（h5页面）
                WebActivity.loadUrl(
                    context, H5_ORDER_CONFIRM.combineH5Url(
                        mapOf(
                            "id" to orderRes.productId, // 点击产品ID
                            "amount" to orderRes.currentRepaymentAmount, // 产品试算金额列表返回data数组里的最大值 fixme 试算金额
                            "productName" to orderRes.productName, // 点击的产品名称
                            "trackCode" to "", // 入口埋点（取埋点里code字段）
                            // TODO: 2021/1/21 track code
                        )
                    )
                )
            }
        }
    }

    private fun navRepayChoose(orderRes: OrderRes, payType: Int) {
        WebActivity.loadUrl(
            context, H5_PAY_ORDER.combineH5Url(
                mapOf(
                    "allowDelay" to orderRes.allowDelay,// 是否支持延期（0不支持，1支持）
                    "orderId" to orderRes.orderId,// 订单ID
                    "amount" to orderRes.currentRepaymentAmount,// 产品试算金额列表返回data数组里的最大值
                    "payType" to payType,// 还款类型（1还款，2延期）
                    "trackCode" to "" // 入口埋点（取埋点里code字段）
                    // TODO: 2021/1/21 track code
                )
            )
        )
    }
}