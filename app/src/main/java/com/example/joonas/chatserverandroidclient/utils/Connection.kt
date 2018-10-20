package com.example.joonas.chatserverandroidclient.utils

import java.io.*
import java.net.Socket
import java.nio.charset.Charset
import android.util.Log
import android.content.SharedPreferences
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.contract.FromServerToChatActivity
import com.example.joonas.chatserverandroidclient.model.App
import com.example.joonas.chatserverandroidclient.model.Messages
import com.example.joonas.chatserverandroidclient.contract.FromServerToLoginActivity
import com.example.joonas.chatserverandroidclient.model.TopChatData
import com.example.joonas.chatserverandroidclient.utils.JsonUtils.makeMessageObject
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetSocketAddress
import kotlin.concurrent.thread

private const val TAG = "Connection"
private const val port = 9999

object Connection {

  private var isConnected = false
  private var userExitAction = false

  private lateinit var socket: Socket
  private lateinit var input:  BufferedReader
  private lateinit var output: OutputStream

  private lateinit var preferences: SharedPreferences
  private lateinit var chatActivity: FromServerToChatActivity

  fun connect(activity: FromServerToLoginActivity) {

    try {

      createConnection()

      // Writes to server ":android 'username'". This
      // allows server to identify client as an android.
      writeToServer(
              makeMessageObject(
                      type = "command",
                      message = ":android ${preferences.getString(App.resourses?.getString(R.string.pref_user), "")}"
              ).toString()
      )

      // Login loop. Keeps prompting user for invalid username until
      // valid username is given. Following login attempts are
      // issued from LoginActivity.kt
      do {
        val serverMessage = JSONObject(readServerOutput())
        if (serverMessage.getString("type") == "error") {
          Thread.sleep(1000)
          activity.updateErrorMessage(serverMessage.getString("message"))
          activity.expandJoinButton(true)
        }
      } while (serverMessage.getString("message") != ":success")


      // Clear existing messages so there wont be duplicates if user
      // re-joins without quiting the app
      Messages.clearMessages()

      // Get chat history from server
      val chatHistoryJSONArray = JSONArray(readServerOutput())
      for (i in 0 until chatHistoryJSONArray.length()) {
        Messages.addMessageWithoutNotify(chatHistoryJSONArray.getJSONObject(i))
      }

      // Checks if server sent top chat data.
      readTopChatData()

      // Sleep is used to delay the opening of chat activity
      // so the login animation isn't interrupted on login activity.
      Thread.sleep(1000)
      activity.openChatActivity()

      while (true) {
        try {

          Log.d(TAG, "listening server output...")
          val outputInJSON = JSONObject(readServerOutput())

          when (outputInJSON.get("type")) {
            "error"             -> activity.updateErrorMessage(outputInJSON.getString("message"))
            "message", "update" -> Messages.addMessage(outputInJSON)
          }

          readTopChatData()

        } catch (e: Exception) {
          isConnected = false
          chatActivity.closeChatActivity()
          if (!userExitAction) activity.updateErrorMessage("lost connection to server")
          return
        }

      }
    } catch (e: Exception) {

      Log.d(TAG, "Could not connect to server.")
      Thread.sleep(1000)
      activity.expandJoinButton(true)
      activity.updateErrorMessage("can't connect to server")
      isConnected = false

    }

  }

  private fun readTopChatData() {
    try {
      val topChatData = JSONArray(readServerOutput())

      TopChatData.setNewTopChatData(topChatData)

    } catch (e: Exception) {
      Log.d(TAG, "failed to construct top chat data")
    }
  }

  private fun createConnection() {
    userExitAction = false
    val prefKey = App.resourses?.getString(R.string.pref_hostaddr)
    val prefDefVal = App.resourses?.getString(R.string.server_address_home)
    val hostAddress = preferences.getString(prefKey, prefDefVal)

    socket = Socket()
    socket.connect(InetSocketAddress(hostAddress, port), 2000)
    isConnected = true

    input = BufferedReader(InputStreamReader(socket.getInputStream()))
    output = socket.getOutputStream()

  }

  fun isConnected(): Boolean {
    return isConnected
  }

  fun closeConnection() {
    try {
      socket.close()
      Log.d(TAG, "connection closed")
    } catch (e: Exception) {
      Log.d(TAG, "could not close connection")
    }
  }

  fun addPreferences(prefs: SharedPreferences) {
    preferences = prefs
  }

  fun writeToServer(messageObject: String) {
    //TODO("try implementing this with a coroutine")
    thread {
      output.write(("$messageObject\n").toByteArray(Charset.defaultCharset()))
    }
  }

  fun registerChatActivity(activity: FromServerToChatActivity) {
    chatActivity = activity
  }

  fun exitServer() {
    userExitAction = true
  }

  private fun readServerOutput(): String? {
    return input.readLine()
  }

}