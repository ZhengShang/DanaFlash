package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.MainActivity
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.databinding.FragmentHomeBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.ui.comm.CommLoadStateAdapter
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.android.synthetic.main.view_home_banner.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val type = ObservableInt(PRODUCT_TYPE_API)
    private val tabVisible = ObservableBoolean(false)
    private lateinit var binding: FragmentHomeBinding

    private lateinit var apiAdapter: ProductAdapter
    private lateinit var gpAdapter: ProductAdapter

    //过滤掉第一次的刷新页面操作, 只有从其他页面返回回来才需要刷新
    private var fromCreate = true

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
        binding.tabVisible = tabVisible

        initList(binding.apiList, PRODUCT_TYPE_API)
        initList(binding.gpList, PRODUCT_TYPE_GP)

        //重新设置一边, 覆盖statusView里面的设置. 因为这里需要区分不同的产品来刷新
        binding.swipeRefresh.setOnRefreshListener {
            refreshList()
        }

        binding.bannerLayout.root.setOnClickListener {
            if (UserFace.isLogin()) {
                val mainActivity = activity as? MainActivity ?: return@setOnClickListener
                if (mainActivity.isAllPermissionGranted()) {
                    // TODO: 2021/1/24 check user info edit status
                } else {
                    mainActivity.requestAllPermissions()
                }
            } else {
                CommUtils.navLogin()
            }
        }
        binding.bannerLayout.tabApi.setOnClickListener {
            type.set(PRODUCT_TYPE_API)
            homeViewModel.getAd(AD_TITLE_APIPOP)
        }
        binding.bannerLayout.tabGp.setOnClickListener {
            type.set(PRODUCT_TYPE_GP)
            homeViewModel.getAd(AD_TITLE_POP)
        }

        homeViewModel.productSupportIndex.observe(viewLifecycleOwner) {
            tabVisible.set(it == 2)
            if (it != 2) {
                type.set(if (it == 0) PRODUCT_TYPE_API else PRODUCT_TYPE_GP)
                //第一次主动请求广告
                homeViewModel.getAd(if (it == 0) AD_TITLE_APIPOP else AD_TITLE_POP)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (fromCreate.not()) {
            refreshList()
        }
        fromCreate = false
    }

    private fun refreshList() {
        if (type.get() == PRODUCT_TYPE_API) {
            apiAdapter.refresh()
        } else {
            gpAdapter.refresh()
        }
    }

    private fun initList(view: View, productType: Int) {

        val pageAdapter = ProductAdapter(productType).apply {
            withLoadStateFooter(CommLoadStateAdapter(this::retry))
            productClick = { viewId, product -> clickProduct(productType, viewId, product) }
        }.also {
            if (productType == PRODUCT_TYPE_API) {
                apiAdapter = it
            } else {
                gpAdapter = it
            }
        }

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = pageAdapter
            isNestedScrollingEnabled = true
        }

        view.findViewById<StatusView>(R.id.status_view)
            .bindAdapter(pageAdapter, binding.swipeRefresh)

        lifecycleScope.launch(Dispatchers.IO) {
            val flow: Flow<PagingData<ProductRes.Product>> =
                if (productType == PRODUCT_TYPE_API) {
                    homeViewModel.apiFlow
                } else {
                    homeViewModel.gpFlow
                }
            flow.collectLatest {
                pageAdapter.submitData(it)
            }
        }
    }

    private fun clickProduct(productType: Int, clickId: Int, product: ProductRes.Product?) {
        if (product == null) {
            return
        }
        if (productType == PRODUCT_TYPE_API) {
            if (clickId == R.id.loan || clickId == R.id.root) {
                findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToProductActivity(product)
                )
            }
        } else {
            if (clickId == R.id.loan) {
                CommUtils.navGoogleDownload(context, product.link)
            }
        }
    }
}