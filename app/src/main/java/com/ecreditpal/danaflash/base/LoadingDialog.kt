package com.ecreditpal.danaflash.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.ConvertUtils
import com.ecreditpal.danaflash.R

class LoadingDialog : BaseDialogFragment() {
    companion object {
        fun newInstance() = LoadingDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.shape_white_solid__r16)
            setLayout(
                ConvertUtils.dp2px(100f),
                ConvertUtils.dp2px(100f)
            )
        }
    }
}