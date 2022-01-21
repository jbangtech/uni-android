package com.jbangit.pushdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import io.dcloud.common.util.Md5Utils.ALGORITHM
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.reflect.KClass

fun Context.toPage(kClass: KClass<out Activity>, bundle: Bundle) {
    val intent = Intent(this, kClass.java)
    intent.putExtras(bundle)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun encryptByPublic(context: Context, content: String): String? {
    return try {
        val pubKey = getPublicKeyFromAssets(context) ?: ""
        val publicKey = getPublicKeyFromX509(pubKey)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        val plaintext = content.toByteArray();
        val output = cipher.doFinal(plaintext);
        val str = Base64.encodeToString(output, Base64.DEFAULT);
        str
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getPublicKeyFromX509(bysKey: String): PublicKey {
    val decodeKey = Base64.decode(bysKey, Base64.DEFAULT);
    val x509 = X509EncodedKeySpec(decodeKey);
    val keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(x509);
}

/**
 * 获取公钥
 */
private fun getPublicKeyFromAssets(context: Context): String? {
    try {
        val inputReader = InputStreamReader(context.resources.assets.open("rsa_public_key.pem"));
        val bufReader = BufferedReader(inputReader);
        var result = ""
        bufReader.lineSequence().iterator().forEach {
            if (it.startsWith("-")) {
                return@forEach
            }
            result += it
        }

        return result
    } catch (e: Exception) {
        e.printStackTrace();
        return null
    }
}
