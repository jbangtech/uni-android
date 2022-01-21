package com.jbangit.unimini.module.statistics

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.jbangit.unimini.JBUniMini
import io.dcloud.feature.uniapp.common.UniModule

import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback


class UniStatisticsModule : UniModule() {

    val statistics: UniStatistics? = JBUniMini.uniStatistics
    var context: Context? = null

    @UniJSMethod(uiThread = false)
    fun init() {
        if (context == null) {
            context = mWXSDKInstance.context
            statistics?.init(context)
        }
    }


    @UniJSMethod(uiThread = false)
    fun onPageStart(pageName: String) {
        statistics?.onPageStart(pageName)
    }

    @UniJSMethod(uiThread = false)
    fun onPageEnd(pageName: String) {
        statistics?.onPageEnd(pageName)
    }

    @UniJSMethod(uiThread = false)
    fun onEvent(eventId: String, label: String, acc: Int, attributes: Map<String?, String?>?) {
        statistics?.onEvent(eventId, label, acc, attributes)
    }


    @UniJSMethod(uiThread = false)
    fun onEventStart(eventId: String, label: String) {
        statistics?.onEventStart(eventId, label)
    }

    @UniJSMethod(uiThread = false)
    fun onEventEnd(eventId: String, label: String, attributes: Map<String?, String?>?) {
        statistics?.onEventEnd(eventId, label, attributes)
    }


    @UniJSMethod(uiThread = false)
    fun onEventDuration(
        eventId: String,
        label: String,
        milliseconds: Long,
        attributes: Map<String?, String?>?
    ) {
        statistics?.onEventDuration(eventId, label, milliseconds, attributes)
    }
}