package com.kokasin

object Constants {
    // extra static 상수 정의
    object EXTRA {
        const val CALLBACK_TYPE = "callback_type"
        const val LINK_URL = "link_url"
        const val FUNCTION_NAME = "function_name"
        const val BACK_KEY_YN = "back_key_yn"
    }

    // URL 정의
    object URL {
        const val MAIN_URL: String = BuildConfig.serverUrl + "/"
    }

    // Action 정의
    object ACTION {
        const val ASSET_CHANGE_COMPLETE = "asset_change_complete"
        const val WEBVIEW_FINISHED = "webview_finished"
    }

}