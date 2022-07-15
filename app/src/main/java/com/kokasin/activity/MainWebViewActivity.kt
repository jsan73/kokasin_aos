package com.kokasin.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kokasin.R
import com.kokasin.databinding.ActivityMainWebviewBinding
import com.kokasin.util.DomainUtil
import com.kokasin.util.LogUtil
import com.kokasin.util.PreferenceUtil
import java.util.*

class MainWebViewActivity :BaseWebViewActivity() {

    private lateinit var binding: ActivityMainWebviewBinding
    private var mIsFinish = false
    private val mExitTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_webview)

//        val intent = Intent(this, GPIntroActivity::class.java)
//        startActivity(intent)

        initUI()
        saveFcmToken()
        loadMain()  // 자동로그인 실행
//
//        // 백그라운드에서 푸시 알림 선택하여 실행했는지 체크 (바로 실행하면 안열림)
//        Handler(Looper.getMainLooper()).postDelayed({
//            checkPushRun(getIntent())
//        }, 2500)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkPushRun(intent)  // 앱 실행 중 푸시 알림 선택하여 실행 시
    }

    override fun onBackPressed() {
        if(binding.wvMain.url.equals(DomainUtil.mainUrl(this))) {  // 메인화면
            // 2초 동안 Back Key 를 두 번 연속 누르면 어플 종료
            if (!mIsFinish) {
                mExitTimer.schedule(object : TimerTask() {
                    override fun run() {
                        mIsFinish = false
                    }
                }, 2000)
                mIsFinish = true

                val message = "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.".replace(" ", "\u00A0")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                finish()
                ActivityCompat.finishAffinity(this)
            }
        }
        else if(binding.wvMain.url!!.contains("/login")) {  // 로그인화면
            finish();
        }
        else {
            if (binding.wvMain.canGoBack()) {
                binding.wvMain.goBack()
            } else {
                finish()
            }
        }
    }
    // 푸시 토큰 저장
    private fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                val token = task.result
                PreferenceUtil(this).put(PreferenceUtil.KEYS.PUSH_ID, token)
                LogUtil.e("test", "push token : $token")
            }
        })
    }
    private fun initUI() {
        setWebView(this, binding.wvMain)
        setJavascriptInterface(null)
    }
}