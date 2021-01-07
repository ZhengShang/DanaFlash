package com.ecreditpal.danaflash.ui.settings

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.blankj.utilcode.util.ConvertUtils
import com.ecreditpal.danaflash.base.BaseFragment

class PrivacyFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(context).apply {
            movementMethod = ScrollingMovementMethod.getInstance()
            setPadding(
                ConvertUtils.dp2px(14f),
                ConvertUtils.dp2px(18f),
                ConvertUtils.dp2px(14f),
                ConvertUtils.dp2px(25f)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView = view as? TextView ?: return
        val privacyHtml = view.context.applicationContext.assets
            .open("privacy_policies.html").bufferedReader()
            .use {
                it.readText()
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text =
                Html.fromHtml(privacyHtml, Html.FROM_HTML_MODE_COMPACT);
        } else {
            textView.text = Html.fromHtml(privacyHtml);
        }
    }
}