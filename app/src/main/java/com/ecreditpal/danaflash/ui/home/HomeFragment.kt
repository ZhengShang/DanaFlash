package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeBinding
    private val adapter = ProductAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.recycler.adapter = adapter

        lifecycleScope.launch {
            homeViewModel.flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}