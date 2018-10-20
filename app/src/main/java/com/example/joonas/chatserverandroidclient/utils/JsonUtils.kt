package com.example.joonas.chatserverandroidclient.utils

import org.json.JSONObject

object JsonUtils {
  fun makeMessageObject(type: String,
                        user: String = "",
                        message: String = "",
                        timeStamp: String = ""): JSONObject {
    return JSONObject()
            .put("timestamp", timeStamp)
            .put("message", message)
            .put("user", user)
            .put("type", type)
  }
}