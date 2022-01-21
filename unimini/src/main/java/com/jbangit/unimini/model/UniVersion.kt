package com.jbangit.unimini.model

import org.json.JSONObject

data class UniVersion(val name: String, val code: Int) {
    constructor(jsonObject: JSONObject?) : this(
        jsonObject?.get("name")?.toString() ?: "",
        jsonObject?.get("code")?.toString()?.toInt() ?: 0
    )
}