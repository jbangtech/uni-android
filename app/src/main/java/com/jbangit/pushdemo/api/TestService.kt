package com.jbangit.pushdemo.api

import com.jbangit.pushdemo.model.AliasAndTags
import com.jbangit.unimini.model.Result
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface TestService {

    @FormUrlEncoded
    @POST("user/api/debug/v1/pushAlias")
    fun push(
        @Field("alias") alias: String,
        @Field("message") message: String,
        @Field("targetId") targetId: String?,
        @Field("targetPath") targetPath: String?,
        @Field("targetType") targetType: String?
    ): Call<Result<Any>>

    @FormUrlEncoded
    @POST("user/api/debug/v1/pushTags")
    fun pushWithTag(
        @Field("tags") tags: String,
        @Field("message") message: String,
        @Field("targetId") targetId: String?,
        @Field("targetPath") targetPath: String?,
        @Field("targetType") targetType: String?
    ): Call<Result<Any>>

    @GET("user/api/im/push/v1/profile")
    fun getAliasAndTags():Call<Result<AliasAndTags>>

}