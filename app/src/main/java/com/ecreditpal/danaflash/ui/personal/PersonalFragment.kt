package com.ecreditpal.danaflash.ui.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding

class PersonalFragment : BaseFragment() {

    private lateinit var personalViewModel: PersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback {
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        personalViewModel =
            ViewModelProvider(this).get(PersonalViewModel::class.java)

        val binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.vm = personalViewModel
        return binding.root
    }


}