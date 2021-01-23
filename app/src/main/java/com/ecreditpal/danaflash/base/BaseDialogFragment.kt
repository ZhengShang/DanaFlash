package com.ecreditpal.danaflash.base

import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

/**
 * 原则上需要用Navigation库来调用navigate显示, 然后用要用findNavController().popBackStack()来关闭
 * 否则可能会出现当前destination不对造成的崩溃问题
 * 如果是用show方法显示的, 需要用dismiss来消失
 */
open class BaseDialogFragment : AppCompatDialogFragment() {

    fun show(manager: FragmentManager) {
        show(manager, this::class.java.simpleName)
    }
}