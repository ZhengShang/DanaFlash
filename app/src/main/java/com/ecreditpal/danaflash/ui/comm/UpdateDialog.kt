package com.ecreditpal.danaflash.ui.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.databinding.DialogUpdateBinding
import com.ecreditpal.danaflash.ui.home.HomeViewModel

class UpdateDialog : BaseDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val versionRes = arguments?.let {
            UpdateDialogArgs.fromBundle(it).versionRes
        }
        if (versionRes == null || versionRes.updateStatus == 0) {
            findNavController().popBackStack()
            return
        }
        isCancelable = versionRes.updateStatus != 2

        val binding = DialogUpdateBinding.bind(view)
        binding.lifecycleOwner = this
        binding.versionRes = versionRes
        binding.close.setOnClickListener { findNavController().popBackStack() }
        binding.close.visibility = if (isCancelable) View.VISIBLE else View.GONE
        binding.update.setOnClickListener {
            WebActivity.loadUrl(context, versionRes.link, forDownload = true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //需要显示下一个弹窗
        val homeViewModel: HomeViewModel by activityViewModels()
        homeViewModel.tryShowPopDialog()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(null)
            setLayout(
                (ScreenUtils.getScreenWidth() * 0.8f).toInt(),
                (ScreenUtils.getScreenHeight() * 0.65f).toInt()
            )
        }
    }
}