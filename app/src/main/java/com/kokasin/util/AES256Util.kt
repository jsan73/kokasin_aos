package kr.richnco.goodrichplannermobile.util

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES256Util @Throws(UnsupportedEncodingException::class) constructor() {

    init {
        System.loadLibrary("native-lib")
        AES256Util()
    }

    private external fun getCryptoKey(): String

    private var iv: String? = null
    private var keySpec: Key? = null

    @Throws(UnsupportedEncodingException::class)
    fun AES256Util() {
        val secretKey = getCryptoKey()
        iv = secretKey.substring(0, 16)
        val keyBytes = ByteArray(16)
        val b = secretKey.toByteArray(charset("UTF-8"))
        var len = b.size
        if (len > keyBytes.size) len = keyBytes.size
        System.arraycopy(b, 0, keyBytes, 0, len)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        this.keySpec = keySpec
    }

    // 암호화
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun aesEncode(str: String): String? {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv!!.toByteArray()))
        val encrypted = c.doFinal(str.toByteArray(charset("UTF-8")))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    //복호화
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun aesDecode(str: String): String {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv!!.toByteArray(charset("UTF-8"))))
        val byteStr = Base64.decode(str.toByteArray(), Base64.NO_WRAP)
        return String(c.doFinal(byteStr), Charset.forName("UTF-8"))
    }
}