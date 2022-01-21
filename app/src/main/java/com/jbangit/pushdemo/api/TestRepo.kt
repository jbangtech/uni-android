package com.jbangit.pushdemo.api

import android.content.Context
import com.jbangit.unimini.network.ApiManager
import com.jbangit.unimini.network.UniService

class TestRepo(context: Context) {
    private val service =
        ApiManager.build(context, "https://edifier-forum.jbangit.com", TestService::class.java)

    fun push(message: String, targetPath: String) =
        service.push("1", message, null, targetPath, null)

    fun push(tags:String,message: String, targetPath: String) =
        service.pushWithTag(tags, message, null, targetPath, null)

    fun getAliasAndTags() = service.getAliasAndTags()
}