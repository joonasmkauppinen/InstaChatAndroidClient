package com.example.joonas.chatserverandroidclient.view


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.adapters.TopChatAdapter
import com.example.joonas.chatserverandroidclient.model.TopChatData
import kotlinx.android.synthetic.main.fragment_top_chat.*
import org.json.JSONArray
import java.util.*

class TopChatFragment : Fragment(), Observer {

  private lateinit var adapter: TopChatAdapter

  companion object {
    fun newInstance(): TopChatFragment {
      return TopChatFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_top_chat, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    TopChatData.addObserver(this)

    topchat_list.layoutManager = LinearLayoutManager(activity)
    adapter = TopChatAdapter(activity?.baseContext)
    topchat_list.adapter = adapter
    adapter.updateTopChatList(TopChatData.getTopChatData())
  }

  fun fadeOutAnim() {
    topChatRoot.startAnimation( AnimationUtils.loadAnimation(activity, android.R.anim.fade_out) )
  }

  override fun update(o: Observable?, arg: Any?) {
    if (arg is JSONArray) adapter.updateTopChatList(arg)
  }

}
