package com.example.joonas.chatserverandroidclient.utils

object BackgroundAnim {
   fun calcStripeSpeed(progress: Int): Long {
    return 10000 - ( (progress - 1) * 100 ).toLong()
  }
}