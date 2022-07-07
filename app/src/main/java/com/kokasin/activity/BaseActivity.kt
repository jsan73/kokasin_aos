package com.kokasin.activity

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonParser
import com.kokasin.BuildConfig
import com.kokasin.dialog.CommonDialog
import com.kokasin.util.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

open class BaseActivity: AppCompatActivity() {

    private var mPermissionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // API 23 이상
            CommonUtil.setStatusBarColor(this, Color.parseColor("#ffffff"), "Y")
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  // API 21 이상
            CommonUtil.setStatusBarColor(this, Color.parseColor("#000000"), "N")
        }
        // API 19, 20 호출하지 않음
    }

    override fun onResume() {
        super.onResume()
        checkTokenValidation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

    }

    // 알림 다이얼로그 (버튼 1개 짜리)
    fun openDialogAlert(title: String, message: String, button: String, cancelable: Boolean) {
        val dialog = CommonDialog(this)
        dialog.setDialog(title, message)
        dialog.setNegativeButton(button, View.OnClickListener { dialog.dismiss() })
        dialog.setCancelable(cancelable)
        dialog.show()
    }

    // 알림 다이얼로그 (버튼 2개 짜리)
    fun openDialogAlert(
        dialog: CommonDialog,
        title: String,
        message: String,
        cancelButton: String,
        confirmButton: String,
        cancelable: Boolean,
        cancelListener: View.OnClickListener?,
        okListener: View.OnClickListener?
    ) {
        dialog.setDialog(title, message)
        if (cancelListener != null) {
            dialog.setNegativeButton(cancelButton, cancelListener)
        }
        if (okListener != null) {
            dialog.setPositiveButton(confirmButton, okListener)
        }
        dialog.setCancelable(cancelable)
        dialog.show()
    }

    // 1일 1회 토큰 가져오기
    private fun checkTokenValidation() {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val currentDate = sdf.format(Date()).toInt()
        val checkedDate = PreferenceUtil(this).getValue(PreferenceUtil.KEYS.TOKEN_DATE, 0)  // 가져온 날짜

        if(checkedDate < currentDate) {
      //      requestGetToken()
        }
    }

    // 기기정보, 푸시 토큰 등록
    fun requestRegisterInfo() {
        val url = "${BuildConfig.apiUrl}/api/mobile/device/upd"
        val deviceId = CommonUtil.getDeviceId(this@BaseActivity)
        val appVersion = CommonUtil.getAppVersion(this@BaseActivity)
        val pushToken = PreferenceUtil(this@BaseActivity).getValue(
            PreferenceUtil.KEYS.PUSH_ID, "")
        val isPushEnable: Boolean = NotificationUtil.areNotificationsEnabled(
            this@BaseActivity)
        val pushYn = if(isPushEnable) { "Y" } else { "N" }

        // Base64 인코딩 필요
        val encDeviceId = Base64.encodeToString(deviceId.toByteArray(), Base64.NO_WRAP)
        val encPushToken = Base64.encodeToString(pushToken.toByteArray(), Base64.NO_WRAP)

        val jsonObject = JSONObject()
        jsonObject.put("osType", "A")
        jsonObject.put("dvceId", encDeviceId)
        jsonObject.put("appVer", appVersion)
        jsonObject.put("dvceName", Build.MODEL)
        jsonObject.put("osVer", Build.VERSION.RELEASE)
        jsonObject.put("pushYn", pushYn)
        jsonObject.put("pushId", encPushToken)

        LogUtil.e("requestRegisterInfo : ${url}")
        LogUtil.e("params : ${jsonObject}")

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url, jsonObject,
            Response.Listener { response ->
                LogUtil.e("test", "requestRegisterInfo => $response")
            },
            Response.ErrorListener { error ->
                LogUtil.e("test", "ErrorListener => $error")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                val token = PreferenceUtil(applicationContext).getValue(PreferenceUtil.KEYS.TOKEN, "")
                LogUtil.e("api token : ${token}")

                params["X-Auth-Token"] = token
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                return params
            }
        }
        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    // x-auth-token 요청
    fun requestGetToken() {
        val url = BuildConfig.apiUrl + "/api/guard/get/token"
        LogUtil.e("requestGetToken : $url")

        val request: StringRequest = object : StringRequest(
            Method.POST, url, Response.Listener { response ->
                LogUtil.e("requestGetToken => $response")

                val json = JsonParser.parseString(response).asJsonObject
                val status = json["status"].asString

                if (status.uppercase() == "SUCCESS") {
                    val data = json["data"].asJsonObject
                    val token = data["token"].asString
                    PreferenceUtil(this).put(PreferenceUtil.KEYS.TOKEN, token)  // 토큰 저장

                    val sdf = SimpleDateFormat("yyyyMMdd")
                    val currentDate = sdf.format(Date()).toInt()
                    PreferenceUtil(this).put(PreferenceUtil.KEYS.TOKEN_DATE, currentDate)  // 날짜 저장
                }
                else {
                    val dialog = CommonDialog(this)
                    val cancelListener = View.OnClickListener {
                        dialog.dismiss()
                    }
                    val okListener = View.OnClickListener {
                        dialog.dismiss()
                        requestGetToken()
                    }
                    openDialogAlert(dialog, "", "유효한 토큰을 가져오지 못했습니다.\n다시 시도하시겠습니까?",
                        "취소", "재시도", false, cancelListener, okListener)
                }
            },
            Response.ErrorListener { error ->
                LogUtil.e("test", "ErrorListener => " + "토큰 요청 실패 : ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                val ck = CookieManager.getInstance().getCookie(DomainUtil.serverUrl(this@BaseActivity))
                if(!TextUtils.isEmpty(ck)) {
                    params["Cookie"] = ck
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                }
                return params
            }
        }
        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }
}