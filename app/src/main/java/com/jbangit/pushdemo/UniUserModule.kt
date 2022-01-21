package com.jbangit.pushdemo

import android.content.Context
import android.util.Log
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniModule

class UniUserModule : UniModule() {

    val spf by lazy {
        mUniSDKInstance.context.getSharedPreferences("uni", Context.MODE_PRIVATE)
    }


    @UniJSMethod(uiThread = false)
    fun getUserId(): String {
        val context = mUniSDKInstance.context
        val userId = spf.getString("userId", "")
        val token = encryptByPublic(context, userId ?: "") ?: ""
        Log.e("TAG", "getUserId: ---- $userId ---- ")
        return token
    }

}