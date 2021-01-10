package com.ecreditpal.danaflash.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.databinding.FragmentFeedbackBinding

class FeedbackFragment : BaseFragment() {

    private lateinit var _binding: FragmentFeedbackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!UserFace.isLogin()) {
            findNavController().navigate(R.id.action_feedbackFragment_to_loginActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!UserFace.isLogin()) {
            activity?.onBackPressed()
        }
    }
}