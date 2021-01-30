package com.ecreditpal.danaflash.ui.comm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.EncodeUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseActivity
import com.ecreditpal.danaflash.base.ImageUploader
import com.ecreditpal.danaflash.data.OSS_BUCKET
import com.ecreditpal.danaflash.data.OSS_ENDPOINT
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.helper.toBytes
import com.ecreditpal.danaflash.js.AndroidAppInterface
import com.ecreditpal.danaflash.js.WebInterface
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.camera.StartOcr
import com.ecreditpal.danaflash.ui.contact.StartContact

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
    private val webInterface = WebInterface()

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
        webView.addJavascriptInterface(AndroidAppInterface(this), "DanaFlash")
        webView.loadUrl(url)
    }

    private fun callJs(jsStr: String) {
        webView.evaluateJavascript(jsStr) { value ->

        }
    }

    fun startOcrPage(json: String?) {
        ocrLauncher.launch(json)
    }

    fun startLiveness(json: String?) {
        livenessLauncher.launch(json)
    }

    fun startContact() {
        contactLauncher.launch(null)
    }

    private val ocrLauncher = registerForActivityResult(StartOcr()) { pair ->
        ImageUploader().uploadImage(lifecycleScope, pair.second) {
            callJs(webInterface.isUploading(it))
            if (it == "1") {
                callJs(
                    webInterface.sendImgUrl(
                        url = "https://${OSS_BUCKET + OSS_ENDPOINT + CommUtils.getOssObjectKey(pair.second!!)}",
                        type = pair.first,
                        img = EncodeUtils.base64Encode2String(pair.second.toBytes(this))
                    )
                )
            }
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        CommUtils.stepAfterLiveness(lifecycleScope, this, it)
    }

    private val contactLauncher = registerForActivityResult(StartContact()) {
        when (it.first) {
            "0" -> {

            }
            "-1" -> {

            }
            "1" -> {

            }
        }
    }
}