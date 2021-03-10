package com.ecreditpal.danaflash.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.databinding.DialogAmountDropBinding
import com.ecreditpal.danaflash.helper.SurveyHelper

class AmountDropDialog : BaseDialogFragment() {

    private lateinit var binding: DialogAmountDropBinding
    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAmountDropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val orderRes = arguments?.let { AmountDropDialogArgs.fromBundle(it).orderRes }
        val trialRes = arguments?.let { AmountDropDialogArgs.fromBundle(it).trialRes }

        if (orderRes == null || trialRes == null) {
            findNavController().popBackStack()
            return
        }
        binding.orderInfo = orderRes
        binding.trialInfo = trialRes
        binding.lifecycleOwner = viewLifecycleOwner

        binding.cancel.setOnClickListener {
            SurveyHelper.addOneSurvey("/orderPage", "derateCancel", "AR")
            findNavController().popBackStack()
        }
        binding.confirm.setOnClickListener {
            orderViewModel.requestDrop(orderRes.orderId!!)
            findNavController().popBackStack()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.shape_white_solid__r16)
            setLayout(
                (ScreenUtils.getScreenWidth() * 0.85f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}