package com.jbangit.pushdemo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifyData(val targetId:String?,val targetPath:String?,val targetType:String?):Parcelable