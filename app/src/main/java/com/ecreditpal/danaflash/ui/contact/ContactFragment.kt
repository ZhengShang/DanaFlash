package com.ecreditpal.danaflash.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.widget.StatusView

class ContactFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusView = view.findViewById<StatusView>(R.id.status_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler)


    }
}