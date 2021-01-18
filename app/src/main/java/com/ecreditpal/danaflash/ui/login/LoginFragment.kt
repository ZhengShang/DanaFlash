package com.ecreditpal.danaflash.ui.login

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentLoginBinding


class LoginFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.statusBarColor = Color.WHITE
        BarUtils.setStatusBarLightMode(requireActivity(), true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginVM: LoginViewModel by viewModels()
        loginVM.loginResult.observe(viewLifecycleOwner, { login ->
            if (login) {
                activity?.finish()
            }
        })

        val bind = FragmentLoginBinding.bind(view).apply {
            lifecycleOwner = this@LoginFragment
            back.setOnClickListener { activity?.onBackPressed() }
            clearPhoneNumber.setOnClickListener { phoneNumber.text = null }
            clearVerifyCode.setOnClickListener { verifyCode.text = null }
            coldDown.paintFlags = coldDown.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            phoneNumber.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        KeyboardUtils.hideSoftInput(v)
                        true
                    }
                    else -> false
                }
            }
            verifyCode.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_GO -> {
                        KeyboardUtils.hideSoftInput(v)
                        if (login.isEnabled) {
                            login.performClick()
                        }
                        true
                    }
                    else -> false
                }
            }

            val declare = getString(R.string.login_protocol_declare)
            val protocol = getString(R.string.register_protocol)
            val index = declare.indexOf(protocol)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToPolicyFragment2(
                            policyFileName = "register_policies.html",
                            label = getString(R.string.register_protocol),
                            hideTitle = false
                        )
                    )
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(view.context, R.color.dana_red)
                }
            }

            privacyTips.movementMethod = LinkMovementMethod.getInstance()
            privacyTips.text = declare.toSpannable().apply {
                if (index != -1) {
                    setSpan(
                        clickableSpan, index,
                        index + protocol.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        bind.viewMoel = loginVM

        if (BuildConfig.DEBUG) {
            bind.phoneNumber.setText("888899991234")
        }
    }
}