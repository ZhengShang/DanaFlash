package com.ecreditpal.danaflash.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.databinding.FragmentProductDetailBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.*
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.comm.WebActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentProductDetailBinding
    private val productViewModel: ProductViewModel by viewModels()

    private var productDetailRes: ProductDetailRes? = null
    private var amountTrialRes: AmountTrialRes? = null
    private var productId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product: ProductRes.Product? = activity?.intent?.extras?.let { ProductActivityArgs.fromBundle(it).product }
        if (product == null) {
            activity?.finish()
            return
        }
        productId = product.id ?: 0

        binding.product = product
        binding.vm = productViewModel


        productViewModel.detailRes.observe(viewLifecycleOwner) {
            productDetailRes = it
        }

        productViewModel.userInfoStatus.observe(viewLifecycleOwner) {
            binding.baseInfo.siEndText = when {
                it.isBaseInfoCompleted() -> {
                    binding.baseInfo.endTextView.setBackgroundResource(R.drawable.shape_daan_gray_solid_r20)
                    binding.baseInfo.endTextView.setTextColor(
                        ContextCompat.getColor(
                            binding.baseInfo.context,
                            R.color.sub_text
                        )
                    )
                    binding.baseInfo.isEnabled = false
                    getString(R.string.complete)
                }
                it.isBaseToFix() -> {
                    getString(R.string.to_fix)
                }
                else -> {
                    getString(R.string.to_fill_in)
                }
            }
            binding.otherInfo.siEndText = when {
                it.isOtherInfoComplete() -> {
                    binding.otherInfo.isEnabled = false
                    getString(R.string.complete)
                }
                it.isOtherToFix() -> {
                    binding.otherInfo.endTextView.setBackgroundResource(R.drawable.shape_red_solid_r20)
                    binding.otherInfo.endTextView.setTextColor(Color.WHITE)
                    getString(R.string.to_fix)
                }
                else -> {
                    getString(R.string.to_fill_in)
                }
            }
        }

        binding.baseInfo.setOnClickListener { clickInfoItem(it.id) }
        binding.otherInfo.setOnClickListener { clickInfoItem(it.id) }
        binding.loan.setOnClickListener { clickInfoItem(it.id) }
    }

    override fun onResume() {
        super.onResume()
        updateInfoEndViews()
    }

    private fun updateInfoEndViews() {
        if (UserFace.isLogin()) {
            productViewModel.fetchUserInfoStatus()
            productViewModel.fetchOrderProcessing(productId)
            productViewModel.getProductDetail(productId)
        } else {
            binding.baseInfo.siEndText = "Lengkapi Info"
            binding.otherInfo.siEndText = "Lengkapi Info"
        }
    }

    private fun clickInfoItem(id: Int) {
        if (UserFace.isLogin().not()) {
            CommUtils.navLogin()
            return
        }

        val res = productViewModel.userInfoStatus.value
            ?: productDetailRes?.infoStatus
            ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            if (amountTrialRes == null) {
                LoadingTips.showLoading()
                calAmountTrial()
                LoadingTips.dismissLoading()
            }
            clickAction(id, res)
        }
    }

    private fun clickAction(id: Int, it: UserInfoStatusRes) {
        when (id) {
            R.id.base_info -> {
                when {
                    it.basicInfo != 1
                            || it.ocrComplete != 1
                            || it.faceRecognition != 1 && it.faceRecognition != 2 -> {

                        WebActivity.loadUrl(context, H5_BASE_INFO.combineH5Url(getH5Params()))
                    }
                    it.isBaseToFix() -> {
                        livenessLauncher.launch(null)
                    }
                    it.faceRecognition == 3 -> {
                        ToastUtils.showLong(R.string.detection_failed_need_service)
                    }
                }
            }
            R.id.other_info -> {
                when {
                    it.emergencyInfo != 1 -> {
                        WebActivity.loadUrl(context, H5_CONTACT_PEOPLE.combineH5Url(getH5Params()))
                    }
                    it.bankInfo != 1 -> {
                        WebActivity.loadUrl(context, H5_BANK_INFO.combineH5Url(getH5Params()))
                    }
                    it.otherInfo != 1 -> {
                        WebActivity.loadUrl(context, H5_OTHER_INFO.combineH5Url(getH5Params()))
                    }
                }
            }
            R.id.loan -> {
                SurveyHelper.addOneSurvey("/apiProductDetail", "ApiClickApply")
                when {
                    binding.baseInfo.endTextView.text != getString(R.string.complete) -> {
                        binding.baseInfo.performClick()
                    }
                    binding.otherInfo.endTextView.text != getString(R.string.complete) -> {
                        binding.otherInfo.performClick()
                    }
                    else -> {
                        clickToLoan()
                    }
                }
            }
        }
    }

    private fun clickToLoan() {
        val process = productViewModel.orderProcessingRes.value
        if (process == null) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingTips.showLoading()
                val res = danaRequestWithCatch {
                    dfApi().orderProcessing(id)
                }
                LoadingTips.dismissLoading()
                navLoan(res)
            }
        } else {
            navLoan(process)
        }
    }

    private fun navLoan(orderProcessingRes: OrderProcessingRes?) {
        if (orderProcessingRes == null) {
            return
        }
        if (orderProcessingRes.status == 2) {
            findNavController().navigate(R.id.action_productDetailFragment_to_ordersActivity2)
        } else if (orderProcessingRes.status == 0) {
            WebActivity.loadUrl(context, H5_ORDER_CONFIRM.combineH5Url(getH5Params()))
        }
    }

    private suspend fun calAmountTrial() {
        amountTrialRes = danaRequestWithCatch {
            dfApi().amountTrial(
                productId,
                productDetailRes?.termUnit ?: 0,
                productDetailRes?.amountMax?.intValueExact() ?: 0,
                productDetailRes?.termUnit ?: 0
            )
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        CommUtils.stepAfterLiveness(lifecycleScope, context, it)
    }

    private fun getH5Params() = mutableMapOf(
        "id" to productId, // 点击产品ID
        "amount" to amountTrialRes?.getMaxAmount(), // 产品试算金额列表返回data数组里的最大值
        "productName" to productDetailRes?.name, // 点击的产品名称
        "trackCode" to "al", // 入口埋点（取埋点里code字段）
    )
}