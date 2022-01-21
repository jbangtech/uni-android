package com.jbangit.unimini.network

import android.content.Context


class UniRepo(context: Context) {
    private val service = ApiManager.build(context,"https://edifier-forum.jbangit.com",UniService::class.java)

    fun getAppInfo(appId:String) = service.getUniMimi(appId)
}