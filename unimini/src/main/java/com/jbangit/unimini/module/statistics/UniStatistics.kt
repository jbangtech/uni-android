package com.jbangit.unimini.module.statistics

import android.content.Context

/**
 * 统计扩展
 */
interface UniStatistics {

    fun init(context: Context?)

    fun onPageStart(pageName: String)

    fun onPageEnd(pageName: String)

    fun onEventStart(eventId: String, label: String)

    fun onEventEnd(
        eventId: String?,
        label: String?,
        attributes: Map<String?, String?>? = null
    )

    fun onEventDuration(
        eventId: String,
        label: String,
        milliseconds: Long,
        attributes: Map<String?, String?>? = null
    )

    fun onEvent(
        eventId: String,
        label: String,
        acc: Int = 1, //次数
        attributes: Map<String?, String?>? = null
    )

}

