package com.example.joonas.chatserverandroidclient.contract

interface FromServerToLoginActivity {
  fun openChatActivity()
  fun updateErrorMessage(msg: String)
  fun expandJoinButton(animate: Boolean)
  fun closeChatActivity()
}