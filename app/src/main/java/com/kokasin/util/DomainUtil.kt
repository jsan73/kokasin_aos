package com.kokasin.util

import android.content.Context
import android.text.TextUtils
import android.webkit.URLUtil
import com.kokasin.BuildConfig
import com.kokasin.Constants

object DomainUtil {
    fun serverUrl(context: Context): String {
        if(BuildConfig.DEBUG) {
            val domain = PreferenceUtil(context).getValue(PreferenceUtil.KEYS.DOMAIN_URL, "")
            if(!TextUtils.isEmpty(domain) && URLUtil.isValidUrl(domain)) {
                return domain
            }
        }
        return BuildConfig.serverUrl
    }

    fun loginUrl(context: Context): String {
        if(BuildConfig.DEBUG) {
            val domain = PreferenceUtil(context).getValue(PreferenceUtil.KEYS.DOMAIN_URL, "")
            if(!TextUtils.isEmpty(domain) && URLUtil.isValidUrl(domain)) {
                return "https://sso-dev.richnco.kr/account/login?returnUrl=$domain"
            }
        }
        return Constants.URL.LOGIN_URL
    }

    fun mainUrl(context: Context): String {
        if(BuildConfig.DEBUG) {
            val domain = PreferenceUtil(context).getValue(PreferenceUtil.KEYS.DOMAIN_URL, "")
            if(!TextUtils.isEmpty(domain) && URLUtil.isValidUrl(domain)) {
                return "$domain/main"
            }
        }
        return Constants.URL.MAIN_URL
    }
}