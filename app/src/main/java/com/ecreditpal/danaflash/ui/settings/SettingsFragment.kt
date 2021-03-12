package com.ecreditpal.danaflash.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentSettingsBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val loginStateObserve = ObservableBoolean(UserFace.isLogin())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val versionViewModel = ViewModelProvider(this).get(VersionViewModel::class.java)
        binding.versionVm = versionViewModel
        binding.loginState = loginStateObserve
        versionViewModel.checkVersion(false)

        binding.privacy.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToPolicyFragment(
                    "privacy_policies.html",
                    getString(R.string.privacy_policy)
                )
            )
        }
        binding.registerProtocol.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToPolicyFragment(
                    "register_policies.html",
                    getString(R.string.register_protocol)
                )
            )
        }
        binding.signOut.setOnClickListener {
            if (UserFace.isLogin()) {
                SurveyHelper.addOneSurvey("/setting", "clickLogout")
                ConfirmDialog(
                    titleStr = "Notifikasi",
                    contentStr = "Yakin Ingin Keluar？",
                    positiveClickListener = {
                        activity?.onBackPressed()
                        UserFace.clearData()
                    }
                )
                    .show(childFragmentManager)
            } else {
                SurveyHelper.addOneSurvey("/setting", "clickLogin")
                CommUtils.navLogin()
            }
        }
        binding.version.setOnClickListener { v ->
            if (binding.version.siEndText.isNullOrEmpty()) {
                ToastUtils.showShort(R.string.your_app_is_newest)
                versionViewModel.checkVersion(false)
                return@setOnClickListener
            }
            if (versionViewModel.versionRes.value?.updateStatus == 0) {
                ToastUtils.showShort(R.string.your_app_is_newest)
                return@setOnClickListener
            }
            v.isEnabled = false
            versionViewModel.versionRes.value?.let {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToUpdateDialog(it)
                )
            }
            v.postDelayed({ v.isEnabled = true }, 500)
        }
    }

    override fun onStart() {
        super.onStart()
        loginStateObserve.set(UserFace.isLogin())
    }
}