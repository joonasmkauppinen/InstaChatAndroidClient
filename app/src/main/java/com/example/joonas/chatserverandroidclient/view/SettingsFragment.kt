package com.example.joonas.chatserverandroidclient.view

import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.SeekBar
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.example.joonas.chatserverandroidclient.R
import com.example.joonas.chatserverandroidclient.utils.BackgroundAnim
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

private const val TAG = "SettingsFragment"

class SettingsFragment : Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener {

  private lateinit var preferences: SharedPreferences
  private lateinit var editor: SharedPreferences.Editor

  companion object {
    fun newInstance(): SettingsFragment {
      return SettingsFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_settings, container, false)
    retainInstance = true
    view.buttonCloseSettings.setOnClickListener(this)
    view.seekbar_bluramount.setOnSeekBarChangeListener(this)
    view.seekbar_stripespeed.setOnSeekBarChangeListener(this)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupPreferenceValues()
    if (savedInstanceState == null) revealAnim(true)
  }

  private fun setupPreferenceValues() {
    preferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)

    val blurAmount            = preferences.getString(getString(R.string.pref_bluramount), "8")
    val stripeSpeed           = preferences.getString(getString(R.string.pref_bgspeed), "10")
    val selectedServerAddress = preferences.getString(getString(R.string.pref_hostaddr), getString(R.string.server_address_home))

    textView_blurAmount_value.text = blurAmount
    seekbar_bluramount.progress    = blurAmount.toInt()

    textView_stripeSpeed_value.text = stripeSpeed
    seekbar_stripespeed.progress    = stripeSpeed.toInt()

    if (selectedServerAddress == getString(R.string.server_address_home)) {
      radioButton_home.isChecked = true
      radioButton_hotspot.isChecked = false
    } else {
      radioButton_home.isChecked = false
      radioButton_hotspot.isChecked = true
    }
  }

  fun revealAnim(opening: Boolean) {

    val location = IntArray(2)
    activity?.buttonSettings?.getLocationOnScreen(location)
    val cx = location[0] + (activity?.buttonSettings?.width as Int / 2)
    val cy = location[1] + (activity?.buttonSettings?.height as Int / 2)

    val pow = 2.0
    val screenWidth: Double = activity?.settings_fragment_container?.width?.toDouble() as Double
    val screenHeight = activity?.settings_fragment_container?.height?.toDouble() as Double
    val screenDiagonal = Math.sqrt( Math.pow(screenWidth, pow) + Math.pow(screenHeight, pow) )

    var startRadius = 0f
    var endRadius = screenDiagonal.toFloat()
    if (!opening) {
      startRadius = screenDiagonal.toFloat()
      endRadius = 0f
    }

    val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, endRadius)
    anim.duration = 700
    anim.interpolator = FastOutSlowInInterpolator()
    anim.start()
  }

  // SEEKBAR CHANGE LISTENER
  override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    when (seekBar?.id) {
      R.id.seekbar_bluramount -> {
        textView_blurAmount_value.text = progress.toString()
        activity!!.realtimeBlurView.setBlurRadius(progress.toFloat())
      }
      R.id.seekbar_stripespeed -> {
        textView_stripeSpeed_value.text = progress.toString()
        val stripeAnimSpeed = BackgroundAnim.calcStripeSpeed(progress)
        activity!!.stripes.animation.duration = stripeAnimSpeed
      }
    }
  }
  override fun onStartTrackingTouch(seekBar: SeekBar?) {

    settingsRoot.setBackgroundColor(Color.TRANSPARENT)
    settingsToolbar.visibility = View.INVISIBLE
    hostServerRoot.visibility = View.INVISIBLE

      when (seekBar?.id) {
        R.id.seekbar_bluramount -> {
          stripeSpeedRoot.visibility = View.INVISIBLE
        }
        R.id.seekbar_stripespeed -> {
          blurAmountRoot.visibility = View.INVISIBLE
        }
      }
  }
  override fun onStopTrackingTouch(seekBar: SeekBar?) {

    settingsRoot.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorMainWhite))
    settingsToolbar.visibility = View.VISIBLE
    hostServerRoot.visibility = View.VISIBLE

    editor = preferences.edit()
    when (seekBar?.id) {
      R.id.seekbar_bluramount -> {
        editor.putString(getString(R.string.pref_bluramount), seekBar.progress.toString())
        editor.apply()
        stripeSpeedRoot.visibility = View.VISIBLE
      }
      R.id.seekbar_stripespeed -> {
        editor.putString(getString(R.string.pref_bgspeed), seekBar.progress.toString())
        editor.apply()
        blurAmountRoot.visibility = View.VISIBLE
      }
    }
  }

  // ON CLICK LISTENER FOR CLOSE BUTTON
  override fun onClick(v: View?) {
    revealAnim(false)
    Handler().postDelayed({
      activity?.supportFragmentManager?.popBackStackImmediate()
    }, 700)
  }

}