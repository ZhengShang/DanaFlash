package com.ecreditpal.danaflash.ui.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.data.AD_TITLE_APIPOP
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.data.AD_TITLE_POP
import com.ecreditpal.danaflash.data.IMAGE_PREFIX
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.helper.combineH5Url
import com.ecreditpal.danaflash.helper.setImageUrl
import com.ecreditpal.danaflash.model.AdRes
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator


class AdDialog : BaseDialogFragment() {

    private var adTitle = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_ad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adRes: AdRes? = null

        arguments?.let {
            adTitle = AdDialogArgs.fromBundle(it).adTitle
            adRes = AdDialogArgs.fromBundle(it).adRes
        }

        val dataMap = adRes?.imgs
            ?.filterNotNull()
            ?.filter { it.img.isNullOrEmpty().not() }
            ?.associateBy({ IMAGE_PREFIX.plus(it.img) }, { it.url.combineH5Url() })
            ?: return

        if (dataMap.isNullOrEmpty()) {
            findNavController().popBackStack()
            return
        }

        val act = when (adTitle) {
            AD_TITLE_APIPOP -> {
                "apiPop"
            }
            AD_TITLE_POP -> {
                "pop"
            }
            else -> {
                ""
            }
        }
        if (act.isNotEmpty()) {
            SurveyHelper.addOneSurvey("/", act)
        }


        view.findViewById<Banner<String, BannerAdapter>>(R.id.banner).apply {
            addBannerLifecycleObserver(this@AdDialog)
            adapter = BannerAdapter(dataMap) {
                var code = ""
                var p = "/"
                val a = when (adTitle) {
                    AD_TITLE_APIPOP -> {
                        code = "AA"
                        "clickApiPop"
                    }
                    AD_TITLE_POP -> {
                        code = "AB"
                        "clickPop"
                    }
                    AD_TITLE_PERSONALPOP -> {
                        p = "/my"
                        code = "AM"
                        "clickPersonalPop"
                    }
                    else -> ""
                }
                SurveyHelper.addOneSurvey(p, a, code)
                WebActivity.loadUrl(context, it)
                findNavController().popBackStack()
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
        view.findViewById<ImageView>(R.id.close)
            .setOnClickListener { findNavController().popBackStack() }
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

    override fun onStop() {
        super.onStop()
        var code = ""
        var p = "/"
        val act = when (adTitle) {
            AD_TITLE_APIPOP -> {
                code = "AA"
                "closeApiPop"
            }
            AD_TITLE_POP -> {
                code = "AB"
                "closePop"
            }
            AD_TITLE_PERSONALPOP -> {
                p = "/"
                code = "AM"
                "closePersonalPop"
            }
            else -> ""
        }
        SurveyHelper.addOneSurvey(p, act, code)
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
                it.scaleType = ImageView.ScaleType.FIT_CENTER
                setImageUrl(it, data)
                it.setOnClickListener {
                    click.invoke(dataMap.getValue(data as String))
                }
            }
        }
    }
}