package com.ecreditpal.danaflash.ui.login

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
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
        }
        bind.viewMoel = loginVM

        if (BuildConfig.DEBUG) {
            bind.phoneNumber.setText("888899991234")
        }
    }
}