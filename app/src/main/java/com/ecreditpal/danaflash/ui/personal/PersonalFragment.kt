package com.ecreditpal.danaflash.ui.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.AD_TITLE_PERSONALPOP
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentPersonalBinding
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.ui.home.HomeViewModel
import com.ecreditpal.danaflash.ui.home.MainFragmentDirections

class PersonalFragment : BaseFragment() {

    private val personalViewModel: PersonalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding.text1.setOnClickListener { CommUtils.navLogin() }
        binding.text2.setOnClickListener { CommUtils.navLogin() }

        binding.faq.setOnClickListener {
            SurveyHelper.addOneSurvey("/my", "clickTips")
            findNavController().navigate(R.id.action_mainFragment_to_faqActivity)
        }
        binding.feedback.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_feedbackActivity)
        }
        binding.settings.setOnClickListener {
            SurveyHelper.addOneSurvey("/my", "clickSetting")
            findNavController().navigate(R.id.action_mainFragment_to_settingsActivity)
        }

        return binding.root
    }

    fun nav(status: Int) {
        SurveyHelper.addOneSurvey("/my", "myOrder")
        SurveyHelper.addOneSurvey("/my", "myOrder", "AR")
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

    override fun onResume() {
        super.onResume()
        SurveyHelper.addOneSurvey("/my", "in")
    }
}