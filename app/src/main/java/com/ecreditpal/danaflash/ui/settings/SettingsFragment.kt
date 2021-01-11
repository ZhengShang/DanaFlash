package com.ecreditpal.danaflash.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding

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
        versionViewModel.checkVersion()

        binding.signOut.setOnClickListener {
            activity?.onBackPressed()
            UserFace.clearData()
        }
        binding.version.setOnClickListener { v ->
            if (binding.version.siEndText.isNullOrEmpty()) {
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
}