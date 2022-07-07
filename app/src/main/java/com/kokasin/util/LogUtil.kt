package com.kokasin.util

import android.text.TextUtils
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.kokasin.BuildConfig
import java.util.*

object LogUtil {

    private const val GLOBAL_TAG = "koksin"
    private const val LOG_MAX_LENGTH = 3000

    enum class LEVEL {
        VERBOSE(2, "V"),
        DEBUG(3, "D"),
        INFO(4, "I"),
        WARN(5, "W"),
        ERROR(6, "E"),
        ASSERT(7, "A");

        private var level: Int = 0;
        private var levelString: String = ""

        constructor(level: Int, levelString: String) {
            this.level = level
            this.levelString = levelString
        }
    }

    private fun getCurrentTag(tag: String?): String? {
        if (!TextUtils.isEmpty(tag)) {
            return GLOBAL_TAG
        }

        if (!TextUtils.isEmpty(GLOBAL_TAG)) {
            return GLOBAL_TAG
        }

        val stacks = Thread.currentThread().stackTrace
        return if (stacks.size >= 4) {
            stacks[3].className
        } else null
    }

    private fun toPrettyFormat(jsonString: String?): String? {
        try {
            val json = JsonParser.parseString(jsonString).asJsonObject
            val gson = GsonBuilder().setPrettyPrinting().create()
            return gson.toJson(json)

        } catch (e: Exception) {
            //e.printStackTrace()
        }

        return jsonString
    }

    private fun log2Console(level: LEVEL, tag: String, msg: String, thr: Throwable?) {
        when (level) {
            LEVEL.VERBOSE -> if (thr == null) {
                Log.v(tag, msg)
            } else {
                Log.v(tag, msg, thr)
            }
            LEVEL.DEBUG -> if (thr == null) {
                Log.d(tag, msg)
            } else {
                Log.d(tag, msg, thr)
            }
            LEVEL.INFO -> if (thr == null) {
                Log.i(tag, msg)
            } else {
                Log.i(tag, msg, thr)
            }
            LEVEL.WARN -> if (thr == null) {
                Log.w(tag, msg)
            } else if (TextUtils.isEmpty(msg)) {
                Log.w(tag, thr)
            } else {
                Log.w(tag, msg, thr)
            }
            LEVEL.ERROR -> if (thr == null) {
                Log.e(tag, msg)
            } else {
                Log.e(tag, msg, thr)
            }
            LEVEL.ASSERT -> if (thr == null) {
                Log.wtf(tag, msg)
            } else if (TextUtils.isEmpty(msg)) {
                Log.wtf(tag, thr)
            } else {
                Log.wtf(tag, msg, thr)
            }
        }
    }

    private fun log(level: LEVEL, tag: String?, msg: String?, tr: Throwable?) {
        var msg = msg
        if(BuildConfig.DEBUG) {
            val objThread = Thread.currentThread()
            val strThreadName = objThread.name
            val nLineNumber = objThread.stackTrace[4].lineNumber
            var strFileName = objThread.stackTrace[4].fileName

            if(strFileName.length > 20) {
                strFileName = strFileName.substring(0, 20)
            }

            val curTag = getCurrentTag(tag) + "." + strThreadName
            msg = toPrettyFormat(msg)

            if(msg!!.length > LOG_MAX_LENGTH) {
                val chunkCount = msg.length / LOG_MAX_LENGTH
                for(i in 0..chunkCount) {
                    var maxLen = (i + 1) * LOG_MAX_LENGTH
                    if(maxLen > msg.length) maxLen = msg.length
                    val strLog = String.format(Locale.US, "[%-20s:%5d] %02d - %s\n", strFileName, nLineNumber, i + 1, msg.substring(i * LOG_MAX_LENGTH, maxLen))
                    log2Console(level, curTag, strLog, tr)
                }
            }
            else {
                val strLog = String.format(Locale.US, "[%-20s:%5d] %s\n", strFileName, nLineNumber, msg)
                log2Console(level, curTag, strLog, tr)
            }
        }
    }

    // LogUtil.i
    fun i(message: String) {
        log(LEVEL.INFO, null, message, null)
    }

    fun i(tag: String, message: String?) {
        if(BuildConfig.DEBUG) {
            var message = message ?: "null"
            log(LEVEL.INFO, tag, message, null)
        }
    }

    // LogUtil.d
    fun d(message: String) {
        log(LEVEL.DEBUG, null, message, null)
    }

    fun d(tag: String, message: String?) {
        if(BuildConfig.DEBUG) {
            var message = message ?: "null"
            log(LEVEL.DEBUG, tag, message, null)
        }
    }

    // LogUtil.w
    fun w(message: String) {
        log(LEVEL.WARN, null, message, null)
    }

    fun w(tag: String, message: String?) {
        if(BuildConfig.DEBUG) {
            var message = message ?: "null"
            log(LEVEL.WARN, tag, message, null)
        }
    }

    // LogUtil.e
    public fun e(message: String) {
        log(LEVEL.ERROR, null, message, null)
    }

    public fun e(tag: String, message: String?) {
        if(BuildConfig.DEBUG) {
            var message = message ?: "null"
            log(LEVEL.ERROR, tag, message, null)
        }
    }

    // LogUtil.v
    fun v(message: String) {
        log(LEVEL.VERBOSE, null, message, null)
    }

    fun v(tag: String, message: String?) {
        if(BuildConfig.DEBUG) {
            var message = message ?: "null"
            log(LEVEL.VERBOSE, tag, message, null)
        }
    }
}