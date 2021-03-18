package com.ecreditpal.danaflash.ui.contact

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.isAlphabet
import com.ecreditpal.danaflash.model.ContactRes
import com.ecreditpal.danaflash.widget.SideBarSortView
import com.ecreditpal.danaflash.widget.StatusView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class ContactFragment : BaseFragment() {

    private lateinit var statusView: StatusView
    private lateinit var sideBarSortView: SideBarSortView
    private val dataList = mutableListOf<Any>()

    private val contactAdapter = ContactAdapter(dataList) {
        resultBack("1", it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        statusView = view.findViewById(R.id.status_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = contactAdapter
        }
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        sideBarSortView = view.findViewById(R.id.side_bar)
        sideBarSortView.clickListener = object : SideBarSortView.OnIndexChangedListener {
            override fun onSideBarScrollUpdateItem(word: String?) {
                val index = dataList.indexOf(word)
                layoutManager.scrollToPositionWithOffset(index, 0)
            }

            override fun onSideBarScrollEndHideText() {

            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    val data = dataList[position]
                    if (data is String) {
                        sideBarSortView.onItemScrollUpdateText(data)
                    }
                }
            }
        })

        if (ContextCompat.checkSelfPermission(
                view.context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            contactLauncher.launch(Manifest.permission.READ_CONTACTS)
            return
        }

        loadContactsData()
    }

    private val contactLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            loadContactsData()
        } else {
            resultBack("-1", null)
        }
    }

    private fun loadContactsData() {
        lifecycleScope.launch {
            statusView.showLoading()
            val list = withContext(Dispatchers.IO) {
                CommUtils.getAllContacts(context)
            }
            when {
                list == null -> {
                    statusView.matchError(IOException()) {
                        loadContactsData()
                    }
                }
                list.isEmpty() -> {
                    resultBack("0", null)
                }
                else -> {
                    dataList.clear()
                    list.filter { it.name.isNullOrEmpty().not() }
                        .sortedBy {
                            if (it.name?.firstOrNull().isAlphabet()) {
                                it.name?.first()?.toUpperCase()
                            } else {
                                //任意返回一个ASCII表上比字母靠后得字符, 来使其排序到最后
                                '~'
                            }
                        }
                        .groupBy {
                            if (it.name?.firstOrNull().isAlphabet()) {
                                it.name?.first()?.toUpperCase()
                            } else {
                                "#"
                            }

                        }
                        .forEach {
                            dataList.add(it.key.toString().toUpperCase(Locale.ROOT))
                            dataList.addAll(it.value)
                        }

                    sideBarSortView.setValidLabels(dataList)
                    sideBarSortView.visibility = View.VISIBLE
                    contactAdapter.notifyDataSetChanged()
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