package com.kokasin.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.kokasin.Constants
import com.kokasin.R
import com.kokasin.databinding.ActivityCommonWebviewBinding


/**
 * 메인 웹뷰에서 (또는 그 외) 추가로 열리는 웹뷰
 */
class CommonWebViewActivity : BaseWebViewActivity() {

    private lateinit var binding: ActivityCommonWebviewBinding
    private lateinit var linkUrl: String
    private lateinit var backKeyYn: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_common_webview)
        linkUrl = intent.getStringExtra(Constants.EXTRA.LINK_URL).toString()
        backKeyYn = intent.getStringExtra(Constants.EXTRA.BACK_KEY_YN).toString()

        initUI()
    }

    override fun onBackPressed() {
        if(TextUtils.isEmpty(backKeyYn) || backKeyYn == "Y") {
            if(binding.wvMain.canGoBack()) {
                binding.wvMain.goBack()
            }
            else {
                finish()
            }
        }
        else if(backKeyYn == "N") {
            // 백키 동작 없음
        }
    }

    private fun initUI() {
        setWebView(this, binding.wvMain)
        setJavascriptInterface(null)
        loadUrl(linkUrl)
    }
}