package com.example.joonas.chatserverandroidclient.adapters

import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.model.App
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.other_message.view.*
import kotlinx.android.synthetic.main.chat_update_message.view.*
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "MessageAdapter"

private const val VIEW_TYPE_MY_MESSAGE    = 1
private const val VIEW_TYPE_OTHER_MESSAGE = 2
private const val VIEW_TYPE_CHAT_UPDATE   = 3

class MessageAdapter(private val context: Context) : RecyclerView.Adapter<MessageViewHolder>() {

  private val messages = JSONArray()
  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  private var lastPosition = -1

  fun addMessage(message: JSONObject) {
    Log.d(TAG, "adding message to MessageAdapter messages list")
    messages.put(message)
    notifyDataSetChanged()
  }

  // Animation functions for all message types
  private fun setMyMessageAnimation(view: View, position: Int) {
    if (position > lastPosition) {
      val itemAnim = AnimationUtils.loadAnimation(context, R.anim.my_message_reveal)
      view.startAnimation(itemAnim)
      lastPosition = position
    }
  }
  private fun setOtherMessageAnimation(view: View, position: Int) {
    if (position > lastPosition) {
      val itemAnim = AnimationUtils.loadAnimation(context, R.anim.other_message_reveal)
      view.startAnimation(itemAnim)
      lastPosition = position
    }
  }
  private fun setUpdateMessageAnimation(view: View, position: Int) {
    if (position > lastPosition) {
      val itemAnim = AnimationUtils.loadAnimation(context, R.anim.update_message_reveal)
      view.startAnimation(itemAnim)
      lastPosition = position
    }
  }
  // -----------------------------------------


  override fun getItemCount(): Int {
    return messages.length()
  }

  override fun getItemViewType(position: Int): Int {
    val message: JSONObject = messages.getJSONObject(position)
    val currentUser = preferences.getString(App.resourses?.getString(R.string.pref_user), "")

    return when (message.getString("type")) {
      "update" -> VIEW_TYPE_CHAT_UPDATE
      else -> {
        if (currentUser == message.getString("user")) {
          VIEW_TYPE_MY_MESSAGE
        } else {
          VIEW_TYPE_OTHER_MESSAGE
        }
      }
    }

  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
    return when(viewType) {
      VIEW_TYPE_CHAT_UPDATE -> UpdateMessageViewHolder( LayoutInflater.from(context).inflate(R.layout.chat_update_message, parent, false) )
      VIEW_TYPE_MY_MESSAGE  -> MyMessageViewHolder( LayoutInflater.from(context).inflate(R.layout.my_message, parent, false) )
      else                  -> OtherMessageViewHolder( LayoutInflater.from(context).inflate(R.layout.other_message, parent, false) )
    }
  }

  override fun onBindViewHolder(holder: MessageViewHolder?, position: Int) {
    val message = messages.getJSONObject(position)
    holder?.bind(message)

    when (holder) {
      is MyMessageViewHolder     -> setMyMessageAnimation(holder.itemView, position)
      is OtherMessageViewHolder  -> setOtherMessageAnimation(holder.itemView, position)
      is UpdateMessageViewHolder -> setUpdateMessageAnimation(holder.itemView, position)
    }

  }

  inner class MyMessageViewHolder(view: View) : MessageViewHolder(view) {
    private var messageText: TextView   = view.myMessage
    private var timeStampText: TextView = view.myTimeStamp

    override fun bind(message: JSONObject) {
      messageText.text   = message.getString("message")
      timeStampText.text = message.getString("timestamp")
    }
  }

  inner class OtherMessageViewHolder(view: View) : MessageViewHolder(view) {
    private var usernameText: TextView  = view.otherUsername
    private var messageText: TextView   = view.otherMessage
    private var timeStampText: TextView = view.otherTimeStamp

    override fun bind(message: JSONObject) {
      usernameText.text  = message.getString("user")
      messageText.text   = message.getString("message")
      timeStampText.text = message.getString("timestamp")
    }
  }

  inner class UpdateMessageViewHolder(view: View) : MessageViewHolder(view) {
    private val messageText: TextView = view.updateMessage

    override fun bind(message: JSONObject) {
      messageText.text = message.getString("message")
    }
  }

}

open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  open fun bind(message: JSONObject) {}
}