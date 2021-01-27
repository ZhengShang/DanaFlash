package com.ecreditpal.danaflash.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.H5_BASE_INFO
import com.ecreditpal.danaflash.data.H5_ORDER_CONFIRM
import com.ecreditpal.danaflash.data.H5_OTHER_INFO
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentProductDetailBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.ui.comm.WebActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentProductDetailBinding
    private val productViewModel: ProductViewModel by viewModels()

    private var product: ProductRes.Product? = null

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

        product = activity?.intent?.extras?.let { ProductActivityArgs.fromBundle(it).product }
        if (product == null) {
            activity?.finish()
            return
        }

        binding.product = product
        binding.vm = productViewModel

        productViewModel.userInfoStatus.observe(viewLifecycleOwner) {
            binding.baseInfo.siEndText = if (it.isBaseInfoVerified()) {
                getString(R.string.verified)
            } else {
                getString(R.string.go_verify)
            }
            binding.otherInfo.siEndText = if (it.isOtherInfoVerified()) {
                getString(R.string.verified)
            } else {
                getString(R.string.go_verify)
            }
        }

        binding.baseInfo.setOnClickListener { clickInfoItem(it.id) }
        binding.otherInfo.setOnClickListener { clickInfoItem(it.id) }

        binding.loan.setOnClickListener {
            productViewModel.orderProcessingRes.value?.let {
                if (it.status == 2) {
                    findNavController().navigate(R.id.action_productDetailFragment_to_ordersActivity2)
                } else if (it.status == 0) {
                    navH5OrderConfirm()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateInfoEndViews()
    }

    private fun updateInfoEndViews() {
        if (UserFace.isLogin()) {
            productViewModel.fetchUserInfoStatus()
            productViewModel.fetchOrderProcessing(product?.id!!)
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
        productViewModel.userInfoStatus.value?.let {
            when (id) {
                R.id.base_info -> {
                    if (it.isBaseInfoVerified().not()) {
                        toInfoDetail(H5_BASE_INFO)
                    }
                }
                R.id.other_info -> {
                    if (it.isOtherInfoVerified().not()) {
                        toInfoDetail(H5_OTHER_INFO)
                    }
                }
            }
        }
    }

    private fun toInfoDetail(url: String) {
        WebActivity.loadUrl(
            context, url.combineH5Url(
                mapOf(
                    "id" to product?.id,
                    "amount" to product?.amountMax,
                    "productName" to product?.name,
                    "trackCode" to "" // TODO: 2021/1/18 add trackCode
                )
            )
        )
    }

    private fun navH5OrderConfirm() {
        val id = product?.id ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().amountTrial(
                    id,
                    product?.periodUnit,
                    product?.amountMax?.intValueExact(),
                    product?.periodMax
                )
            }
            LoadingTips.dismissLoading()
            if (res == null) {
                return@launch
            }
            WebActivity.loadUrl(
                context, H5_ORDER_CONFIRM.combineH5Url(
                    mapOf(
                        "id" to id, // 点击产品ID
                        "amount" to res.getMaxAmount(), // 产品试算金额列表返回data数组里的最大值
                        "productName" to product?.name, // 点击的产品名称
                        "trackCode" to "", // 入口埋点（取埋点里code字段）
                        // TODO: 2021/1/21 track code
                    )
                )
            )
        }
    }
}