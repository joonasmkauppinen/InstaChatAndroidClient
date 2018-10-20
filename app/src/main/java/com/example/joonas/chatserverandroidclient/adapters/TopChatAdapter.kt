package com.example.joonas.chatserverandroidclient.adapters

import android.view.View
import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.joonas.chatserverandroidclient.R
import kotlinx.android.synthetic.main.topchat_list_item.view.*
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "TopChatAdapter"

class TopChatAdapter(private val context: Context?): RecyclerView.Adapter<TopChatViewHolder>() {

  private var topChatData = JSONArray()
  private var listItemFontMedium = Typeface.createFromAsset(context?.assets, "fonts/dosis_medium.ttf")
  private var listItemFontBold = Typeface.createFromAsset(context?.assets, "fonts/dosis_bold.ttf")

  fun updateTopChatList(topChatDataArray: JSONArray) {
    topChatData = topChatDataArray
    notifyDataSetChanged()
    Log.d(TAG, "updating top chat list: ${topChatData.toString(2)}")
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TopChatViewHolder {
    return TopChatItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.topchat_list_item, parent, false)
    )
  }

  override fun getItemCount(): Int {
    return topChatData.length()
  }

  override fun onBindViewHolder(holder: TopChatViewHolder?, position: Int) {
    val topChatItem = topChatData.getJSONObject(position)
    holder?.bind(topChatItem, position)
  }

  inner class TopChatItemViewHolder(view: View): TopChatViewHolder(view) {
    private var positionText: TextView = view.textView_position
    private var nameText: TextView = view.textView_name
    private var msgAmountText: TextView = view.textView_msgAmount
    private val viewDivider: View = view.itemDivider

    override fun bind(topChatObject: JSONObject, position: Int) {
      positionText.text = "${(position + 1)}."
      positionText.typeface = listItemFontBold
      nameText.text = topChatObject.getString("name")
      nameText.typeface = listItemFontMedium
      msgAmountText.text = topChatObject.getString("msgAmount")
      msgAmountText.typeface = listItemFontMedium
      if (position == topChatData.length() - 1) {
        viewDivider.visibility = View.GONE
      } else {
        viewDivider.visibility = View.VISIBLE
      }
    }
  }

}

open class TopChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
  open fun bind(topChatObject: JSONObject, position: Int) {}
}