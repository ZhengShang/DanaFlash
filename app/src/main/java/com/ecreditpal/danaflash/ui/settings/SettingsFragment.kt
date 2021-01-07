package com.ecreditpal.danaflash.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentSettingsBinding.inflate(inflater, container, false).root
    }

}