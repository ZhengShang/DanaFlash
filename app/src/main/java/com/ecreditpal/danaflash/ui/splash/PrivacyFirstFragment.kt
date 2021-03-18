package com.ecreditpal.danaflash.ui.splash

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.data.DataStoreKeys
import com.ecreditpal.danaflash.helper.writeDsData
import kotlinx.coroutines.launch

class PrivacyFirstFragment : BaseDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_privacy_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val privacyHtml = view.context.applicationContext.assets
            .open("privacy_policies.html").bufferedReader()
            .use {
                it.readText()
            }
        view.findViewById<TextView>(R.id.content).apply {
            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(privacyHtml, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(privacyHtml)
            }
        }
        view.findViewById<TextView>(R.id.agree).setOnClickListener {
            findNavController().navigate(R.id.action_privacyPoliciesFragment_to_mainActivity)
            writeAcceptValue()
            activity?.finish()
        }
        view.findViewById<TextView>(R.id.disagree).setOnClickListener {
            activity?.finish()
        }
    }

    private fun writeAcceptValue() {
        viewLifecycleOwner.lifecycleScope.launch {
            context.writeDsData(DataStoreKeys.IS_ACCEPT_PRIVACY, true)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)
            setBackgroundDrawableResource(R.drawable.shape_white_solid__r16)
            setLayout(
                ScreenUtils.getScreenWidth() - 90,
                (ScreenUtils.getScreenHeight() * 3 / 4f).toInt()
            )
            setGravity(Gravity.BOTTOM)
            attributes = attributes.apply {
                y = ConvertUtils.dp2px(25f)
            }
        }
    }
}