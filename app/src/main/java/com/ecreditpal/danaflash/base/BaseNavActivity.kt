package com.ecreditpal.danaflash.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NavigationRes
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.DialogFragmentNavigator
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.helper.SurveyHelper

abstract class BaseNavActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_base)
        setDefToolbarNav()
    }

    //点击返回按钮的回调, 一般做埋点用
    var backClickListener: (() -> Unit)? = null

    private fun setDefToolbarNav() {
        if (navGraphId() == 0) {
            throw RuntimeException("必须传入NavHost依赖的res/navigation/里面的导航文件.")
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            SurveyHelper.addOneSurvey("任意p", "back")
            backClickListener?.invoke()
            onBackPressed()
        }
        try {
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            navController.setGraph(navGraphId())
            navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
                //Dialog no need to show title
                if (destination !is DialogFragmentNavigator.Destination) {
                    findViewById<TextView>(R.id.title).text = destination.label
                }
            }
        } catch (e: Exception) {
            //Do not have nav host fragment
            Log.e("BaseNavActivity", "Set navigation failed.", e)
        }
    }

    protected fun hideToolbar() {
        findViewById<View>(R.id.toolbar)?.visibility = View.GONE
    }

    /**
     * @return 返回当前Activity持有的导航文件的资源id
     */
    @NavigationRes
    protected abstract fun navGraphId(): Int
}