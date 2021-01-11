package com.ecreditpal.danaflash.ui.comm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.databinding.DialogUpdateBinding
import com.ecreditpal.danaflash.ui.settings.VersionViewModel

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

        val versionVm = ViewModelProvider(requireActivity()).get(VersionViewModel::class.java)

        val versionRes = arguments?.let {
            UpdateDialogArgs.fromBundle(it).versionRes
        } ?: return
//        isCancelable = versionRes.updateStatus != 2

        val binding = DialogUpdateBinding.bind(view)
        binding.lifecycleOwner = this
        binding.versionRes = versionRes
        binding.update.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(versionRes.link)
                setPackage(view.context.packageName)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                ToastUtils.showLong(R.string.failed_to_update_in_google_store)
            }
        }
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