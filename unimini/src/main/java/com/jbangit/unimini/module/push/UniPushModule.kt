package com.jbangit.unimini.module.push

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.jbangit.unimini.JBUniMini
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule

class UniPushModule : UniModule() {

    val mPushAgent: UniPush? = JBUniMini.uniPush
    var context: Context? = null

    @UniJSMethod(uiThread = true)
    fun init() {
        if (context == null) {
            context = mWXSDKInstance.context
            mPushAgent?.init(context)
        }
    }

    @UniJSMethod(uiThread = true)
    fun setTags(tags: String, result: UniJSCallback? = null) {
        mPushAgent?.setTags(tags.split(",").toTypedArray(), Callback(result)::callBack)
    }

    @UniJSMethod(uiThread = true)
    fun addTags(tags: String, result: UniJSCallback? = null) {
        mPushAgent?.addTags(tags.split(",").toTypedArray(), Callback(result)::callBack)
    }

    @UniJSMethod(uiThread = true)
    fun deleteTags(tags: String, result: UniJSCallback? = null) {
        mPushAgent?.deleteTags(tags.split(",").toTypedArray(), Callback(result)::callBack)

    }

    @UniJSMethod(uiThread = true)
    fun deleteAllTags(result: UniJSCallback? = null) {
        mPushAgent?.deleteAllTags(Callback(result)::callBack)
    }

    @UniJSMethod(uiThread = true)
    fun setAlias(alias: String, aliasType: String?, result: UniJSCallback? = null) {
        mPushAgent?.setAlias(alias, aliasType, Callback(result)::callBack)
    }

    @UniJSMethod(uiThread = true)
    fun removeAlias(alias: String, aliasType: String?, result: UniJSCallback? = null) {
        mPushAgent?.deleteAlias(alias, aliasType, Callback(result)::callBack)
    }


    private class Callback(private val jsCallback: UniJSCallback? = null) {
        fun callBack(isSuccess: Boolean, msg: String) {
            val objects = JSONObject()
            objects["isSuccess"] = isSuccess
            objects["message"] = msg
            jsCallback?.invoke(objects)
        }
    }
}