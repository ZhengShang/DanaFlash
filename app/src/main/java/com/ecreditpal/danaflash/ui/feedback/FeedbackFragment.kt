package com.ecreditpal.danaflash.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentFeedbackBinding
import com.ecreditpal.danaflash.helper.danaRequestResult
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.launch

class FeedbackFragment : BaseFragment() {

    private lateinit var _binding: FragmentFeedbackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = this
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!UserFace.isLogin()) {
            findNavController().navigate(R.id.action_feedbackFragment_to_loginActivity)
        }

        _binding.submit.setOnClickListener {
            lifecycleScope.launch {
                LoadingTips.showLoading()
                val success = danaRequestResult {
                    dfApi().feedback(_binding.input.text.toString())
                }
                LoadingTips.dismissLoading()
                if (success) {
                    ToastUtils.showLong(R.string.feedback_success)
                    _binding.input.text = null
                } else {
                    ToastUtils.showLong(R.string.feedback_failed)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!UserFace.isLogin()) {
            activity?.onBackPressed()
        }
    }
}