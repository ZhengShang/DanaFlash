package com.ecreditpal.danaflash.ui.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding

class PersonalFragment : BaseFragment() {

    private lateinit var dashboardViewModel: PersonalViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(PersonalViewModel::class.java)

        val _binding = FragmentPersonalBinding.inflate(inflater, container, false)

        return _binding.root
    }


}