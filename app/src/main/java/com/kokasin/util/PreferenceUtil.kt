package com.kokasin.util

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import kr.richnco.goodrichplannermobile.util.AES256Util

class PreferenceUtil(private val context: Context) {

    object KEYS {
        const val PREF_NAME = "settings"
        const val TOKEN = "token"  // 토큰만 암호화 (굿리치플래너에서 현재 미사용)
        const val TOKEN_DATE = "token_date"  // 토큰 저장한 날짜
        const val PUSH_ID = "push_id"
        const val LOGIN_ID = "login_id"
        const val DEBUGGING_YN = "debugging_yn"
        const val DOMAIN_URL = "domain_url"  // 서버에서 도메인 설정 (테스트용)
    }

    fun put(key: String, value:String) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        try {
            if(!TextUtils.isEmpty(value)) {
                if(key == KEYS.TOKEN) {
                    val encText = AES256Util().aesEncode(value)
                    editor.putString(key, encText)
                }
                else {
                    editor.putString(key, value)
                }
            }
            else {
                editor.putString(key, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            editor.putString(key, value)
        }

        editor.commit()
    }

    fun put(key: String, value: Boolean) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putBoolean(key, value)
        editor.commit()
    }

    fun put(key: String, value: Int) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putInt(key, value)
        editor.commit()
    }

    fun put(key: String, value: Long) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putLong(key, value)
        editor.commit()
    }

    fun getValue(key: String, dftValue: String): String {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val value = pref.getString(key, dftValue)

        return try {
            if(!TextUtils.isEmpty(value)) {
                if(key == KEYS.TOKEN) {
                    AES256Util().aesDecode(value!!)
                } else {
                    value!!
                }
            } else {
                value!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dftValue
        }
    }

    fun getValue(key: String, dftValue: Int): Int {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)

        return try {
            pref.getInt(key, dftValue)
        } catch (e: Exception) {
            dftValue
        }

    }

    fun getValue(key: String, dftValue: Boolean): Boolean {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)

        return try {
            pref.getBoolean(key, dftValue)
        } catch (e: Exception) {
            dftValue
        }

    }

    fun getValue(key: String, dftValue: Long): Long {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)

        return try {
            pref.getLong(key, dftValue)
        } catch (e: Exception) {
            dftValue
        }

    }

    fun remove(key: String) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()

        try {
            editor.remove(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        editor.commit()
    }

    /*fun put(key: String, values: ArrayList<String>) {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        val jArray = JSONArray()

        for(i in values.indices) {
            jArray.put(values[i])
        }
        if(values.isNotEmpty()) {
            editor.putString(key, jArray.toString())
        }
        else {
            editor.putString(key, "")
        }
        editor.apply()
    }

    fun getValue(key: String): ArrayList<String> {
        val pref = context.getSharedPreferences(KEYS.PREF_NAME, Activity.MODE_PRIVATE)
        val json = pref.getString(key, "")
        val list = ArrayList<String>()
        if(!TextUtils.isEmpty(json)) {
            try {
                val jArray = JSONArray(json)
                for(i in 0 until jArray.length()) {
                    val value = jArray.optString(i)
                    list.add(value)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return list
    }*/
}