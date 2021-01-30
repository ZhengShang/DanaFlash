package com.ecreditpal.danaflash.ui.contact

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.model.ContactRes
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.launch

class ContactFragment : BaseFragment() {

    private lateinit var statusView: StatusView
    private val dataList = mutableListOf<Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactAdapter = ContactAdapter(dataList) {
            resultBack("1", it)
        }

        statusView = view.findViewById(R.id.status_view)
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = contactAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }

        if (ContextCompat.checkSelfPermission(
                view.context,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            resultBack("-1", null)
        }

        loadContactsData()
    }

    private fun loadContactsData() {
        lifecycleScope.launch {
            statusView.showLoading()
            val list = CommUtils.getAllContacts(context)
            when {
                list == null -> {
                    statusView.showErrorWithRetry(getString(R.string.failed_to_load)) {
                        loadContactsData()
                    }
                }
                list.isEmpty() -> {
                    resultBack("0", null)
                }
                else -> {
                    dataList.clear()
                    list.sortedBy { it.name }
                        .groupBy { it.name?.first() }
                        .onEach {
                            dataList.add(it.key.toString())
                            dataList.addAll(it.value)
                        }

                    statusView.hideStatus()
                }
            }
        }
    }

    private fun resultBack(result: String, contactRes: ContactRes?) {
        activity?.setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(ContactActivity.EXTRA_CHOSEN_RESULT, result)
                putExtra(ContactActivity.EXTRA_CHOSEN_CONTACT, contactRes)
            }
        )
        activity?.finish()
    }
}