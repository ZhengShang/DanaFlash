package com.ecreditpal.danaflash.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.ui.personal.PersonalFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager2>(R.id.pager)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = object :
            FragmentStateAdapter(this) {

            val fragments = listOf(
                HomeFragment(), PersonalFragment()
            )

            override fun getItemCount() = 2

            override fun createFragment(position: Int) = fragments[position]
        }
        view.findViewById<BottomNavigationView>(R.id.nav_view).apply {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_home -> viewPager.setCurrentItem(0, false)
                    else -> viewPager.setCurrentItem(1, false)
                }
                true
            }
        }
    }
}