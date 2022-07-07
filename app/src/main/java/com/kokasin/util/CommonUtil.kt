package com.kokasin.util

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.Secure
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.JsonObject
import com.kokasin.Constants
import com.kokasin.R
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection
import java.util.*


object CommonUtil {

    // 인터넷 연결 여부 체크
    fun isNetWorkConnected(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = manager?.activeNetworkInfo
        return activeNetwork != null
    }

    // 스토어로 이동
    fun moveToPlayStore(context: Context, pkgName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$pkgName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // 상태바 색상 설정
    fun setStatusBarColor(activity: Activity, color: Int, lightStatusBar: String) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // API 21 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
        }

        // 상태바 폰트 컬러(API 23 이상에서 동작), Soft Key 영역 숨기기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // API 23 이상
            val isLight = lightStatusBar != "N"
            val lFlags: Int = activity.window.decorView.systemUiVisibility
            activity.window.decorView.systemUiVisibility = if (isLight) {
                //lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                //lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    // JSON에서 키에 해당 하는 값 리턴
    fun getStringJsonObject(json: JsonObject, key: String): String {
        var value = ""
        try {
            if (json[key] != null && !json[key].isJsonNull) {
                value = json[key].asString
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
        return value
    }

    // JSON 객체 리턴
    fun getJsonObject(json: JsonObject, key: String): JsonObject? {
        var value: JsonObject? = null
        try {
            if (!json[key].isJsonNull) {
                value = json[key].asJsonObject
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
        return value
    }

    // 현재 앱 버전 리턴
    fun getAppVersion(context: Context): String {
        var version = ""
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            version = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }

    // 앱 설치 여부 리턴
    fun isInstalledPkg(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages) {
            if (packageName == packageInfo.packageName) {
                return true
            }
        }
        return false
    }

    // 외부 앱 실행
    fun runExternalApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if(intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // 구글 플레이 앱으로 이동
    fun linkGooglePlay(context: Context, packageName: String) {
        val linkUrl = "market://details?id=${packageName}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(linkUrl)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // 이미지를 임시 다운로드 하여 가져옴 (명함 MMS 발송 시 사용)
    fun getImageFile(context: Context, bitmap: Bitmap, url: String): File? {
        try {
            if (url.toLowerCase().endsWith(".png")) {
                val file = File(context.cacheDir.path + "/temp.png")
                file.createNewFile()
                val os = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()
                return file
            } else if (url.toLowerCase().endsWith(".jpg")) {
                val file = File(context.cacheDir.path + "/temp.jpg")
                file.createNewFile()
                val os = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
                return file
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    // 공유할 앱 필터링
    fun getShareIntent(
        context: Context,
        nameArray: ArrayList<String>,
        subject: String?,
        text: String?
    ): Intent? {
        var found = false
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        // gets the list of intents that can be loaded.
        val resInfos: List<ResolveInfo> = context.getPackageManager().queryIntentActivities(
            intent,
            0
        )
        if (resInfos == null || resInfos.isEmpty()) return null
        for (info in resInfos) {
            for (i in nameArray.indices) {
                if (info.activityInfo.packageName.toLowerCase().contains(nameArray[i]!!) ||
                    info.activityInfo.name.toLowerCase().contains(nameArray[i]!!)
                ) {
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    intent.putExtra(Intent.EXTRA_TEXT, text)
                    intent.setPackage(info.activityInfo.packageName)
                    found = true
                    break
                }
            }
        }
        return if (found) intent else null
    }



    // 퍼미션 체크
    fun hasPermission(ctx: Context, permissionName: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 안드로이드 버전 체크. 마시멜로우 이상 true, 아니면 false
            val permissionResult = ctx.checkSelfPermission(permissionName!!) // 해당 퍼미션 체크.
            return permissionResult != PackageManager.PERMISSION_DENIED
        }
        return true
    }

    // 디바이스 아이디 리턴 (ANDROID_ID)
    fun getDeviceId(context: Context): String {
        val androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID)
        val uuid = UUID.nameUUIDFromBytes(androidId.toByteArray(charset("UTF-8")))
        return uuid.toString()
    }

    // 푸시 아이디 리턴
    fun getPushId(context: Context): String {
        return PreferenceUtil(context).getValue(PreferenceUtil.KEYS.PUSH_ID, "")
    }

    // 암호화 하여 리턴
    fun getEndData(text: String): String {
        val s = SeedCBC()
        val retMsg: String = s.SetConfig(Constants.KEY.SEED_KEY_IV, Constants.KEY.SEED_KEY_MK)
        return if (retMsg == "OK") {
            val bEncodedData = Base64.encode(
                s.Encryption(text.toByteArray()),
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_CLOSE or Base64.NO_WRAP
            )
            val encData = String(bEncodedData)
            encData.trim { it <= ' ' }
        } else {
            ""
        }
    }

    // 파일의 MIME 타입 리턴
    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr: ContentResolver = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase()
            )
        }
        return mimeType
    }

    // 키보드 내림
    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 앱 내 다운로드 폴더에 파일 생성
    fun createFile(context: Context, data: ByteArray, fileName: String): Boolean {
        LogUtil.d(">> createFile - fileName : $fileName")
        return try {
            val filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            val file = File(filePath, fileName)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data)

            if(fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("png")
                || fileName.endsWith("gif") || fileName.endsWith("bmp")) {
                setMediaStoreFile(context, file, fileName)
            }

            true

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    // Media DB에 등록하여 Public 으로 변경 (갤러리 같은 다른 앱에서 접근 가능)
    private fun setMediaStoreFile(context: Context, file: File, fileName: String) {
        val values = ContentValues()
        with(values) {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.DATA, file.absolutePath)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}