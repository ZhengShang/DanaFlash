package com.ecreditpal.danaflash.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.DataStoreKeys
import com.ecreditpal.danaflash.helper.readDsData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return Space(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            //Delay 1s avoid to enter Main page quickly
            delay(1000)
            val accept = context.readDsData(DataStoreKeys.IS_ACCEPT_PRIVACY, false)
            if (accept) {
                findNavController().navigate(R.id.action_splashFragment_to_mainActivity)
                activity?.finish()
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_privacyPoliciesFragment)
            }
        }
    }
}