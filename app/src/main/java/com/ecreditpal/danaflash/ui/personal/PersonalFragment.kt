package com.ecreditpal.danaflash.ui.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding
import com.ecreditpal.danaflash.ui.home.HomeViewModel

class PersonalFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback {
            activity?.finish()
        }

        val homeViewModel: HomeViewModel by activityViewModels()
        homeViewModel.getAd(AD_TITLE_PERSONALPOP)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val personalViewModel: PersonalViewModel by viewModels()

        val binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.pf = this
        binding.vm = personalViewModel
        return binding.root
    }

    fun nav(status: Int) {
        findNavController().navigate(
            PersonalFragmentDirections.actionNavigationPersonalToOrdersActivity(status)
        )
    }
}