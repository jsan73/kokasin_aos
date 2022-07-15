package com.kokasin.webview

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.gson.JsonParser
import com.google.zxing.integration.android.IntentIntegrator
import com.kokasin.Constants
import com.kokasin.activity.BaseWebViewActivity
import com.kokasin.activity.CommonWebViewActivity
import com.kokasin.dialog.CommonDialog
import com.kokasin.util.CommonUtil
import com.kokasin.util.LogUtil
import com.kokasin.util.PreferenceUtil
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


class AppInterface(activity: BaseWebViewActivity, webView: WebView, appListener: AppListener?) {

    object REQ {
        const val WEBVIEW_CLOSE = 1000;
    }

    object TYPE {
        const val LINK = 1000;
        const val FUNCTION = 2000;
    }

    private var mActivity: BaseWebViewActivity = activity
    private var mWebView: WebView = webView
    private var mMessageMMS = ""
    private var mImageUrlMMS = ""
    private var mNId = 0  // Notification ID
    private var isFirstCall: Boolean = true  // API 한번만 호출

    interface AppListener {
        fun onJsCallBack(webView: WebView?, method: String?, param: String?)
    }

    private val mListener: AppListener? = appListener

    private val mListenerMms: RequestListener<Drawable> = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            Thread(Runnable {
                try {
                    val bitmap = (resource as BitmapDrawable).bitmap
                    val file: File? = CommonUtil.getImageFile(mActivity, bitmap, mImageUrlMMS)
                    if (file != null) {
                        val targetedShareIntents: ArrayList<Intent> = ArrayList()
                        val nameArray = ArrayList<String>()
                        nameArray.add("mms")
                        nameArray.add("messaging")

                        val mmsIntent: Intent? = CommonUtil.getShareIntent(
                            mActivity,
                            nameArray,
                            "",
                            mMessageMMS
                        )

                        if (mmsIntent != null) {
                            // targetSdkVersion 24 이상
                            val uri = FileProvider.getUriForFile(
                                mActivity,
                                mActivity.packageName,
                                file
                            )

                            // targetSdkVersion 23 이하
                            //Uri uri = getImageUri(getApplicationContext(), bitmap);

                            mmsIntent.putExtra(Intent.EXTRA_STREAM, uri)
                            mmsIntent.type = "image/*"
                            targetedShareIntents.add(mmsIntent)

                            val chooser = Intent.createChooser(
                                targetedShareIntents.removeAt(0) as Intent?,
                                ""
                            )
                            chooser.putExtra(
                                Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(
                                    arrayOf<Parcelable>()
                                )
                            )
                            mActivity.startActivity(chooser)
                        } else {
                            Thread(Runnable {
                                Looper.prepare()
                                Handler().post {
                                    Toast.makeText(
                                        mActivity,
                                        "문자를 보낼 수 있는 앱이 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Looper.loop()
                            }).start()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
            return false
        }
    }

    @JavascriptInterface
    fun postMessage(jsonParam: String) {
        LogUtil.e("postMessage : $jsonParam")

        val json = JsonParser.parseString(jsonParam).asJsonObject
        val method: String = CommonUtil.getStringJsonObject(json, "method")

        when (method) {
            // 웹뷰 새창 열기
            "openWebView" -> {
                val url = CommonUtil.getStringJsonObject(json, "url")
                val intent = Intent(mActivity, CommonWebViewActivity::class.java)
                intent.putExtra(Constants.EXTRA.LINK_URL, url)
                intent.putExtra(Constants.EXTRA.BACK_KEY_YN, "Y")
                mActivity.startActivityForResult(intent, REQ.WEBVIEW_CLOSE)
            }

            // 웹뷰 닫기
            "closeWebView" -> {
                val url = CommonUtil.getStringJsonObject(json, "url")
                LogUtil.e("closeWebView :: $url")

                // 웹뷰 닫은 후, 메인화면 URL 이동 (메인 웹뷰 > WEBVIEW_CLOSE)
                if (!TextUtils.isEmpty(url)) {
                    val intent = Intent()
                    intent.putExtra(Constants.EXTRA.CALLBACK_TYPE, TYPE.LINK)
                    intent.putExtra(Constants.EXTRA.LINK_URL, url)
                    mActivity.setResult(Activity.RESULT_OK, intent)
                }

                mActivity.finish()
            }

            // 웹뷰 닫은 후 콜백함수 호출
            "closeWebViewCall" -> {
                val callback = CommonUtil.getStringJsonObject(json, "callback")  // 콜백함수 -> 파라미터 포함

                // 웹뷰 닫은 후, 메인화면 URL 이동 (메인 웹뷰 > WEBVIEW_CLOSE)
                if (!TextUtils.isEmpty(callback)) {
                    val intent = Intent()
                    intent.putExtra(Constants.EXTRA.CALLBACK_TYPE, TYPE.FUNCTION)
                    intent.putExtra(Constants.EXTRA.FUNCTION_NAME, callback)
                    mActivity.setResult(Activity.RESULT_OK, intent)
                }

                mActivity.finish()
            }

            // 토스트 메시지 출력
            "showToast" -> {
                val message = CommonUtil.getStringJsonObject(json, "message")
                Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
            }

            // 알림창 열기 (버튼 1개)
            "showDialogAlert" -> {
                val title = CommonUtil.getStringJsonObject(json, "title")
                val message = CommonUtil.getStringJsonObject(json, "message")
                mActivity.openDialogAlert(title, message, "확인", false)
            }

            // 알림창 열기 (버튼 2개)
            "showDialogConfirm" -> {
                val title = CommonUtil.getStringJsonObject(json, "title")
                val message = CommonUtil.getStringJsonObject(json, "message")
                val callback = CommonUtil.getStringJsonObject(json, "callback")
                val callbackCancel = CommonUtil.getStringJsonObject(json, "callbackCancel")
                var okButton = CommonUtil.getStringJsonObject(json, "okButton")
                var cancelButton = CommonUtil.getStringJsonObject(json, "cancelButton")

                if(TextUtils.isEmpty(okButton)) {
                    okButton = "확인"
                }

                if(TextUtils.isEmpty(cancelButton)) {
                    cancelButton = "취소"
                }

                val dialog = CommonDialog(mActivity)
                val cancelListener = View.OnClickListener {
                    dialog.dismiss()

                    // 취소 콜백 있는 경우 실행
                    if(!TextUtils.isEmpty(callbackCancel)) {
                        mWebView.post {
                            mWebView.loadUrl("javascript:${callbackCancel}()")
                        }
                    }
                }
                val okListener = View.OnClickListener {
                    mWebView.post {
                        dialog.dismiss()
                        mWebView.loadUrl("javascript:${callback}()")
                    }
                }

                mActivity.openDialogAlert(
                    dialog,
                    title,
                    message,
                    cancelButton,
                    okButton,
                    false,
                    cancelListener,
                    okListener
                )
            }

            // 해당 URL로 웹뷰 로드
            "loadWebViewUrl" -> {
                mWebView.post {
                    val url = CommonUtil.getStringJsonObject(json, "url")
                    mWebView.loadUrl(url)
                }
            }

            // 앱 버전 가져오기
            "getAppVersion" -> {
                val appVersion = CommonUtil.getAppVersion(mActivity)
                val callback = CommonUtil.getStringJsonObject(json, "callback")
                mWebView.post {
                    mWebView.loadUrl("javascript:${callback}('${appVersion}')")
                }
            }

            // OS 버전 가져오기
            "getOsVersion" -> {
                val callback = CommonUtil.getStringJsonObject(json, "callback")
                mWebView.post {
                    mWebView.loadUrl("javascript:${callback}('${Build.VERSION.RELEASE}')")
                }
            }



            // 로그인 ID 넣어서 기기정보 등록 (서버 호출)
            "sendDeviceInfo" -> {
                val guardPhone = CommonUtil.getStringJsonObject(json, "guardPhone")
                val token = CommonUtil.getStringJsonObject(json, "token")
                val refreshToken = CommonUtil.getStringJsonObject(json, "refreshToken")

                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.GUARD_PHONE, guardPhone)
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.TOKEN, token)
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.REFRESH_TOKEN, refreshToken)


                if(isFirstCall) {
                    mActivity.requestRegisterInfo()
                    isFirstCall = false
                }
            }

            // 로그아웃 시 데이터 삭제
            "appLogout" -> {
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.GUARD_PHONE, "")
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.TOKEN, "")
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.REFRESH_TOKEN , "")
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.TOKEN_DATE, 0)
            }


            // 앱 종료
            "exitApp" -> {
                val message = CommonUtil.getStringJsonObject(json, "message")
                if (TextUtils.isEmpty(message)) {
                    ActivityCompat.finishAffinity(mActivity)
                } else {
                    val dialog = CommonDialog(mActivity)
                    val cancelListener = View.OnClickListener {
                        dialog.dismiss()
                    }
                    val okListener = View.OnClickListener {
                        mWebView.post {
                            dialog.dismiss()
                            ActivityCompat.finishAffinity(mActivity)
                        }
                    }

                    mActivity.openDialogAlert(
                        dialog,
                        "앱 종료",
                        message,
                        "취소",
                        "확인",
                        false,
                        cancelListener,
                        okListener
                    )
                }
            }

            // 키보드 내림
            "hideKeyboard" -> {
                CommonUtil.hideKeyboard(mActivity, mWebView)
            }

            // 자동로그인 실행
//            "runAutoLogin" -> {
//                mWebView.post {
//                    mActivity.executeLogin()
//                }
//            }

            // 로딩바 보임/숨김
            "setLoadingView" -> {
                mWebView.post {
                    val visible = CommonUtil.getStringJsonObject(json, "visible")
                    if (visible == "Y") {
                        mActivity.setLoadingProgressBar(View.VISIBLE)
                    } else {
                        mActivity.setLoadingProgressBar(View.GONE)
                    }
                }
            }

            // 디버깅 사용 설정
            "setDebugging" -> {
                val useYn = CommonUtil.getStringJsonObject(json, "useYn")
                PreferenceUtil(mActivity).put(PreferenceUtil.KEYS.DEBUGGING_YN, useYn)
                if (useYn == "Y") {
                    mWebView.post {
                        Toast.makeText(mActivity, "디버깅 ON", Toast.LENGTH_SHORT).show()
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                } else {
                    mWebView.post {
                        Toast.makeText(mActivity, "디버깅 OFF", Toast.LENGTH_SHORT).show()
                        WebView.setWebContentsDebuggingEnabled(false)
                    }
                }
            }

            // 기기정보 리턴
            "getDeviceInfo" -> {
                val callback = CommonUtil.getStringJsonObject(json, "callback")
                val pushToken = PreferenceUtil(mActivity).getValue(PreferenceUtil.KEYS.PUSH_ID, "")
                val guardPhone = PreferenceUtil(mActivity).getValue(PreferenceUtil.KEYS.GUARD_PHONE, "")
                val mapParams: MutableMap<String, String> = HashMap()

                mapParams["os_type"] = "A"
                mapParams["dvc_id"] = CommonUtil.getDeviceId(mActivity)
                mapParams["app_ver"] = CommonUtil.getAppVersion(mActivity)
                mapParams["dvc_mdel"] = Build.MODEL
                mapParams["os_ver"] = Build.VERSION.RELEASE
                mapParams["push_token"] = pushToken
                mapParams["guard_phone"] = guardPhone

                // Map to JSON
                val jsonParams = JSONObject(mapParams as Map<*, *>)
                LogUtil.e("test", "jsonParams : $jsonParams")

                mWebView.post {
                    mWebView.loadUrl("javascript:${callback}('${jsonParams}')")
                }
            }

            // key / value 저장
            "putAppData" -> {
                val key = CommonUtil.getStringJsonObject(json, "key")
                val value = CommonUtil.getStringJsonObject(json, "value")
                PreferenceUtil(mActivity).put(key, value)
            }

            // value 값 리턴
            "getAppData" -> {
                val key = CommonUtil.getStringJsonObject(json, "key")
                val value = PreferenceUtil(mActivity).getValue(key, "")
                val callback = CommonUtil.getStringJsonObject(json, "callback")
                mWebView.post {
                    mWebView.loadUrl("javascript:${callback}('${value}')")
                }
            }
        }
    }
}