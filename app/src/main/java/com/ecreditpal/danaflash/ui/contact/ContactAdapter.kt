package com.ecreditpal.danaflash.ui.contact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.databinding.ItemContactBinding
import com.ecreditpal.danaflash.model.ContactRes
import com.ecreditpal.danaflash.ui.comm.BindingViewHolder

class ContactAdapter(
    private val dataList: List<Any>,
    private val chosenListener: (contact: ContactRes?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            HeaderHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_contact_header, parent, false)
            )
        } else {
            BindingViewHolder.create<ItemContactBinding>(parent, R.layout.item_contact)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == HEADER) {
            (holder as HeaderHolder).bind(dataList[position] as String)
        } else {
            val res = dataList[position] as ContactRes?
            (holder as BindingViewHolder<ItemContactBinding>).binding.info = res

            holder.itemView.setOnClickListener { chosenListener.invoke(res) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is ContactRes) {
            ITEM
        } else {
            HEADER
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    companion object {
        private const val HEADER = 1
        private const val ITEM = 2
    }

}

private class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(value: String) {
        itemView.findViewById<TextView>(R.id.text).text = value
    }
}