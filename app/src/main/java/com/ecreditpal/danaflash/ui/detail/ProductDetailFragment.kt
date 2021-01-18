package com.ecreditpal.danaflash.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.H5_BASE_INFO
import com.ecreditpal.danaflash.data.H5_OTHER_INFO
import com.ecreditpal.danaflash.databinding.FragmentProductDetailBinding
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.ui.comm.WebActivity

class ProductDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentProductDetailBinding

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

        val product: ProductRes.Product =
            activity?.intent?.extras?.let { ProductActivityArgs.fromBundle(it).product }
                ?: return
        binding.product = product
        binding.baseInfo.setOnClickListener {
            toInfoDetail(H5_BASE_INFO, product)
        }
        binding.otherInfo.setOnClickListener {
            toInfoDetail(H5_OTHER_INFO, product)
        }
    }

    private fun toInfoDetail(url: String, product: ProductRes.Product) {
        WebActivity.loadUrl(
            context, url.combineH5Url(
                mapOf(
                    "id" to product.id,
                    "amount" to product.amountMax,
                    "productName" to product.name,
                    "trackCode" to "" // TODO: 2021/1/18 add trackCode
                )
            )
        )
    }
}