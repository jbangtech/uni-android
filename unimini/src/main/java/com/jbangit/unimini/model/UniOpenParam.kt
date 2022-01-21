package com.jbangit.unimini.model

import io.dcloud.feature.sdk.Interface.IDCUniMPAppSplashView
import org.json.JSONObject
import kotlin.reflect.KClass

data class UniOpenParam(
    val splashKClass: KClass<out IDCUniMPAppSplashView>?,
    val redirectPath: String?,
    val arguments: JSONObject?,
)