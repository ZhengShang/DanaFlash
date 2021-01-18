package com.ecreditpal.danaflash.ui.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.data.IMAGE_PREFIX
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.setImageUrl
import com.ecreditpal.danaflash.model.AdRes
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator


class AdDialog : BaseDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_ad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adRes: AdRes = arguments?.let { AdDialogArgs.fromBundle(it).adRes } ?: return

        val dataMap = adRes.imgs
            ?.filterNotNull()
            ?.filter { it.img.isNullOrEmpty().not() }
            ?.associateBy({ IMAGE_PREFIX.plus(it.img) }, { it.url.combineH5Url() })
            ?: return

        view.findViewById<Banner<String, BannerAdapter>>(R.id.banner).apply {
            addBannerLifecycleObserver(this@AdDialog)
            adapter = BannerAdapter(dataMap) {
                WebActivity.loadUrl(context, it)
                dismissAllowingStateLoss()
            }
            setIndicator(CircleIndicator(context))
        }

//        view.findViewById<TextView>(R.id.content).apply {
//            movementMethod = ScrollingMovementMethod.getInstance()
//            text = "Here show the tips of permissions required"
//            setOnClickListener {
//                ToastUtils.showLong("navigate to h5 page")
//            }
//        }
        view.findViewById<ImageView>(R.id.close).setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(null)
            setLayout(
                (ScreenUtils.getScreenWidth() * 0.77f).toInt(),
                (ScreenUtils.getScreenHeight() * 0.56f).toInt()
            )
        }
    }

    private class BannerAdapter(
        val dataMap: Map<String, String>,
        val click: (url: String) -> Unit
    ) : BannerImageAdapter<String>(dataMap.keys.toList()) {

        override fun onBindView(
            holder: BannerImageHolder?,
            data: String?,
            position: Int,
            size: Int
        ) {
            holder?.imageView?.let {
                setImageUrl(it, data)
                it.setOnClickListener { v ->
                    click.invoke(dataMap.getValue(data as String))
                }
            }
        }
    }
}