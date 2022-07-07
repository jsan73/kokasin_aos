package com.kokasin

object Constants {

    object Notification {
        const val CHANNEL_ID = "kokasin_channel_01"
    }

    // extra static 상수 정의
    object EXTRA {
        const val CALLBACK_TYPE = "callback_type"
        const val LINK_URL = "link_url"
        const val FUNCTION_NAME = "function_name"
        const val FILE_PATH = "file_path"
        const val BACK_KEY_YN = "back_key_yn"

        const val ASSET_NO_LIST = "asset_no_list"
        const val URL = "url"
        const val USER_NAME = "user_name"
        const val ASSET_LIST = "asset_list"
    }

    // URL 정의
    object URL {
        const val MAIN_URL: String = BuildConfig.serverUrl + "/"
        const val LOGIN_URL: String = BuildConfig.loginUrl
        const val API_URL: String = BuildConfig.serverUrl + "/asetadduser"
    }

    // Action 정의
    object ACTION {
        const val FILE_OPEN = "file_open"
        const val ASSET_CHANGE_COMPLETE = "asset_change_complete"
        const val WEBVIEW_FINISHED = "webview_finished"
    }

    // 암호화 키
    object KEY {
        // 암호화 SEED (서버와 동일)
        const val SEED_KEY_IV = "{0xb6,0xac,0xba,0xe2,0xbb,0xbe,0xd2,0x9b,0x00,0x5d,0x99,0xf3,0x9d,0xd7,0xbc,0xb6}"
        const val SEED_KEY_MK = "{0xcb,0x7a,0xd1,0x04,0x1c,0x99,0x06,0x71,0xc3,0x9a,0x0b,0x80,0xcc,0xa6,0xae,0xd2}"

        // 버전체크 API 호출 시 고정 토큰
        const val VERSION_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyaW9uIiwidXNlck5vIjowLCJ1c2VyTm0iOiJyaW9uIiwibG9naW5JZCI6InJpb24iLCJjbG50Tm8iOjEwMDAwMCwib2duaU5vIjowLCJyb2xlcyI6WyJST0xFX0FDQ0VTUyJdLCJlbXBObyI6MCwiaWF0IjoyNTM0MDIyNjgzOTksImV4cCI6MjUzNDAyMjY4Mzk5fQ.b0Rsi34SDvWS5EJqA0QDCtDHxfjab45giSkWKIjFyizwFtjePkVM06GlExm6Vtp9iRN0hAUk_LBHB_5t8NFdiA"
    }

    // 푸시 수신 키
    object PUSH {
        const val PUSH_URL = "pushUrl"
    }
}