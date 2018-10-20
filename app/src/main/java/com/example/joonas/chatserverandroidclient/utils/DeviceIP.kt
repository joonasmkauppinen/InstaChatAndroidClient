package com.example.joonas.chatserverandroidclient.utils

import android.content.Context
import android.net.wifi.WifiManager

class DeviceIP(context: Context) {

    private val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun getIpAddress(): String {
        return ipToString(wifiManager.connectionInfo.ipAddress)
    }

    private fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

}