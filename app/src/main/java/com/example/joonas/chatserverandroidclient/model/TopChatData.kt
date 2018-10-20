package com.example.joonas.chatserverandroidclient.model

import android.util.Log
import org.json.JSONArray
import java.util.*

private const val TAG = "TopChatData"

object TopChatData: Observable() {

  private var topChatJSONArray = JSONArray()

  fun setNewTopChatData(jsonArray: JSONArray) {
    topChatJSONArray = jsonArray
    //Log.d(TAG, topChatJSONArray.toString(2))
    setChanged()
    notifyObservers(topChatJSONArray)
  }

  fun getTopChatData(): JSONArray {
    return topChatJSONArray
  }

}