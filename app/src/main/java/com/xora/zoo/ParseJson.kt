package com.xora.zoo

import org.json.JSONObject

class ParseJson(json: String) : JSONObject(json) {
    val assets = this.optJSONArray("assets")
        ?.let {
            0.until(it.length()).map { i ->
                it.optJSONObject(i) // Returns an array of JSONObjects
            }
        }
}

class Asset(json: String) : JSONObject(json) {
    val name = this.optString("name")
    val emoji_int = this.optInt("emoji_int")
    val asset_url = this.optString("asset_url")
}