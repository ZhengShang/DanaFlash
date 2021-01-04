package com.ecreditpal.danaflash.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.NavigationRes
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import com.ecreditpal.danaflash.R

abstract class BaseNavActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_base)
        setDefToolbarNav()
    }

    private fun setDefToolbarNav() {
        if (navGraphId() == 0) {
            throw RuntimeException("必须传入NavHost依赖的res/navigation/里面的导航文件.")
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        try {
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            navController.setGraph(navGraphId())
            navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
                toolbar.title = destination.label
            }
        } catch (e: Exception) {
            //Do not have nav host fragment
            Log.e("BaseNavActivity", "Set navigation failed.", e)
        }
    }

    protected fun hideToolbar() {
        findViewById<Toolbar>(R.id.toolbar)?.visibility = View.GONE
    }

    /**
     * @return 返回当前Activity持有的导航文件的资源id
     */
    @NavigationRes
    protected abstract fun navGraphId(): Int
}