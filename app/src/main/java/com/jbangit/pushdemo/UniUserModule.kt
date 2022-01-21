package com.jbangit.pushdemo

import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniModule

class UniUserModule : UniModule() {

    @UniJSMethod(uiThread = false)
    fun getUserId(): String {
        val context = mUniSDKInstance.context
        return encryptByPublic(context, userId)?:""
    }

    companion object {
        var userId = ""
    }

}