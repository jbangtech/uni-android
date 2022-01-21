package com.jbangit.unimini

import android.content.Context
import android.net.Uri

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IDCUniMPAppSplashView
import org.json.JSONObject
import java.lang.Exception

import kotlin.reflect.KClass

/**
 * 打开uni界面
 * @param appId 可以是内置的uni的appId，也可以是在线的uni下载链接
 * @param splashKClass 启动页面，建议设置
 * @param redirectPath uni的路径，如果不设置则打开uni小程序首页
 * @param arguments 传递的参数
 */
@JvmOverloads
fun LifecycleOwner.toUniPage(
    appId: String? = null,
    splashKClass: KClass<out IDCUniMPAppSplashView>? = null,
    redirectPath: String? = null,
    arguments: JSONObject? = null
) {
    JBUniMini.toUniPage(this, appId, splashKClass, redirectPath, arguments)
}

/**
 * 缓存uni小程序
 * @param urls uni的下载链接
 */
fun LifecycleOwner.upgradleUni() {
    JBUniMini.upgradeCacheUni(this)
}


fun Context.showToast(toast:String,duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(this,toast,duration).show()
}