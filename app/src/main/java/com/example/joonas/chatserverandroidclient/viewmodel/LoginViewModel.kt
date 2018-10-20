package com.example.joonas.chatserverandroidclient.viewmodel

import android.databinding.ObservableField
import android.util.Log
import java.util.*

private const val TAG = "LoginViewModel"

class LoginViewModel : IViewModel, Observer {

  val errorMessage = ObservableField<String>()

  override fun onCreate() {
    // Setting errorMessage to empty string so
    // it's not null when activity_login.xml
    // checks errorMessage's value.
    errorMessage.set("")
  }

  override fun update(o: Observable?, arg: Any?) {
    if (arg is String) errorMessage.set(arg.toString())
  }

}