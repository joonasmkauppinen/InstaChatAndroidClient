package com.example.joonas.chatserverandroidclient.view

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.RadioButton
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.preference.PreferenceManager
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.model.Messages
import com.example.joonas.chatserverandroidclient.utils.Connection
import com.example.joonas.chatserverandroidclient.utils.JsonUtils.makeMessageObject
import com.example.joonas.chatserverandroidclient.contract.FromServerToLoginActivity
import com.example.joonas.chatserverandroidclient.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.concurrent.thread
import android.animation.ValueAnimator
import com.example.joonas.chatserverandroidclient.utils.BackgroundAnim

private const val TAG = "MainActivity"
private const val JOIN_WIDTH = 290
private const val JOIN_HEIGHT = 50

class LoginActivity : AppCompatActivity(), FromServerToLoginActivity {

  private val viewModel = LoginViewModel()

  private lateinit var preferences: SharedPreferences
  private lateinit var editor: SharedPreferences.Editor

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    preferences = PreferenceManager.getDefaultSharedPreferences(this)
    editor = preferences.edit()

//  ********************** background animations ******************
    val animationDrawable: AnimationDrawable = layout_root.background as AnimationDrawable
    animationDrawable.setEnterFadeDuration(10)
    animationDrawable.setExitFadeDuration(5000)
    animationDrawable.start()

    val backgroundStripesAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.background_stripes_movement)
    backgroundStripesAnimation.interpolator = LinearInterpolator()
    stripes.startAnimation(backgroundStripesAnimation)
    stripes.animation.duration = BackgroundAnim.calcStripeSpeed(
      preferences.getString(getString(R.string.pref_bgspeed), "1").toInt()
    )

    realtimeBlurView.setBlurRadius( preferences.getString(getString(R.string.pref_bluramount), "3").toFloat() )
//  ***************************************************************

    // Custom font for collapsing toolbar title
    val customFont = Typeface.createFromAsset(this.assets, "fonts/pacifico_regular.ttf")
    collapsingToolbar.setExpandedTitleTypeface(customFont)
    collapsingToolbar.setCollapsedTitleTypeface(customFont)


    editText_login_username.setOnClickListener { _ -> expandToolbar(false) }
    editText_login_username.setOnEditorActionListener { _, actionId, _ ->
      when (actionId) { EditorInfo.IME_ACTION_DONE -> runOnUiThread { expandToolbar(true) }
      }
      false
    }

    button_login_join.setOnClickListener { _ -> loginToServer() }

    Connection.addPreferences(preferences)

    Messages.addObserver(viewModel)
    viewModel.onCreate()

  }

  override fun onResume() {
    super.onResume()

    if (Connection.isConnected()) {
      Connection.closeConnection()
    }

    if (editText_login_username.text.isNotEmpty()) {
      editText_login_username.text.clear()
      expandJoinButton(false)
    }

  }

  private fun loginToServer() {

    // Check if entered username is same as on shared preferences, if not set new preference.
    if (editText_login_username.text.toString() != preferences.getString(getString(R.string.pref_user), "")) {
      editor.putString(getString(R.string.pref_user), editText_login_username.text.toString())
      editor.apply()
    }

    hideKeyboard()
    expandToolbar(true)

    if (editText_login_username.text.isNotEmpty()) {

      collapseJoinButton()

      updateErrorMessage("")
      if (!Connection.isConnected()) {
        thread { Connection.connect(this) }
      } else {
        // Connection object opens chat fragment if username was valid
        Connection.writeToServer(
                makeMessageObject(
                        type = "command",
                        message = ":android ${editText_login_username.text}"
                ).toString()
        )
      }
    } else updateErrorMessage("please enter username")

  }

  private fun collapseJoinButton() {
    Handler().postDelayed({

      button_login_join.text = ""
      val widthAnimator = ValueAnimator.ofInt(button_login_join.width, button_login_join.height)
      widthAnimator.addUpdateListener { animation ->
        val animatedValue = animation.animatedValue as Int
        button_login_join.layoutParams.width = animatedValue
        button_login_join.requestLayout()
      }
      widthAnimator.duration = 300
      widthAnimator.start()

      Handler().postDelayed({
        button_login_join.visibility = View.GONE
        loadingSpinner.visibility = View.VISIBLE
      }, 280)

    }, 300)

  }

  override fun expandJoinButton(animate: Boolean) {
    runOnUiThread {

      if (!animate) {

        loadingSpinner.visibility = View.GONE
        button_login_join.visibility = View.VISIBLE
        button_login_join.layoutParams.width = (JOIN_WIDTH * this.resources.displayMetrics.density).toInt()
        button_login_join.text = getString(R.string.login_join_button)

      } else {

        loadingSpinner.visibility = View.GONE
        button_login_join.visibility = View.VISIBLE

        val widthAnimator = ValueAnimator.ofInt(
                (JOIN_HEIGHT * this.resources.displayMetrics.density).toInt(),
                (JOIN_WIDTH * this.resources.displayMetrics.density).toInt())
        widthAnimator.addUpdateListener { animation ->
          val animatedValue = animation.animatedValue as Int
          button_login_join.layoutParams.width = animatedValue
          button_login_join.requestLayout()
        }
        widthAnimator.duration = 300
        widthAnimator.start()

        Handler().postDelayed({
          button_login_join.text = getString(R.string.login_join_button)
        }, 100)

      }

    }

  }

  private fun hideKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
    )
  }

  private fun expandToolbar(state: Boolean) {
    appBarLayout.setExpanded(state, true)
  }

  override fun closeChatActivity() {
    hideKeyboard()
    onBackPressed()
  }

  override fun openChatActivity() {
    Log.d(TAG, "Opening chat activity...")
    val intent = Intent(this, ChatActivity::class.java).apply { }
    startActivity(intent)
  }

  override fun updateErrorMessage(msg: String) {
    runOnUiThread {
      if (msg == "") {
        textView_error_message.visibility = View.GONE
      } else {
        textView_error_message.visibility = View.VISIBLE
        textView_error_message.text = msg
        val errorAnim = AnimationUtils.loadAnimation(this, R.anim.error_reveal)
        errorAnim.interpolator = LinearInterpolator()
        textView_error_message.startAnimation(errorAnim)
      }
    }
  }

  fun openSettingsFragment(view: View) {
    if (view is ImageButton) {
      supportFragmentManager.beginTransaction()
              .addToBackStack("LOGIN_SETTINGS")
              .add(R.id.settings_fragment_container, SettingsFragment.newInstance(), "LOGIN_SETTINGS")
              .commit()
    }
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 0) {
      val settingsFragment = supportFragmentManager.findFragmentByTag("LOGIN_SETTINGS") as SettingsFragment
      settingsFragment.revealAnim(false)
      Handler().postDelayed({
        super.onBackPressed()
      }, 700)
    } else {
      super.onBackPressed()
    }
  }

  fun onRadioButtonClick(view: View) {
    if (view is RadioButton) {
      when (view.id) {
        R.id.radioButton_home -> {
          editor.putString(getString(R.string.pref_hostaddr), getString(R.string.server_address_home))
          editor.apply()
        }
        R.id.radioButton_hotspot -> {
          editor.putString(getString(R.string.pref_hostaddr), getString(R.string.server_address_hotsopt))
          editor.apply()
        }
      }
    }
  }

}