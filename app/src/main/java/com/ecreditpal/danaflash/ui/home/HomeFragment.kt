package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.AD_TITLE_INDEX
import com.ecreditpal.danaflash.data.AD_TITLE_POP
import com.ecreditpal.danaflash.data.IMAGE_PREFIX
import com.ecreditpal.danaflash.databinding.FragmentHomeBinding
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.setImageUrl
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.ui.comm.CommLoadStateAdapter
import com.ecreditpal.danaflash.ui.comm.WebActivity
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.android.synthetic.main.view_home_banner.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val type = ObservableInt(ProductAdapter.PRODUCT_TYPE_API)
    private lateinit var binding: FragmentHomeBinding

    private lateinit var apiAdapter: ProductAdapter
    private lateinit var gpAdapter: ProductAdapter

    private var bannerClickUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        binding.lifecycleOwner = this
        binding.type = type

        binding.swipeRefresh.setOnRefreshListener {
            if (type.get() == ProductAdapter.PRODUCT_TYPE_API) {
                apiAdapter.refresh()
            } else {
                gpAdapter.refresh()
            }
        }

        initList(binding.apiList, ProductAdapter.PRODUCT_TYPE_API)
        initList(binding.gpList, ProductAdapter.PRODUCT_TYPE_GP)

        binding.bannerLayout.root.setOnClickListener {
            WebActivity.loadUrl(context, bannerClickUrl)
        }
        binding.bannerLayout.tabApi.setOnClickListener {
            type.set(ProductAdapter.PRODUCT_TYPE_API)
            homeViewModel.getAd(AD_TITLE_POP)
        }
        binding.bannerLayout.tabGp.setOnClickListener {
            type.set(ProductAdapter.PRODUCT_TYPE_GP)
            homeViewModel.getAd(AD_TITLE_INDEX)
        }

        homeViewModel.adLiveData.observe(viewLifecycleOwner) {
            val url = it.second.imgs?.firstOrNull()?.img ?: return@observe
            val clickUrl = it.second.imgs?.firstOrNull()?.url
            when (it.first) {
                AD_TITLE_POP,
                AD_TITLE_INDEX -> {
                    setImageUrl(binding.bannerLayout.bg, IMAGE_PREFIX + url)
                    bannerClickUrl = clickUrl.combineH5Url()
                }
            }
        }
        homeViewModel.getAd(AD_TITLE_POP)
    }

    private fun initList(view: View, productType: Int) {

        val pageAdapter = ProductAdapter(productType).apply {
            withLoadStateFooter(CommLoadStateAdapter(this::retry))
            productClick = { viewId, product ->
                if (viewId == R.id.loan && product != null) {
                    findNavController().navigate(
                        MainFragmentDirections.actionMainFragmentToProductActivity(product)
                    )
                }
            }
        }.also {
            if (productType == ProductAdapter.PRODUCT_TYPE_API) {
                apiAdapter = it
            } else {
                gpAdapter = it
            }
        }

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = pageAdapter
            isNestedScrollingEnabled = true
        }

        view.findViewById<StatusView>(R.id.status_view).bindAdapter(pageAdapter)

        lifecycleScope.launch(Dispatchers.IO) {
            val flow: Flow<PagingData<ProductRes.Product>> =
                if (productType == ProductAdapter.PRODUCT_TYPE_API) {
                    homeViewModel.apiFlow
                } else {
                    homeViewModel.gpFlow
                }
            flow.collectLatest {
                pageAdapter.submitData(it)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}