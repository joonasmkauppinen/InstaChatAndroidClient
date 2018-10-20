package com.example.joonas.chatserverandroidclient.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.content.SharedPreferences
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.preference.PreferenceManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.adapters.MessageAdapter
import com.example.joonas.chatserverandroidclient.contract.FromServerToChatActivity
import com.example.joonas.chatserverandroidclient.model.Messages
import com.example.joonas.chatserverandroidclient.utils.Connection
import com.example.joonas.chatserverandroidclient.utils.JsonUtils.makeMessageObject

import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_top_chat.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "ChatActivity"

class ChatActivity : AppCompatActivity(), Observer, FromServerToChatActivity {

  private val formatter = DateTimeFormatter.ofPattern("HH:mm")
  private lateinit var adapter: MessageAdapter
  private lateinit var preferences: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat)

    Connection.registerChatActivity(this)
    preferences = PreferenceManager.getDefaultSharedPreferences(this)

    window.statusBarColor = getColor(R.color.colorMainWhite)
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

    messageList.layoutManager = LinearLayoutManager(this)
    adapter = MessageAdapter(this)
    messageList.adapter = adapter

    buttonBack_chat.setOnClickListener { _ ->
      this.onBackPressed()
    }

    buttonSend.setOnClickListener { _ ->
      if (editText_input.text.isNotEmpty()) {
        Connection.writeToServer(
                makeMessageObject(
                        type = "message",
                        user = preferences.getString(getString(R.string.pref_user), ""),
                        message = editText_input.text.toString(),
                        timeStamp = LocalDateTime.now().format(formatter)
                ).toString())
        editText_input.text.clear()
      }
    }

    editText_input.setOnClickListener { _ ->
      Handler().postDelayed({
        messageList.scrollToPosition(adapter.itemCount - 1)
      }, 100)
    }

    val messagesArray = Messages.getMessagesJSONArray()
    for (i in 0 until messagesArray.length()) {
      adapter.addMessage(messagesArray.getJSONObject(i))
    }

  }

  override fun onStart() {
    super.onStart()
    Messages.addObserver(this)
    runOnUiThread {
      messageList.scrollToPosition(adapter.itemCount - 1)
    }
  }

  override fun update(o: Observable?, arg: Any?) {
    if (arg is JSONObject) {
      runOnUiThread {
        adapter.addMessage(arg)
        messageList.scrollToPosition(adapter.itemCount - 1)
      }
    }
  }

  fun openTopChatters(view: View) {
    if (view is ImageButton) {
      hideKeyboard()
      supportFragmentManager.beginTransaction()
              .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
              .addToBackStack("TOP_CHATTERS")
              .add(R.id.top_chat_fragment_container, TopChatFragment.newInstance(), "TOP_CHATTERS")
              .commit()
    }
  }

  fun onTopChatCloseClicked(view: View) {
    if (view is Button) onBackPressed()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 0) {
      val topChatFragment = supportFragmentManager.findFragmentByTag("TOP_CHATTERS") as TopChatFragment
      topChatFragment.fadeOutAnim()
      Handler().postDelayed({
        super.onBackPressed()
      }, 400)
    } else {
      Connection.exitServer()
      super.onBackPressed()
    }
  }

  private fun hideKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
    )
  }

  override fun closeChatActivity() {
    hideKeyboard()
    this.finish()
  }
}

