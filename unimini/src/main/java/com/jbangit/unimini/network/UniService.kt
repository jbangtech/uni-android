package com.jbangit.unimini.network

import com.jbangit.unimini.model.UniMiniInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.jbangit.unimini.model.Result

interface UniService {

    @GET("user/api/app/v1/info")
    fun getUniMimi(@Query("appId")appId:String): Call<Result<UniMiniInfo>>
}