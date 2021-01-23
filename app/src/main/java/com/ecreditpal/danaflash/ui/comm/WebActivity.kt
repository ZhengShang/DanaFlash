package com.ecreditpal.danaflash.ui.comm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseActivity

class WebActivity : BaseActivity() {

    companion object {
        private const val URL = "url"

        fun loadUrl(context: Context?, url: String?) {
            context?.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(URL, url)
            })
        }
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.dana_orange)
        webView = WebView(this)
        setContentView(webView)

        val url: String = intent?.getStringExtra(URL) ?: ""
        if (url.isEmpty()) {
            finish()
            return
        }
        webView.apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 不用cache
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.blockNetworkLoads = false
            settings.blockNetworkImage = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setAppCacheEnabled(true)
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
            settings.allowFileAccess = true //设置可以访问文件
            settings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            settings.loadsImagesAutomatically = true //支持自动加载图片
            settings.defaultTextEncodingName = "utf-8"//设置编码格式


        }
        webView.loadUrl(url)
    }

}