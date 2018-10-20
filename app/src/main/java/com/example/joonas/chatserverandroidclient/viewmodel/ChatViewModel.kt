package com.example.joonas.chatserverandroidclient.viewmodel

import android.databinding.ObservableField
import android.util.Log
import com.example.joonas.chatserverandroidclient.model.Messages
import java.util.*
import kotlin.concurrent.thread

class ChatViewModel : IViewModel, Observer {

  private val TAG = "ChatViewModel"

  val messages = ObservableField<String>()
  var messagesString = ""

  fun pushNewMessage(message: String) {
    messagesString += "$message\n"
    messages.set(messagesString)
  }

  override fun onCreate() {
    //messages.set("[USER] @12:36 - A test message from user")
  }

  override fun update(o: Observable?, arg: Any?) {
    Log.d(TAG, "pushing new message: $arg, ${arg.toString()}")
    pushNewMessage(arg.toString())
  }
}