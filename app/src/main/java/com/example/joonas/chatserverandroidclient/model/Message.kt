package com.example.joonas.chatserverandroidclient.model

import java.time.LocalDateTime

data class Message(val username: String,
                   val message: String,
                   val timeStamp: LocalDateTime)