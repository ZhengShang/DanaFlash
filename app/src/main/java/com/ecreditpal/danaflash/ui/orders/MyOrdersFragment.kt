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
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.databinding.FragmentMyOrdersBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.AmountTrialRes
import com.ecreditpal.danaflash.model.OrderRes
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog
import com.ecreditpal.danaflash.ui.comm.WebActivity
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyOrdersFragment : BaseFragment() {

    private lateinit var binding: FragmentMyOrdersBinding
    private val orderViewModel: OrderViewModel by viewModels()

    //保存三个adapter刷新用
    private val adapterList = mutableListOf<OrderAdapter>()
    private var amountTrialRes: AmountTrialRes? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.vm = orderViewModel
        binding.viewFlipper.addView(generateList(OrderStatus.ALL.toString()))
        binding.viewFlipper.addView(
            generateList(
                arrayOf(
                    OrderStatus.STATUS_LOAN_SUCCESS,
                    OrderStatus.STATUS_REPAYMENTING,
                    OrderStatus.STATUS_OVERDUE
                ).joinToString()
            )
        )
        binding.viewFlipper.addView(generateList(OrderStatus.STATUS_REPAYMENTED.toString()))

        val status =
            activity?.intent?.extras?.let { OrdersActivityArgs.fromBundle(it).status } ?: 0
        orderViewModel.changeOrderStatus(status)
        orderViewModel.status.observe(viewLifecycleOwner) {
            binding.viewFlipper.displayedChild = when (it) {
                OrderStatus.ALL -> 0
                OrderStatus.STATUS_REPAYMENTING -> 1
                OrderStatus.STATUS_REPAYMENTED -> 2
                else -> 0
            }
            //需求要求每次切换都刷新列表
            refreshList()
        }
        orderViewModel.amountDropResult.observe(viewLifecycleOwner) {
            if (it) {
                SurveyHelper.addOneSurvey("/orderPage", "derateSubmit", "AR")
                ToastUtils.showLong(R.string.derating_success)
                refreshList()
            } else {
                SurveyHelper.addOneSurvey("/orderPage", "derateSubmitFail", "AR")
                ToastUtils.showLong(R.string.failed_to_derating)
            }
        }
    }

    override fun onDestroy() {
        adapterList.clear()
        super.onDestroy()
    }

    private fun generateList(status: String): View {
        return View.inflate(context, R.layout.view_pull_list, null).apply {
            val adapter = OrderAdapter(clickListener)
            if (adapter !in adapterList) {
                adapterList.add(adapter)
            }
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

    private fun refreshList() {
        adapterList[binding.viewFlipper.displayedChild].refresh()
    }

    private val clickListener: (clickType: Int, orderRes: OrderRes) -> Unit =
        { clickType, orderRes ->
            when (clickType) {
                OrderAdapter.CLICK_CARD -> {
                    WebActivity.loadUrl(
                        context, H5_ORDER_DETAIL.combineH5Url(
                            mapOf(
                                "from" to "%2ForderPage",
                                "orderId" to orderRes.orderId
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
                SurveyHelper.addOneSurvey("/orderPage", "modifyBank", "AR")
                WebActivity.loadUrl(
                    context, H5_EDIT_BANK.combineH5Url(
                        mapOf(
                            "card_id" to orderRes.debitOpenBankId, // 银行卡号
                            "orderId" to orderRes.orderId, // 订单ID
                            "trackCode" to "AR", // 入口埋点（取埋点里code字段）
                        )
                    )
                )
            }
            OrderStatus.STATUS_CHECKED -> {
                //点击后弹出降额确认弹窗
                ConfirmDialog(titleStr = getString(R.string.derating_confirm),
                    negativeClickListener = {
                        SurveyHelper.addOneSurvey("/orderPage", "derateCancel", "AR")
                    },
                    positiveClickListener = {
                        orderViewModel.requestDrop(orderRes.orderId!!)
                    })
                    .apply {
                        isCancelable = false
                    }
                    .show(childFragmentManager)
                SurveyHelper.addOneSurvey("/orderPage", "derate", "AR")
            }
            OrderStatus.STATUS_REPAYMENTING,
            OrderStatus.STATUS_OVERDUE -> {
                //点击后进入还款方式选择页（h5页面）
                navRepayChoose(orderRes, 1)
            }
            OrderStatus.STATUS_REPAYMENTED -> {
                //再借一单
                //点击后进入该产品的订单确认页（h5页面）
                SurveyHelper.addOneSurvey("/orderPage", "onMoreOrder", "aj")
                navByUserInfoStatus(orderRes)
            }
        }
    }

    private fun navRepayChoose(orderRes: OrderRes, payType: Int) {
        val act = if (payType == 1) "ipay" else "idelay"
        SurveyHelper.addOneSurvey("/orderPage", act, "AR")

        if (payType == 1 && orderRes.repayLink.isNullOrEmpty().not()) {
            WebActivity.loadUrl(context, orderRes.repayLink)
            return
        } else if (payType == 2 && orderRes.delayLink.isNullOrEmpty().not()) {
            WebActivity.loadUrl(context, orderRes.delayLink)
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            calAmountTrial(orderRes)
            LoadingTips.dismissLoading()

            WebActivity.loadUrl(
                context, H5_PAY_ORDER.combineH5Url(
                    mapOf(
                        "allowDelay" to orderRes.allowDelay,// 是否支持延期（0不支持，1支持）
                        "orderId" to orderRes.orderId,// 订单ID
                        "amount" to amountTrialRes?.getMaxAmount(),// 产品试算金额列表返回data数组里的最大值
                        "payType" to payType,// 还款类型（1还款，2延期）
                        "trackCode" to "AR" // 入口埋点（取埋点里code字段）
                    )
                )
            )
        }
    }


    private fun navByUserInfoStatus(orderRes: OrderRes) {
        lifecycleScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().getUserInfoStatus()
            }
            LoadingTips.dismissLoading()

            if (res == null) {
                return@launch
            }

            LoadingTips.showLoading()
            calAmountTrial(orderRes)
            LoadingTips.dismissLoading()

            when {
                res.basicInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_BASE_INFO.combineH5Url(getH5Params(orderRes)))
                }
                res.faceRecognition == 2 -> {
                    livenessLauncher.launch(null)
                }
                res.faceRecognition == 3 -> {
                    ToastUtils.showLong(R.string.detection_failed_need_service)
                }
                res.emergencyInfo != 1 -> {
                    WebActivity.loadUrl(
                        context,
                        H5_CONTACT_PEOPLE.combineH5Url(getH5Params(orderRes))
                    )
                }
                res.bankInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_BANK_INFO.combineH5Url(getH5Params(orderRes)))
                }
                res.otherInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_OTHER_INFO.combineH5Url(getH5Params(orderRes)))
                }
                else -> {
                    WebActivity.loadUrl(
                        context,
                        H5_ORDER_CONFIRM.combineH5Url(getH5Params(orderRes))
                    )
                }
            }
        }
    }

    private fun getH5Params(orderRes: OrderRes) = mutableMapOf(
        "id" to orderRes.productId, // 点击产品ID
        "amount" to amountTrialRes?.getMaxAmount(), // 产品试算金额列表返回data数组里的最大值
        "productName" to orderRes.productName, // 点击的产品名称
        "trackCode" to "AR", // 入口埋点（取埋点里code字段）
    )

    private suspend fun calAmountTrial(orderRes: OrderRes) {
        amountTrialRes = danaRequestWithCatch {
            dfApi().amountTrial(
                orderRes.productId,
                orderRes.loanTermUnit ?: 0,
                orderRes.loanAmount?.intValueExact() ?: 0,
                orderRes.loanTerm ?: 0
            )
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        CommUtils.stepAfterLiveness(lifecycleScope, context, it)
    }

}