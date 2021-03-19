package com.ecreditpal.danaflash.ui.comm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.GsonUtils
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
import com.ecreditpal.danaflash.ui.camera.CameraActivity
import com.ecreditpal.danaflash.ui.camera.StartLiveness
import com.ecreditpal.danaflash.ui.camera.StartOcr
import com.ecreditpal.danaflash.ui.contact.StartContact
import com.ecreditpal.danaflash.widget.StatusView
import org.json.JSONObject
import java.io.File


class WebActivity : BaseActivity(), LifecycleObserver {

    companion object {
        private const val EXTRA_URL = "url"
        private const val EXTRA_SHOW_TOOLBAR = "show_toolbar"
        private const val EXTRA_ONLY_FOR_DOWNLOAD = "only_for_download"
        private const val EXTRA_BROWSER_MODE = "browser_mode"
        private const val EXTRA_TITLE = "title"

        fun loadUrl(
            context: Context?,
            url: String?,
            toolbar: Boolean = false,
            forDownload: Boolean = false,
            browserMode: Boolean = false,
            title: String? = ""
        ) {
            context?.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_SHOW_TOOLBAR, toolbar)
                putExtra(EXTRA_ONLY_FOR_DOWNLOAD, forDownload)
                putExtra(EXTRA_BROWSER_MODE, browserMode)
                putExtra(EXTRA_TITLE, title)
            })
        }
    }

    private lateinit var webView: WebView
    private lateinit var statusView: StatusView
    private val webInterface = WebInterface()
    private var getPhotoPair: Pair<String?, Uri?>? = null
    private var browserMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web)

        val url: String = intent?.getStringExtra(EXTRA_URL) ?: ""
        if (url.isEmpty()) {
            finish()
            return
        }

        webView = findViewById(R.id.web)
        statusView = findViewById(R.id.status_view)
        statusView.showLoading()
        browserMode = intent?.getBooleanExtra(EXTRA_BROWSER_MODE, false) ?: false
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: ""
        val showToolbar = intent?.getBooleanExtra(EXTRA_SHOW_TOOLBAR, false) ?: false
        if (showToolbar) {
            //显示了标题栏就让backPress可以直接返回吧
            browserMode = true

            findViewById<ImageView>(R.id.back).setOnClickListener { finish() }
            findViewById<TextView>(R.id.title).text = title
        } else {
            findViewById<View>(R.id.toolbar).visibility = View.GONE
        }

        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT // 不用cache
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
            loadsImagesAutomatically = true //支持自动加载图片
            defaultTextEncodingName = "utf-8"//设置编码格式
        }
        setWebContentsDebuggingEnabled(true)

        val onlyForDownload = intent?.getBooleanExtra(EXTRA_ONLY_FOR_DOWNLOAD, false) ?: false

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val path = request?.url?.toString() ?: ""
                if (path.contains("play.google.com") || path.contains("market")) {
                    CommUtils.navGoogleDownload(this@WebActivity, path)
                    //link进入webView只是为了进行重定向以便于跳转Google Play Store
                    if (onlyForDownload) {
                        finish()
                    }
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                statusView.hideStatus()
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
        if (browserMode) {
            finish()
            return
        }
        callJs(webInterface.backPress())
    }

    override fun onDestroy() {
        if (UserFace.mediaSource.isNotEmpty()) {
            callJs(webInterface.sendMediaSource())
        }
        super.onDestroy()
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
        val photoFile = File(CameraActivity.outputDirectory, "pic_" + System.currentTimeMillis() + ".jpg")
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", photoFile)

        val input = kotlin.runCatching {
            if (json.isNullOrEmpty()) {
                ""
            } else {
                JSONObject(json).optString("type")
            }
        }.getOrNull() ?: ""

        getPhotoPair = Pair(input, uri)
        photoLauncher.launch(uri)
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

    private fun uploadUriImage(pair: Pair<String?, Uri?>) {
        val imageUri = pair.second ?: return
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val objectKey = CommUtils.getOssObjectKey(imageUri)
        ImageUploader().uploadImage(lifecycleScope, bitmap, objectKey) { status, imageBytes ->
            if (status == "1") {
                callJs(
                    webInterface.sendImgUrl(
                        url = "http://${OSS_BUCKET}.${OSS_ENDPOINT}/$objectKey",
                        type = pair.first ?: "",
                        img = EncodeUtils.base64Encode2String(imageBytes)
                    )
                )
            }
            callJs(webInterface.isUploading(status))
        }
    }

    private val ocrLauncher = registerForActivityResult(StartOcr()) { pair ->
        if (pair.second == null) {
            callJs(webInterface.ocrBack())
            return@registerForActivityResult
        }

        uploadUriImage(pair)
    }

    private val photoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it && getPhotoPair != null) {
            uploadUriImage(getPhotoPair!!)
        }
    }

    private val livenessLauncher = registerForActivityResult(StartLiveness()) {
        callbackInterface("launchLiveness", it)
    }

    private val contactLauncher = registerForActivityResult(StartContact()) {
        val jsonString = GsonUtils.toJson(
            mapOf(
                "success" to it.first,
                "name" to (it.second?.name ?: ""),
                "phone" to (it.second?.phone ?: "")
            )
        )
        callbackInterface("launchContact", jsonString)
    }

    private val requestPLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val granted = it.all { entry ->
                entry.value == true
            }

            val result = if (granted) "1" else "-1"
            callbackInterface("generalRequestPermission", result)

            //默认启动获取定位任务, 如果没有定位权限, worker里面会做拦截的
            CommUtils.startGetLocationWorker(this)
            CommUtils.saveDeviceId(this, lifecycleScope)
        }
}