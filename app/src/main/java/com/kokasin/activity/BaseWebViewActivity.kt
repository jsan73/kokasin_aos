package com.kokasin.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import com.kokasin.Constants
import com.kokasin.R
import com.kokasin.dialog.CommonDialog
import com.kokasin.util.CommonUtil
import com.kokasin.util.DomainUtil
import com.kokasin.util.LogUtil
import com.kokasin.util.PreferenceUtil
import com.kokasin.webview.AppInterface

open class BaseWebViewActivity :BaseActivity() {

    private lateinit var mBaseActivity: BaseWebViewActivity
    private lateinit var mBaseWebView: WebView
    private lateinit var mProgressLoading: View

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Constants.ACTION.ASSET_CHANGE_COMPLETE) {
                mBaseWebView.reload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_webview)
        registerBroadcastReceiver()
    }

    override fun onResume() {
        super.onResume()
        CookieSyncManager.getInstance().startSync()
    }

    override fun onPause() {
        super.onPause()
        CookieSyncManager.getInstance().stopSync()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION.ASSET_CHANGE_COMPLETE)
        registerReceiver(receiver, filter)
    }

    // ?????? ?????????
    fun setWebView(activity: BaseWebViewActivity, webView: WebView) {
        mBaseActivity = activity
        mBaseWebView = webView
        //mProgressLoading = progressLoading

        // ?????? Sync
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieSyncManager.createInstance(this)
        }

        // ????????? false (?????? ??? ??????????????? ??????)
        val isDebug = PreferenceUtil(this).getValue(PreferenceUtil.KEYS.DEBUGGING_YN, "Y")
        if(isDebug == "Y") {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        else {
            WebView.setWebContentsDebuggingEnabled(false)
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setGeolocationEnabled(true)
        webSettings.textZoom = 100
        webSettings.useWideViewPort = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(false)
        webSettings.setSupportMultipleWindows(true)
        webSettings.setAppCacheEnabled(true)

        val userAgent = webSettings.userAgentString
        webSettings.userAgentString = "$userAgent kokasin-aos"

        webView.requestFocus()
        webView.isFocusableInTouchMode = true

        webView.webViewClient = object : WebViewClient() {

            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val uri: Uri = Uri.parse(url)
                return handleUri(uri)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri: Uri = request!!.url
                return handleUri(uri)
            }

            private fun handleUri(uri: Uri): Boolean {
                LogUtil.e("handleUri : $uri")
                LogUtil.i("++ Uri : host = ${uri.host}, scheme = ${uri.scheme}")
                //val url = uri.toString().toLowerCase()
                val url = uri.toString()
                when {
                    url == DomainUtil.mainUrl(mBaseActivity) -> {
                        checkPushRun(intent)  // ?????? ?????? ?????? ??????
                    }

                    url.startsWith("intent:") -> {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            val existPackage = packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                            if(existPackage != null) {
                                startActivity(intent)
                            }
                            else {
                                val dialog = CommonDialog(mBaseActivity)
                                val cancelListener = View.OnClickListener {
                                    dialog.dismiss();
                                }
                                val okListener = View.OnClickListener {
                                    dialog.dismiss()
                                    CommonUtil.moveToPlayStore(applicationContext, intent.getPackage()!!)
                                }

                                openDialogAlert(dialog, "??? ?????????", "?????? ???????????? ?????? ????????????. ??? ????????? ?????? Play ???????????? ????????????????",
                                    "??????", "??????", true, cancelListener, okListener)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        return true
                    }
                    url.startsWith("market:") -> {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if(intent != null) {
                            startActivity(intent)
                        }
                        return true
                    }
                    url.startsWith("tel:") -> {
                        val intent = Intent(Intent.ACTION_DIAL, uri)
                        startActivity(intent)
                        return true
                    }

                    url.startsWith("sms:") || url.startsWith("mms:") -> {
                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                        startActivity(intent)
                        return true
                    }

                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                LogUtil.e("test", "onPageStarted :: $url")

                // ?????? ??? ?????? ????????? ????????? ???????????? (????????? ?????? ?????? ????????? ?????? ??????)
                if(!url!!.contains("sendUrl")) {
                    setLoadingProgressBar(View.VISIBLE)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setLoadingProgressBar(View.GONE)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync()
                } else {
                    CookieManager.getInstance().flush()
                }

                val intent = Intent(Constants.ACTION.WEBVIEW_FINISHED)
                intent.setPackage(packageName)
                sendBroadcast(intent)
            }
        }

        webView.webChromeClient = object: WebChromeClient() {
            private var mCustomView: View? = null
            private lateinit var mFullscreenContainer: FrameLayout
            private var mOriginalOrientation: Int = 0
            private var COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            private lateinit var mCallBack: CustomViewCallback

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (mCustomView != null) {
                    callback!!.onCustomViewHidden()
                    return
                }

                mOriginalOrientation = requestedOrientation
                val decor = mBaseActivity.window.decorView as FrameLayout
                mFullscreenContainer = FullscreenHolder(mBaseActivity)
                mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS)
                decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS)
                mCustomView = view!!
                setFullscreen(true)
                mCallBack = callback!!

                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                if (mCustomView == null) {
                    return
                }

                setFullscreen(false);
                val decor = mBaseActivity.window.decorView as FrameLayout
                decor.removeView(mFullscreenContainer)
                mCallBack.onCustomViewHidden()
                mBaseActivity.requestedOrientation = mOriginalOrientation
                super.onHideCustomView()
            }

            private fun setFullscreen(enabled: Boolean) {
                val win = window
                val winParams = win.attributes
                val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
                if (enabled) {
                    winParams.flags = (winParams.flags or bits)
                } else {
                    winParams.flags = (winParams.flags and bits.inv())
                    if (mCustomView != null) {
                        mCustomView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
                win.attributes = winParams
            }

            inner class FullscreenHolder : FrameLayout {
                constructor(context: Context) : super(context) {
                    init(context)
                }

                constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
                    init(context)
                }

                constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
                    context,
                    attrs,
                    defStyleAttr
                ) {
                    init(context)
                }

                private fun init(context: Context) {
                    setBackgroundColor(Color.BLACK)

                }
                override fun onTouchEvent(event: MotionEvent?): Boolean {
                    return true
                }
            }



            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (message != null) {
                    openDialogAlert(getString(R.string.app_name), message, "??????", true)
                    result?.confirm()
                    return true
                }
                return false
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                LogUtil.d("[WebView JS Console Error] = ${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} sourceID : $[consoleMessage.sourceId()]")
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback?.invoke(origin, true, false)
            }
        }
    }

    fun setJavascriptInterface(appListener: AppInterface.AppListener?) {
        mBaseWebView.addJavascriptInterface(AppInterface(mBaseActivity, mBaseWebView, appListener), "AppInterface")
    }

    // ????????? URL ??????
    fun loadUrl(linkUrl: String) {
        mBaseWebView.loadUrl(linkUrl)
    }

    // ?????? ?????? ?????? ??????????????? ??????
    fun checkPushRun(intent: Intent?) {
        if(intent != null) {
            val bundle = intent.extras
            LogUtil.e("checkPushRun() :: $bundle")

            if (bundle != null) {
                val linkUrl = bundle.getString(Constants.PUSH.PUSH_URL)
                if (!TextUtils.isEmpty(linkUrl)) {
                    mBaseWebView.loadUrl(DomainUtil.serverUrl(this) + "$linkUrl")

                    getIntent().replaceExtras(Bundle())
                    getIntent().action = ""
                    getIntent().data = null
                    getIntent().flags = 0
                }
            }
        }
    }

    // ??????????????? ??????
//    fun executeLogin() {
//        // ?????? ???????????? ????????? ????????? URL ????????????, ??????????????? ????????? SSO ???????????? ??????
//        val sdfNow = SimpleDateFormat("yyyyMMddHHmmss")
//        val deviceId: String = CommonUtil.getDeviceId(this)
//        val pushId: String = CommonUtil.getPushId(this)
//        val dateTime = sdfNow.format(Date(System.currentTimeMillis()))
//        val encDeviceId: String = CommonUtil.getEndData(deviceId)
//        val encPushId: String = CommonUtil.getEndData(pushId)
//        val encDateTime: String = CommonUtil.getEndData(dateTime)
//
//        try {
//            val postData =
//                "dvceId=" + URLEncoder.encode(encDeviceId, "utf-8") +
//                        "&pushId=" + URLEncoder.encode(encPushId, "utf-8") +
//                        "&dt=" + URLEncoder.encode(encDateTime, "utf-8") +
//                        "&serviceId=rion-mini"  // serviceId ??? ????????? ??? ????????? ???????????? ??? (????????? ?????? ??????)
//
//            mBaseWebView.postUrl(DomainUtil.loginUrl(this), postData.toByteArray())
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun loadMain() {
        try {
            mBaseWebView.loadUrl(DomainUtil.serverUrl(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun reloadMap() {
        var url = mBaseWebView.url
        if(url.equals(DomainUtil.mainUrl(this))) {
            mBaseWebView.loadUrl("javascript:window.InterfaceLocation.requestLocation()")
        }else{
            loadMain()
        }
    }

    // ???????????? ????????? show/hide
    public fun setLoadingProgressBar(visibility: Int) {
//        mProgressLoading.visibility = visibility
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == AppInterface.REQ.WEBVIEW_CLOSE) {  // ?????? ?????? ??? ?????? ??????
                val callbackType = data?.getIntExtra(Constants.EXTRA.CALLBACK_TYPE, 0)
                if(callbackType == AppInterface.TYPE.LINK) {  // URL ??????
                    val linkUrl = data?.getStringExtra(Constants.EXTRA.LINK_URL)
                    if (linkUrl != null) {
                        mBaseWebView.loadUrl(linkUrl)
                    }
                }
                else if(callbackType == AppInterface.TYPE.FUNCTION) {  // ?????? ??????
                    val function = data?.getStringExtra(Constants.EXTRA.FUNCTION_NAME)
                    mBaseWebView.loadUrl("javascript:${function}")  // ???????????? ??????
                }
            }
        }
    }
}