package com.jbangit.pushdemo.baidu

import android.content.Context
import android.util.StateSet
import com.baidu.mobstat.StatService
import com.jbangit.unimini.module.statistics.UniStatistics

class BaiduStatistics : UniStatistics {
    var context: Context? = null

    override fun init(context: Context?) {
        this.context = context
        StatService.start(context)
    }

    override fun onPageStart(pageName: String) {
        StatService.onPageStart(context, pageName)
    }

    override fun onPageEnd(pageName: String) {
        StatService.onPageEnd(context, pageName)

    }

    override fun onEventStart(eventId: String, label: String) {
        StatService.onEventStart(context, eventId, label)
    }

    override fun onEventEnd(eventId: String?, label: String?, attributes: Map<String?, String?>?) {
        StatService.onEventEnd(context, eventId, label, attributes)
    }

    override fun onEventDuration(
        eventId: String,
        label: String,
        milliseconds: Long,
        attributes: Map<String?, String?>?
    ) {
        StatService.onEventDuration(context, eventId, label, milliseconds, attributes)
    }

    override fun onEvent(
        eventId: String,
        label: String,
        acc: Int,
        attributes: Map<String?, String?>?
    ) {
        StatService.onEvent(context, eventId, label, acc, attributes)
    }
}