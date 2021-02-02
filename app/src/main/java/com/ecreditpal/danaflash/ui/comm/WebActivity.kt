package com.ecreditpal.danaflash.ui.comm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseActivity
import com.ecreditpal.danaflash.base.ImageUploader
import com.ecreditpal.danaflash.data.OSS_BUCKET
import com.ecreditpal.danaflash.data.OSS_ENDPOINT
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.CommUtils
import com.ecreditpal.danaflash.js.AndroidAppInterface
import com.ecreditpal.danaflash.js.WebInterface
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.camera.StartOcr
import com.ecreditpal.danaflash.ui.contact.StartContact
import kotlinx.android.synthetic.main.activity_web.*


class WebActivity : BaseActivity(), LifecycleObserver {

    companion object {
        private const val EXTRA_URL = "url"
        private const val EXTRA_SHOW_TOOLBAR = "show_toolbar"

        fun loadUrl(context: Context?, url: String?, toolbar: Boolean = false) {
            context?.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_SHOW_TOOLBAR, toolbar)
            })
        }
    }

    private lateinit var webView: WebView
    private val webInterface = WebInterface()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.dana_orange)
        setContentView(R.layout.activity_web)

        val url: String = intent?.getStringExtra(EXTRA_URL) ?: ""
        if (url.isEmpty()) {
            finish()
            return
        }

        webView = findViewById(R.id.web)
        val showToolbar = intent?.getBooleanExtra(EXTRA_SHOW_TOOLBAR, false) ?: false
        if (showToolbar) {
            findViewById<ImageView>(R.id.back).setOnClickListener { finish() }
        } else {
            findViewById<View>(R.id.toolbar).visibility = View.GONE
        }

        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE // 不用cache
            javaScriptCanOpenWindowsAutomatically = true
            setSupportZoom(false)
            builtInZoomControls = false
            domStorageEnabled = true
            databaseEnabled = true
            blockNetworkLoads = false
            blockNetworkImage = false
            useWideViewPort = true
            allowFileAccess = true
            loadWithOverviewMode = true
            setAppCacheEnabled(true)
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
            allowFileAccess = true //设置可以访问文件
            javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            loadsImagesAutomatically = true //支持自动加载图片
            defaultTextEncodingName = "utf-8"//设置编码格式
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val path = request?.url?.toString() ?: ""
                if (path.contains("play.google.com") || path.contains("market")) {
                    CommUtils.navGoogleDownload(this@WebActivity, path)
                    return true
                }
                return false
            }
        }

        webView.addJavascriptInterface(AndroidAppInterface(this), "DanaFlash")
        webView.loadUrl(url)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        sendReferIfPossible()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() { // app moved to foreground
        callJs(webInterface.ReturnForeground())
    }

    override fun onBackPressed() {
        callJs(webInterface.backPress())
        if (UserFace.mediaSource.isNotEmpty()) {
            callJs(webInterface.sendMediaSource())
        }
        super.onBackPressed()
    }

    private fun callJs(jsStr: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            webView.post { callJs(jsStr) }
        } else {
            LogUtils.d("ready call js: \n$jsStr")
            webView.evaluateJavascript(jsStr) {}
        }
    }

    fun callbackInterface(methodName: String, json: String) {
        callJs(webInterface.sendCallback(methodName, json))
    }

    fun callAfBack() {
        callbackInterface("getAfInstallConversionData", UserFace.mediaSource)
    }

    fun startOcrPage(json: String?) {
        ocrLauncher.launch(Pair(json, false))
    }

    fun startPhoto(json: String?) {
        ocrLauncher.launch(Pair(json, true))
    }

    fun startLiveness(json: String?) {
        livenessLauncher.launch(json)
    }

    fun startContact() {
        contactLauncher.launch(null)
    }

    fun startRequestPermissions(permissions: Array<String>) {
        requestPLauncher.launch(permissions)
    }

    private fun sendReferIfPossible() {
        UserFace.referrerDetails?.let {
            callJs(webInterface.sendRefer(it.installReferrer))
        }
    }

    private val ocrLauncher = registerForActivityResult(StartOcr()) { pair ->
        if (pair.second == null) {
            callJs(webInterface.ocrBack())
            return@registerForActivityResult
        }

        val imageUri: Uri = pair.second!!
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val objectKey = CommUtils.getOssObjectKey(imageUri)
        ImageUploader().uploadImage(lifecycleScope, bitmap, objectKey) { status, imageBytes ->
            if (status == "1") {
                callJs(
                    webInterface.sendImgUrl(
                        url = "https://${OSS_BUCKET + OSS_ENDPOINT + objectKey}",
                        type = pair.first,
                        img = EncodeUtils.base64Encode2String(imageBytes)
                    )
                )
            }
            callJs(webInterface.isUploading(status))
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        callbackInterface("launchLiveness", it)
    }

    private val contactLauncher = registerForActivityResult(StartContact()) {
        val jsonString = JSON.toJSONString(
            mapOf(
                "success" to it.first,
                "name" to it.second?.name,
                "phone" to it.second?.phone
            )
        )
        callbackInterface("launchContact", jsonString)
    }

    private val requestPLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.forEach { entry ->
                val result = if (entry.value) "1" else "-1"
                callbackInterface("generalRequestPermission", result)
            }
        }
}