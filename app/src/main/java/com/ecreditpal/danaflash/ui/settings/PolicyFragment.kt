package com.ecreditpal.danaflash.ui.settings

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.BarUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.BaseNavActivity

class PolicyFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { PolicyFragmentArgs.fromBundle(it) } ?: return
        if (activity is BaseNavActivity) {
            (activity as BaseNavActivity).findViewById<TextView>(R.id.title).text = args.label
        }

        val title = view.findViewById<TextView>(R.id.title)
        view.findViewById<View>(R.id.title_bar).visibility =
            if (args.hideTitle) View.GONE else {
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(view.context, R.color.dana_red)
                BarUtils.setStatusBarLightMode(requireActivity(), false)
                title.text = args.label
                View.VISIBLE
            }

        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }

        val textView = view.findViewById<TextView>(R.id.content)
        textView.movementMethod = ScrollingMovementMethod.getInstance()
        val policyHtml = view.context.applicationContext.assets
            .open(args.policyFileName).bufferedReader()
            .use {
                it.readText()
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text =
                Html.fromHtml(policyHtml, Html.FROM_HTML_MODE_COMPACT)
        } else {
            textView.text = Html.fromHtml(policyHtml)
        }
    }
}