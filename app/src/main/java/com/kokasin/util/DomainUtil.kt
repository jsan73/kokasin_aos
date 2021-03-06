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

    fun mainUrl(context: Context): String {
        if(BuildConfig.DEBUG) {
            val domain = PreferenceUtil(context).getValue(PreferenceUtil.KEYS.DOMAIN_URL, "")
            if(!TextUtils.isEmpty(domain) && URLUtil.isValidUrl(domain)) {
                return "$domain"
            }
        }
        return Constants.URL.MAIN_URL
    }
}