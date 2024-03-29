package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ScreenUtils
import com.ecreditpal.danaflash.MainActivity
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseDialogFragment
import com.ecreditpal.danaflash.data.DataStoreKeys
import com.ecreditpal.danaflash.helper.writeDsData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionTipsDialog : BaseDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permission_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        view.findViewById<TextView>(R.id.content).apply {
            text = "Dana Flash membutuhkan\n\n" +
                    "Kami akan meminta beberapa akses data untuk proses verifikasi. Kami juga akan melindungi privasi Anda dan menjaga keamanan informasi Anda.\n" +
                    "*Lokasi\n" +
                    "*Foto dan Rekam Video\n" +
                    "*Kontak\n" +
                    "*Peralatan perangkat\n" +
                    "*Penyimpanan perangkat\n"
        }
        view.findViewById<TextView>(R.id.agree).setOnClickListener {
            writeValue()
            backAndRequestPermissionInMain(true)
        }
        view.findViewById<TextView>(R.id.disagree).setOnClickListener {
            writeValue()
            backAndRequestPermissionInMain(false)
        }
    }

    private fun backAndRequestPermissionInMain(request: Boolean) {
        findNavController().popBackStack()
        val a = activity as? MainActivity
        a?.let { a.requestLocationPermission(request) }
    }

    private fun writeValue() {
        GlobalScope.launch {
            context.writeDsData(DataStoreKeys.IS_SHOW_PERMISSION_TIPS, false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.shape_white_solid__r16)
            setLayout(
                (ScreenUtils.getScreenWidth() * 0.8f).toInt(),
                (ScreenUtils.getScreenHeight() * 0.6f).toInt()
            )
        }
    }
}