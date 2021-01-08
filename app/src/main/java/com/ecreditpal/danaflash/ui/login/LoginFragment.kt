package com.ecreditpal.danaflash.ui.login

import DataStoreKeys
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentLoginBinding
import com.ecreditpal.danaflash.helper.writeDsData
import kotlinx.coroutines.launch


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
        val bind = FragmentLoginBinding.bind(view).apply {
            lifecycleOwner = this@LoginFragment
            back.setOnClickListener { activity?.onBackPressed() }
            clearPhoneNumber.setOnClickListener { phoneNumber.text = null }
            clearVerifyCode.setOnClickListener { verifyCode.text = null }
            coldDown.paintFlags = coldDown.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        if (BuildConfig.DEBUG) {
            bind.phoneNumber.setText("888899991234")
        }

        val loginVM = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginVM.loginResult.observe(viewLifecycleOwner, { loginRes ->
            if (loginRes != null) {
                lifecycleScope.launch {
                    context.writeDsData(DataStoreKeys.TOKEN, loginRes.token ?: "")
                }
                findNavController().popBackStack()
            }
        })
        bind.viewMoel = loginVM

    }
}