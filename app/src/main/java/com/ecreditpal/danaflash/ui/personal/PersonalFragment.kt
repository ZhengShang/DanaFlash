package com.ecreditpal.danaflash.ui.personal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.BuildConfig
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding
import com.ecreditpal.danaflash.ui.camera.CameraActivity
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections

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

        // FIXME: 2021/1/20 test only
        if (BuildConfig.DEBUG) {
            binding.avatar.setOnClickListener {
                startActivity(Intent(context, CameraActivity::class.java))
            }
        }

        return binding.root
    }

    fun nav(status: Int) {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToOrdersActivity(status)
        )
    }
}