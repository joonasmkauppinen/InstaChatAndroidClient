package com.example.joonas.chatserverandroidclient.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object Messages : Observable() {

  private var messagesJSON = JSONArray()

  fun getMessagesJSONArray(): JSONArray {
    return messagesJSON
  }

  fun addMessageWithoutNotify(messageObject: JSONObject) {
    messagesJSON.put(messageObject)
  }

  fun addMessage(messageObject: JSONObject) {
    messagesJSON.put(messageObject)
    setChanged()
    notifyObservers(messageObject)
  }

  fun clearMessages() {
    messagesJSON = JSONArray()
  }

}