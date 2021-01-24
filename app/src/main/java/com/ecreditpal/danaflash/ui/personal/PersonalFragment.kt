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
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections

class PersonalFragment : BaseFragment() {

    private val personalViewModel: PersonalViewModel by viewModels()

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
        val binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.pf = this
        binding.vm = personalViewModel

        binding.avatar.setOnClickListener {
            if (UserFace.isLogin().not()) {
                CommUtils.navLogin()
            }
        }

        return binding.root
    }

    fun nav(status: Int) {
        if (UserFace.isLogin().not()) {
            CommUtils.navLogin()
            return
        }
        kotlin.runCatching {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToOrdersActivity(status)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        personalViewModel.updatePhone()
    }
}