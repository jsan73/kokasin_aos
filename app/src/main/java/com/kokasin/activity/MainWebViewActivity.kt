package com.kokasin.activity

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kokasin.R
import com.kokasin.databinding.ActivityMainWebviewBinding
import com.kokasin.util.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainWebViewActivity :BaseWebViewActivity() {

    private lateinit var binding: ActivityMainWebviewBinding
    private var mIsFinish = false
    private val mExitTimer = Timer()
    private val MY_PERMISSION_ACCESS_ALL = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_webview)

//        val intent = Intent(this, IntroActivity::class.java)
//        startActivity(intent)

        initUI()
        saveFcmToken()
        loadMain()

        // 백그라운드에서 푸시 알림 선택하여 실행했는지 체크 (바로 실행하면 안열림)
        Handler(Looper.getMainLooper()).postDelayed({
            checkPushRun(getIntent())
        }, 2500)



    }

    override fun onStart() {
        super.onStart()
        checkPermission()
        EventBus.getDefault().register(this); // 이벤트 버스 등록
    }

    @Subscribe(threadMode = ThreadMode.MAIN) // SubScribe 어노테이션 등록
    fun MessageEvent(event: CallEvent) {
        // Push 메시지를 받을 때 마다 이 콜백 메소드가 호출 됨 (이벤트 버스 사용)
        Toast.makeText(this, "위치 전송 됨", Toast.LENGTH_SHORT).show()
        reloadMap()
    }
    override fun onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this); // 이벤트 버스 해제
    }

    // 현재 위치 접속권한
    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        val perFine = CommonUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val perCoarse = CommonUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(perFine && perCoarse) {

        }else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION),MY_PERMISSION_ACCESS_ALL)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === MY_PERMISSION_ACCESS_ALL) {
            if (grantResults.size > 0) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) System.exit(0)
                }
            }
        }
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
    // 푸시 토큰 저장 (웹서버에 저장이 필요함)
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