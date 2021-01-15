package com.ecreditpal.danaflash.ui.comm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BindingViewHolder<T : ViewBinding> private constructor(val binding: T) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun <T : ViewDataBinding> create(parent: ViewGroup, layoutId: Int): BindingViewHolder<T> {

            val _binding: T = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId,
                parent,
                false
            )
            _binding.executePendingBindings()
            return BindingViewHolder(_binding)
        }
    }
}