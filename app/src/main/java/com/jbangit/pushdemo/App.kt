package com.jbangit.pushdemo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.baidu.mobstat.StatService
import com.jbangit.pushdemo.baidu.BaiduStatistics
import com.jbangit.pushdemo.sp.SharedPreferencesHelper
import com.jbangit.pushdemo.umeng.UmInitConfig
import com.jbangit.pushdemo.umeng.UmInitConfig.Companion.APPKEY
import com.jbangit.pushdemo.umeng.UmInitConfig.Companion.APP_SECRET
import com.jbangit.pushdemo.umeng.UmengPush
import com.jbangit.unimini.JBUniMini
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.PushAgent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.e("TAG", "onCreate: app")

        initUmeng()
        //初始化uni
        JBUniMini.init(this) { builder ->
            uniPush = UmengPush() //设置推送拓展
            uniStatistics = BaiduStatistics()//设置百度统计拓展

            registerModule(UniUserModule::class) //注册uni扩展模块
        }

    }

    override fun attachBaseContext(base: Context?) {
        MultiDex.install(base)
        super.attachBaseContext(base)
    }

    fun initUmeng() {
        val sharedPreferencesHelper = SharedPreferencesHelper(this, "umeng")

        //设置LOG开关，默认为false

        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(true)
        //解决推送消息显示乱码的问题
        //解决推送消息显示乱码的问题
        PushAgent.setup(this, APPKEY, APP_SECRET)
        //友盟预初始化
        //友盟预初始化
        UMConfigure.preInit(applicationContext, APPKEY, "Umeng")

        //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
        /**
         * 打开app首次隐私协议授权，以及sdk初始化，判断逻辑请查看SplashTestActivity
         */
        //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
//        if (sharedPreferencesHelper.getSharedPreference("uminit", "").equals("1")) {
        //友盟正式初始化
        val umInitConfig = UmInitConfig()
        umInitConfig.UMinit(applicationContext)
        //QQ官方sdk授权
//            Tencent.setIsPermissionGranted(true)
//        }

    }
}