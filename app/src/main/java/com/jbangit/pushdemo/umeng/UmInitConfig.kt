package com.jbangit.pushdemo.umeng

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.os.bundleOf
import com.umeng.commonsdk.UMConfigure
import com.jbangit.pushdemo.umeng.UmInitConfig
import com.umeng.analytics.MobclickAgent
import com.umeng.message.PushAgent
import com.umeng.message.MsgConstant
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage
import com.umeng.message.UTrack
import com.umeng.message.UmengNotificationClickHandler
import com.jbangit.pushdemo.model.NotifyData
import com.umeng.message.IUmengRegisterCallback
import com.jbangit.pushdemo.App
import com.jbangit.pushdemo.MainActivity
import com.jbangit.pushdemo.R
import com.jbangit.pushdemo.toPage

class UmInitConfig {
    private var handler: Handler? = null
    fun UMinit(context: Context) {

        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(
            context, APPKEY, "Umeng", UMConfigure.DEVICE_TYPE_PHONE,
            APP_SECRET
        )

        //集成umeng-crash-vx.x.x.aar，则需要关闭原有统计SDK异常捕获功能
        MobclickAgent.setCatchUncaughtExceptions(false)
        //PushSDK初始化(如使用推送SDK，必须调用此方法)
        initUpush(context)

        //统计SDK是否支持采集在子进程中打点的自定义事件，默认不支持
        UMConfigure.setProcessEvent(true) //支持多进程打点

        // 页面数据采集模式
        // setPageCollectionMode接口参数说明：
        // 1. MobclickAgent.PageMode.AUTO: 建议大多数用户使用本采集模式，SDK在此模式下自动采集Activity
        // 页面访问路径，开发者不需要针对每一个Activity在onResume/onPause函数中进行手动埋点。在此模式下，
        // 开发者如需针对Fragment、CustomView等自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/
        // MobclickAgent.onPageEnd手动埋点即可。此采集模式简化埋点工作，唯一缺点是在Android 4.0以下设备中
        // 统计不到Activity页面数据和各类基础指标(提示：目前Android 4.0以下设备市场占比已经极小)。

        // 2. MobclickAgent.PageMode.MANUAL：对于要求在Android 4.0以下设备中也能正常采集数据的App,可以使用
        // 本模式，开发者需要在每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，在Activity的
        // onPause函数中手动调用MobclickAgent.onPause接口。在此模式下，开发者如需针对Fragment、CustomView等
        // 自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/MobclickAgent.onPageEnd手动埋点即可。

        // 如下两种LEGACY模式不建议首次集成友盟统计SDK的新用户选用。
        // 如果您是友盟统计SDK的老用户，App需要从老版本统计SDK升级到8.0.0版本统计SDK，
        // 并且：您的App之前MobclickAgent.onResume/onPause接口埋点分散在所有Activity
        // 中，逐个删除修改工作量很大且易出错。
        // 若您的App符合以上特征，可以选用如下两种LEGACY模式，否则不建议继续使用LEGACY模式。
        // 简单来说，升级SDK的老用户，如果不需要手动统计页面路径，选用LEGACY_AUTO模式。
        // 如果需要手动统计页面路径，选用LEGACY_MANUAL模式。
        // 3. MobclickAgent.PageMode.LEGACY_AUTO: 本模式适合不需要对Fragment、CustomView
        // 等自定义页面进行页面访问统计的开发者，SDK仅对App中所有Activity进行页面统计，开发者需要在
        // 每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，在Activity的
        // onPause函数中手动调用MobclickAgent.onPause接口。此模式下MobclickAgent.onPageStart
        // ,MobclickAgent.onPageEnd这两个接口无效。

        // 4. MobclickAgent.PageMode.LEGACY_MANUAL: 本模式适合需要对Fragment、CustomView
        // 等自定义页面进行手动页面统计的开发者，开发者如需针对Fragment、CustomView等
        // 自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/MobclickAgent.onPageEnd
        // 手动埋点即可。开发者还需要在每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，
        // 在Activity的onPause函数中手动调用MobclickAgent.onPause接口。
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    /**
     * 主进程和子进程channel都需要进行初始化和注册
     */
    private fun initUpush(context: Context) {
        val pushAgent = PushAgent.getInstance(context)
        handler = Handler(Looper.getMainLooper())

        //sdk开启通知声音
        pushAgent.notificationPlaySound = MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE
        // sdk关闭通知声音
        // mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        // 通知声音由服务端控制
        // mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);

        // mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        // mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        val messageHandler: UmengMessageHandler = object : UmengMessageHandler() {
            /**
             * 通知的回调方法（通知送达时会回调）
             */
            override fun dealWithNotificationMessage(context: Context, msg: UMessage) {
                //调用super，会展示通知，不调用super，则不展示通知。
                super.dealWithNotificationMessage(context, msg)
            }

            /**
             * 自定义消息的回调方法
             */
            override fun dealWithCustomMessage(context: Context, msg: UMessage) {
                handler!!.post {
                    Log.e(TAG, "run: " + msg.custom + "::" + msg.title)
                    // TODO Auto-generated method stub
                    // 对自定义消息的处理方式，点击或者忽略
                    val isClickOrDismissed = true
                    if (isClickOrDismissed) {
                        //自定义消息的点击统计
                        UTrack.getInstance(context).trackMsgClick(msg)
                    } else {
                        //自定义消息的忽略统计
                        UTrack.getInstance(context).trackMsgDismissed(msg)
                    }
                    Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show()
                }
            }

            /**
             * 自定义通知栏样式的回调方法
             */
            override fun getNotification(context: Context, msg: UMessage): Notification {
                Log.e(
                    TAG,
                    "getNotification: " + msg.custom + "::" + msg.title + "" + msg.builder_id
                )
                val builder: Notification.Builder
                if (Build.VERSION.SDK_INT >= 26) {
                    if (!isChannelSet) {
                        isChannelSet = true
                        val chan = NotificationChannel(
                            PRIMARY_CHANNEL,
                            PushAgent.getInstance(context).notificationChannelName,
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        val manager = context.getSystemService(
                            Context.NOTIFICATION_SERVICE
                        ) as NotificationManager
                        manager?.createNotificationChannel(chan)
                    }
                    builder = Notification.Builder(context, PRIMARY_CHANNEL)
                } else {
                    builder = Notification.Builder(context)
                }
                val myNotificationView = RemoteViews(
                    context.packageName,
                    R.layout.notification_view
                )
                myNotificationView.setTextViewText(R.id.notification_title, msg.title)
                myNotificationView.setTextViewText(R.id.notification_text, msg.text)
                myNotificationView.setImageViewResource(
                    R.id.notification_large_icon,
                    getSmallIconId(context, msg)
                )
                myNotificationView.setImageViewResource(
                    R.id.notification_small_icon,
                    getSmallIconId(context, msg)
                )
                builder.setContent(myNotificationView)
                    .setSmallIcon(getSmallIconId(context, msg))
                    .setTicker(msg.ticker)
                    .setAutoCancel(true)
                return builder.notification
            }
        }
        pushAgent.messageHandler = messageHandler

        /*
         * 自定义行为的回调处理，参考文档：高级功能-通知的展示及提醒-自定义通知打开动作
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         */
        val notificationClickHandler: UmengNotificationClickHandler =
            object : UmengNotificationClickHandler() {
                override fun launchApp(context: Context, msg: UMessage) {
                    super.launchApp(context, msg)
                }

                override fun openUrl(context: Context, msg: UMessage) {
                    super.openUrl(context, msg)
                }

                override fun openActivity(context: Context, msg: UMessage) {
                    super.openActivity(context, msg)
                }

                override fun dealWithCustomAction(context: Context, msg: UMessage) {
                    Log.e(TAG, "dealWithCustomAction: ", )
                    val targetId = msg.extra["targetId"]
                    val targetPath = msg.extra["targetPath"]
                    val targetType = msg.extra["targetType"]
                    Log.e(TAG, "dealWithCustomAction: --------- $targetType -- $targetPath -- $targetId")
                    val data = NotifyData(targetId, targetPath, targetType)
                    context.toPage(MainActivity::class, bundleOf("notification" to data))
                }
            }
        //使用自定义的NotificationHandler
        pushAgent.notificationClickHandler = notificationClickHandler

        //注册推送服务 每次调用register都会回调该接口
        pushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {
                Log.i(TAG, "device token: $deviceToken")
                context.sendBroadcast(Intent(UPDATE_STATUS_ACTION))
            }

            override fun onFailure(s: String, s1: String) {
                Log.i(TAG, "register failed: $s $s1")
                context.sendBroadcast(Intent(UPDATE_STATUS_ACTION))
            }
        })

        //使用完全自定义处理
//        pushAgent.setPushIntentServiceClass(UmengNotificationService.class);

        //小米通道
        //MiPushRegistar.register(this, XIAOMI_ID, XIAOMI_KEY);
        //华为通道
        //HuaWeiRegister.register(this);
        //魅族通道
        //MeizuRegister.register(this, MEIZU_APPID, MEIZU_APPKEY);
    }

    companion object {
        private val TAG = App::class.java.name
        const val UPDATE_STATUS_ACTION = "com.umeng.message.example.action.UPDATE_STATUS"
        const val APPKEY = "61c3ed6c43849667ca912836"
        const val APP_SECRET = "d673b649c32100daaeaa624157f5d2aa"
    }
}