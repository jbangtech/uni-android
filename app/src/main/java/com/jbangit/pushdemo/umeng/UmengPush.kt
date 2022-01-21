package com.jbangit.pushdemo.umeng

import android.content.Context
import android.util.Log
import com.jbangit.unimini.module.push.UniPush
import com.umeng.message.PushAgent

class UmengPush : UniPush {
    private var mPushAgent: PushAgent? = null


    companion object {
        fun init(context: Context?): UmengPush {
            val push = UmengPush()
            push.init(context)
            return push
        }
    }

    override fun init(context: Context?) {
        if (mPushAgent == null) {
            mPushAgent = PushAgent.getInstance(context)
            mPushAgent?.onAppStart()
        }
    }

    override fun addTags(
        tags: Array<String>,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)?
    ) {
        mPushAgent?.tagManager?.addTags({ isSuccess, result ->
            callBack?.invoke(isSuccess, result.msg)
        }, *tags)

    }

    override fun setTags(
        tags: Array<String>,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)?
    ) {
        deleteAllTags { isSuccess, msg ->
            addTags(tags, callBack)
        }
        addTags(tags, callBack)
    }

    override fun deleteTags(
        tags: Array<String>,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)?
    ) {
        mPushAgent?.tagManager?.deleteTags({ isSuccess, result ->
            callBack?.invoke(isSuccess, result.msg)
        }, *tags)
    }

    override fun deleteAllTags(callBack: ((isSuccess: Boolean, msg: String) -> Unit)?) {
        mPushAgent?.tagManager?.getTags { b, mutableList ->
            deleteTags(*mutableList.toTypedArray(), callBack)
        }
    }

    override fun setAlias(
        alias: String,
        aliasType: String?,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)?
    ) {
        mPushAgent?.setAlias(alias, aliasType) { isSuccess, message ->
            callBack?.invoke(isSuccess, message)
        }
    }

    override fun deleteAlias(
        alias: String,
        aliasType: String?,
        callBack: ((isSuccess: Boolean, msg: String) -> Unit)?
    ) {
        mPushAgent?.deleteAlias(alias, aliasType) { isSuccess, message ->
            callBack?.invoke(isSuccess, message)
        }
    }
}