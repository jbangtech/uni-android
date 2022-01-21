package com.jbangit.unimini.module.push

import android.content.Context

/**
 * 推送设置
 */
interface UniPush {
    /**
     * 初始化
     */
    fun init(context: Context?)

    /**
     * 添加tag
     */
    fun addTags(tags: Array<String>, callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null)

    /**
     * 设置tag
     */
    fun setTags(tags: Array<String>, callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null)

    /**
     * 删除tag
     */
    fun deleteTags(tags: Array<String>, callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null)
    fun deleteAllTags(callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null)

    /**
     * 设置别名
     */
    fun setAlias(
        alias: String,
        aliasType: String?,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null
    )

    /**
     * 删除别名
     */
    fun deleteAlias(
        alias: String,
        aliasType: String?,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)? = null
    )
}