package com.jbangit.unimini.module

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.jbangit.unimini.module.push.UniPushModule
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule

class TakePictureModule : UniModule() {
    var input: Uri? = null

    @UniJSMethod(uiThread = true)
    fun addTag(result: UniJSCallback) {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
    }
}