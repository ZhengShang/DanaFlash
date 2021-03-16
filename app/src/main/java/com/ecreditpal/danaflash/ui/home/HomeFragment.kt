package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.*
import com.ecreditpal.danaflash.databinding.FragmentHomeBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.danaRequestWithCatch
import com.ecreditpal.danaflash.model.ProductRes
import com.ecreditpal.danaflash.net.dfApi
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.comm.CommLoadStateAdapter
import com.ecreditpal.danaflash.ui.comm.WebActivity
import com.ecreditpal.danaflash.widget.StatusView
import com.ecreditpal.danaflash.worker.InvokeFilterProductWorker
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
            SurveyHelper.addOneSurvey("/", "clickBannerApply", "AS")
            //不检测登陆,权限和用户信息等, 直接进入H5页面
            WebActivity.loadUrl(context, H5_ONE_CLICK_APPLY.combineH5Url())
        }
        binding.bannerLayout.tabApi.setOnClickListener {
            SurveyHelper.addOneSurvey("/", "apiTabClick")
            type.set(PRODUCT_TYPE_API)
            refreshList()
        }
        binding.bannerLayout.tabGp.setOnClickListener {
            SurveyHelper.addOneSurvey("/", "tabClick")
            type.set(PRODUCT_TYPE_GP)
            refreshList()
        }

        homeViewModel.productSupportIndex.observe(viewLifecycleOwner) {
            tabVisible.set(it == 2)
            if (it != 2) {
                type.set(if (it == 0) PRODUCT_TYPE_API else PRODUCT_TYPE_GP)
            }
            if (it != 1) {
                SurveyHelper.addOneSurvey("/", "api_show")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (fromCreate.not()) {
            //默认显示api的tab
            if (tabVisible.get()) {
                //切换tab自然会刷新列表
                binding.bannerLayout.tabApi.performClick()
            } else {
                //此时tab不显示, 需要主动调用刷新
                refreshList()
            }
        }
        fromCreate = false

        //尝试发送product_filter
        context?.let {
            WorkManager
                .getInstance(it)
                .enqueue(
                    OneTimeWorkRequest.Builder(InvokeFilterProductWorker::class.java).build()
                )
        }
        homeViewModel.tryShowPopDialog()
    }

    private fun refreshList() {
        apiAdapter.refresh()
        gpAdapter.refresh()
        //每次刷新都获取当前tab信息
        homeViewModel.detectProductSupported()
    }

    private fun initList(view: View, productType: Int) {

        val pageAdapter = ProductAdapter(productType).apply {
            withLoadStateFooter(CommLoadStateAdapter(this::retry))
            productClick = { viewId, position, product ->
                clickProduct(
                    productType,
                    viewId,
                    position,
                    product
                )
            }
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

    private fun clickProduct(
        productType: Int,
        clickId: Int,
        position: Int,
        product: ProductRes.Product?
    ) {
        if (product == null) {
            return
        }
        if (productType == PRODUCT_TYPE_API) {
            if (clickId == R.id.loan || clickId == R.id.root) {
                SurveyHelper.addOneSurvey("/", "ApiClickProduct", "AD$position")
                //使用H5版本的详情页
                WebActivity.loadUrl(
                    context, H5_PRODUCT_DETAIL.combineH5Url(
                        mapOf(
                            "id" to product.id, //产品id
                            "productName" to product.name, //产品名
                            "positionIndex" to position //点击列表时产品所在位置（如没有则传空）
                        )
                    )
                )
            }
        } else {
            if (clickId == R.id.loan || clickId == R.id.root) {
                SurveyHelper.addOneSurvey("/", "clickProduct")
                WebActivity.loadUrl(context, product.link, forDownload = true)
            }
        }
    }

    private fun navByUserInfoStatus() {
        lifecycleScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = danaRequestWithCatch {
                dfApi().getUserInfoStatus()
            }
            LoadingTips.dismissLoading()

            if (res == null) {
                return@launch
            }

            /*
            - 基本信息未完成/需要修正时，点击banner进入h5的基本信息填写页（ocr已完成的进入基本信息页，未完成的进入ocr拍照页）
            - 活体检测未完成/相似度未通过/反欺诈分数未通过时，点击banner进入h5的基本信息填写页
            - 活体检测过期时，点击banner拉起活体检测
            - 活体检测达到最大重试次数时，点击banner后弹出toast提示"检测异常，请联系客服处理“
            （印尼语：Tes Gagal, Mohon untuk Hubungi CS）
            - 联系信息未完成/需要修正时，点击banner进入h5的联系人信息填写页
            - 银行卡信息未完成/需要修正时，点击banner进入h5的银行卡信息填写页
            - 其他信息未完成/需要修正时，点击banner进入h5的其他信息填写页
            - 信息全部完成时，点击banner进入h5的一键申请页面
             */

            when {
                res.basicInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_BASE_INFO.combineH5Url())
                }
                res.faceRecognition == 2 -> {
                    livenessLauncher.launch(null)
                }
                res.faceRecognition == 3 -> {
                    ToastUtils.showLong(R.string.detection_failed_need_service)
                }
                res.emergencyInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_CONTACT_PEOPLE.combineH5Url())
                }
                res.bankInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_BANK_INFO.combineH5Url())
                }
                res.otherInfo != 1 -> {
                    WebActivity.loadUrl(context, H5_OTHER_INFO.combineH5Url())
                }
                else -> {
                    WebActivity.loadUrl(context, H5_ONE_CLICK_APPLY.combineH5Url())
                }
            }
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        CommUtils.stepAfterLiveness(lifecycleScope, context, it)
    }
}