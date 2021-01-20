package com.ecreditpal.danaflash.ui.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.databinding.DialogConfirmBinding

class ConfirmDialog(
    private val titleStr: String? = null,
    private val contentStr: String? = null,
    private val positiveStr: String = App.context.getString(R.string.confirm),
    private val negativeStr: String = App.context.getString(R.string.cancel),
    private val negativeClickListener: (() -> Unit)? = null,
    private val positiveClickListener: (() -> Unit)? = null
) : BaseDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DialogConfirmBinding.bind(view).apply {
            title.text = titleStr
            content.text = contentStr
            cancel.text = negativeStr
            confirm.text = positiveStr
            cancel.setOnClickListener {
                negativeClickListener?.invoke()
                dismiss()
            }
            confirm.setOnClickListener {
                positiveClickListener?.invoke()
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.shape_product_card)
            setLayout(
                (ScreenUtils.getScreenWidth() * 0.77f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}