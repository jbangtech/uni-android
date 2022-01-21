package com.jbangit.unimini

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.jbangit.unimini.model.UniMiniInfo
import com.jbangit.unimini.model.UniOpenParam
import com.jbangit.unimini.module.push.UniPush
import com.jbangit.unimini.module.statistics.UniStatistics
import com.jbangit.unimini.module.statistics.UniStatisticsModule
import com.jbangit.unimini.network.UniRepo
import com.jbangit.unimini.model.Result
import com.jbangit.unimini.model.UniVersion
import com.jbangit.unimini.module.push.UniPushModule
import io.dcloud.feature.barcode2.BarcodeProxy.context
import io.dcloud.feature.sdk.DCSDKInitConfig
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IDCUniMPAppSplashView
import io.dcloud.feature.sdk.Interface.IUniMP
import io.dcloud.feature.uniapp.UniSDKEngine
import io.dcloud.feature.uniapp.common.UniModule
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.ref.SoftReference
import kotlin.reflect.KClass


object JBUniMini {
    //缓存打开该uni需要的一些参数,first:uniAppId,second:params
    private val cacheUniParam = mutableMapOf<String, UniOpenParam>()

    //当前是否正在缓存某个uni
    private val cacheIng = mutableListOf<String>()

    //当在缓存中，可以将某些要打开的uni进行等候
    private val shouldOpen = mutableListOf<String>()

    private var softReference: SoftReference<Context>? = null

    private val currentUni = mutableMapOf<String,IUniMP>()

    /**
     * 可配置升级提示弹框
     */
    var loadingDialog: AlertDialog? = null

    /**
     * 设置统计扩展
     */
    var uniStatistics: UniStatistics? = null

    /**
     * 设置推送
     */
    var uniPush: UniPush? = null

    /**
     * uni缓存目录,缓存在../Android/data/包名/cache/files/uni_mini/
     */
    var uniCacheDir: String = "uni_mini"


    /**
     * 初始化uni
     * @param context 上下文
     * @param unRegisterDefModule 是否取消注册默认的模块（默认模块包括应用事件统计和推送注册），true 则取消默认注册
     * @param configBuild uni配置设置
     */
    fun init(
        context: Context,
        unRegisterDefModule: Boolean = false,
        configBuild: (JBUniMini.(DCSDKInitConfig.Builder) -> Unit)? = null
    ) {
        //初始化 uni小程序SDK ----start----------
        softReference = SoftReference(context)
        val builder = DCSDKInitConfig.Builder()
            .setCapsule(true)
            .setNJS(true)
            .setMenuDefFontSize("16px")
            .setMenuDefFontColor("#ff00ff")
            .setMenuDefFontWeight("normal")
            .setEnableBackground(true) //开启后台运行
        configBuild?.invoke(this, builder)
        val config = builder.build()
        DCUniMPSDK.getInstance().initialize(context, config) {
        }
        if (!unRegisterDefModule) {
            registerModule(UniStatisticsModule::class)
            registerModule(UniPushModule::class)
        }
        //初始化 uni小程序SDK ----end----------
    }

    /**
     * 注册uni扩展
     */
    fun registerModule(kClass: KClass<out UniModule>) {
        UniSDKEngine.registerModule(kClass.simpleName, kClass.java)
    }

    /**
     * 打开uni界面,如果[appIdOrUrl]为在线的uni下载链接，那么会先检查当前app中是否包含该uni，如果包含，那么直接打开，否则将去下载
     * 而如果[appIdOrUrl]是uni的appId，那么将会去寻找是否存在该内置uni，如果不存在，则会调用api获取该uni的版本，
     * 和本地缓存的uni版本进行对比（如果本地不存在缓存的uni相当于本地存在缓存的uni但是版本最低）
     * 当本地缓存的uni的版本低于线上的，那么将会去下载。否则将直接打开本地缓存的uni。当下载完成时，将直接打开
     * @param appIdOrUrl 可以是内置的uni的appId，也可以是在线的uni下载链接
     * @param splashKClass 启动页面，建议设置
     * @param redirectPath uni的路径，如果不设置则打开uni小程序首页
     * @param arguments 传递的参数
     */
    fun toUniPage(
        owner: LifecycleOwner,
        appIdOrUrl: String? = null,
        splashKClass: KClass<out IDCUniMPAppSplashView>? = null,
        redirectPath: String? = null,
        arguments: JSONObject? = null
    ) {
        val find = owner.getContext()?.assets?.list("apps")?.find { it == appIdOrUrl }
        if (find.isNullOrEmpty()) {//在内置库中找不到
            if (appIdOrUrl.isNullOrEmpty()) {
                Log.e("TAG", "toUniPage: 缺少Uni appId和线上Uni路径，无法正常跳转")
                return
            }
            val _appId = getUniAppId(appIdOrUrl)
            cacheUniParam.put(_appId, UniOpenParam(splashKClass, redirectPath, arguments))
            checkUni(owner, appIdOrUrl, _appId)
        } else {
            DCUniMPSDK.getInstance()
                .openUniMP(
                    owner.getContext(),
                    appIdOrUrl,
                    splashKClass?.java,
                    redirectPath,
                    arguments
                )
        }
    }

    /**
     * 检查该uni是否缓存过
     */
    private fun checkUni(owner: LifecycleOwner, appIdOrUrl: String, appId: String) {
//        val path = owner.getContext()?.getCacheFilePath(uniCacheDir, "${appId}.wgt")
//        when {
//            path.isNullOrEmpty() -> { //在本地中找不到需要打开的小程序，那么就需要去下载
        //如果是网址，那么直接下载
        if (appIdOrUrl.startsWith("http")) {
            remoteUni(owner, appIdOrUrl, true)
        } else { //否则先触发api，检查当前uni是否是最新的，如果不是，则下载。
            requestUni(owner, appId, isOpen = true, isShowDialog = true)
        }
//            }
//            else -> owner.getContext()?.releaseWgt(appIdOrUrl, path)
//        }
    }

    /**
     * 获取远程的uni
     */
    private fun remoteUni(owner: LifecycleOwner, url: String, isOpen: Boolean) {
        if (cacheIng.contains(url)) {
            shouldOpen.add(url)
        } else {
            cacheUni(owner, isOpen, url)
        }
    }

    /**
     * 请求api，获取uni的下载地址
     */
    private fun requestUni(
        owner: LifecycleOwner,
        appId: String,
        isOpen: Boolean = false,
        isShowDialog: Boolean = false
    ) {
        owner.getContext()?.apply {
            val callback: Callback<Result<UniMiniInfo>> = object : Callback<Result<UniMiniInfo>> {
                override fun onResponse(
                    call: Call<Result<UniMiniInfo>>,
                    response: Response<Result<UniMiniInfo>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.apply {
                            if (code == 0) {
                                data?.apply {
                                    val (_, code) = getUniVersion(appId)
                                    uniDownloadUrl?.let {
                                        if (code < build) {

                                            if (loadingDialog == null) {
                                                loadingDialog = getDefaultDialog(owner.getContext())
                                            }
                                            if (isShowDialog) {
                                                loadingDialog?.show()
                                            }
                                            currentUni[appId]?.closeUniMP()
                                            remoteUni(owner, it, isOpen)
                                        } else {
                                            val path = owner.getContext()
                                                ?.getCacheFilePath(uniCacheDir, "${appId}.wgt")
                                            releaseWgt(it, path, isOpen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<Result<UniMiniInfo>>, t: Throwable) {
                    t.printStackTrace()
                }

            }
            UniRepo(this).getAppInfo(appId).enqueue(callback)
        }
    }

    /**
     * 获取uni小程序的版本信息
     */
    fun getUniVersion(appId: String): UniVersion {
        val jsonObject: JSONObject? = DCUniMPSDK.getInstance().getAppVersionInfo(appId)
        return UniVersion(jsonObject)
    }

    /**
     * 更新缓存的uni
     * 会遍历缓存的目录，将需要升级的uni进行升级
     */
    fun upgradeCacheUni(owner: LifecycleOwner) {
        owner.getContext()?.let { FileCache.init(it) }
        val file = FileCache.getCacheDir(uniCacheDir)
        if (file.isDirectory) {
            file.list().forEach {
                val (appId, _) = it.split(".")
                requestUni(owner, appId)
            }
        }
    }

    /**
     * 缓存uni
     */
    private fun cacheUni(owner: LifecycleOwner, isOpen: Boolean = false, vararg urls: String) {
        val context = if (owner is Fragment) context else owner as? Context
        urls.forEach { url ->
            val _appId = getUniAppId(url)
            cacheIng.add(url)
            context?.fileCache(url, uniCacheDir, "${_appId}.wgt")?.observe(owner) {
                cacheIng.remove(url)
                val shouldOpen = shouldOpen.remove(url)
                context.releaseWgt(url, it, isOpen || shouldOpen)
            }
        }
    }

    /**
     * 获取uni的appId
     */
    private fun getUniAppId(url: String): String {
        val name = Uri.parse(url).lastPathSegment ?: ""
        val (_appId, _) = name.split(".")
        return _appId
    }


    private fun Context.releaseWgt(url: String, path: String?, isOpen: Boolean = false) {
        val appId = getUniAppId(url)
        val param: UniOpenParam? = cacheUniParam.remove(appId)
        DCUniMPSDK.getInstance().releaseWgtToRunPathFromPath(appId, path) { code, pArgs ->
            loadingDialog?.hide()
            if (code == 1) {
                if (isOpen) {
                    try {
                        val uni = DCUniMPSDK.getInstance()
                            .openUniMP(
                                this,
                                appId,
                                param?.splashKClass?.java,
                                param?.redirectPath,
                                param?.arguments
                            )
                        currentUni[appId] = uni
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getDefaultDialog(context: Context?): AlertDialog {
        return ProgressDialog(context).apply {
            setTitle("uni updating");
            setMessage("uni is downloading...");
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
    }

    private fun LifecycleOwner.getContext(): Context? {
        return if (this is Fragment) context else this as? Context
    }
}